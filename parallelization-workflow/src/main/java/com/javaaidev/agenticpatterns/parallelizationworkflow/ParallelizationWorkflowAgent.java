package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.Utils;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.StructuredTaskScope.Subtask.State;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

public abstract class ParallelizationWorkflowAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  public record SubtaskCreationRequest<Request>(
      String taskId,
      TaskExecutionAgent<?, ?> task,
      Function<Request, ?> requestTransformer
  ) {

  }

  public record SubtaskContext<Request>(
      SubtaskCreationRequest<Request> creationRequest,
      @Nullable Subtask<?> job,
      @Nullable Object result,
      @Nullable Throwable error
  ) {

    public static <Request, TaskRequest, TaskResponse> SubtaskContext<Request> create(String taskId,
        TaskExecutionAgent<TaskRequest, TaskResponse> task,
        Function<Request, TaskRequest> requestTransformer) {
      return create(new SubtaskCreationRequest<>(taskId, task, requestTransformer));
    }

    public static <Request> SubtaskContext<Request> create(
        SubtaskCreationRequest<Request> creationRequest) {
      return new SubtaskContext<>(creationRequest, null, null, null);
    }

    public SubtaskContext<Request> taskStarted(Subtask<?> job) {
      return new SubtaskContext<>(this.creationRequest(), job, null,
          null);
    }

    public SubtaskContext<Request> collectResult() {
      if (this.job() == null) {
        return this;
      }
      var state = this.job().state();
      return new SubtaskContext<>(this.creationRequest(), this.job(),
          state == State.SUCCESS ? this.job().get() : null,
          state == State.FAILED ? this.job().exception() : null);
    }

    public String taskId() {
      return creationRequest().taskId();
    }
  }

  public ParallelizationWorkflowAgent(ChatClient chatClient) {
    super(chatClient);
  }

  public ParallelizationWorkflowAgent(ChatClient chatClient, @Nullable Type responseType) {
    super(chatClient, responseType);
  }

  protected final CopyOnWriteArrayList<SubtaskContext> subtasks = new CopyOnWriteArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizationWorkflowAgent.class);

  protected <TaskRequest, TaskResponse> void addSubtask(String taskId,
      TaskExecutionAgent<TaskRequest, TaskResponse> subtask,
      Function<Request, TaskRequest> requestTransformer) {
    subtasks.add(SubtaskContext.create(taskId, subtask, requestTransformer));
  }

  protected Duration getMaxExecutionDuration() {
    return Duration.ofMinutes(3);
  }

  @Nullable
  protected List<SubtaskCreationRequest<Request>> createTasks(@Nullable Request request) {
    return List.of();
  }

  public record PartialResult(@Nullable Object result, @Nullable Throwable error) {

    public boolean hasResult() {
      return result() != null;
    }

    public boolean hasError() {
      return error() != null;
    }
  }

  public record TaskExecutionResults(Map<String, PartialResult> results) {

    public Map<String, Object> allSuccessfulResults() {
      return results().entrySet().stream().filter(entry -> entry.getValue().hasResult())
          .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().result()));
    }
  }

  protected TaskExecutionResults runSubtasks(@Nullable Request request) {
    var createdTasks = createTasks(request);
    if (createdTasks != null) {
      subtasks.addAll(createdTasks.stream().map(SubtaskContext::create).toList());
    }
    try (var scope = new StructuredTaskScope<>()) {
      var jobs = subtasks.stream().map(context -> {
        var creationRequest = context.creationRequest();
        LOGGER.info("Starting subtask {}", creationRequest.taskId());
        var job = scope.fork(
            () -> creationRequest.task().call(creationRequest.requestTransformer().apply(request)));
        return context.taskStarted(job);
      }).toList();
      try {
        LOGGER.info("Waiting for all subtasks to finish, timeout in {}", getMaxExecutionDuration());
        scope.joinUntil(Instant.now().plus(getMaxExecutionDuration()));
      } catch (InterruptedException | TimeoutException e) {
        LOGGER.error("Error occurred when executing subtask, check status for individual subtask",
            e);
      }
      LOGGER.info("All subtasks completed, assembling the results");
      var results = jobs.stream().map(SubtaskContext::collectResult)
          .collect(Collectors.toMap(SubtaskContext::taskId,
              (task -> new PartialResult(task.result(), task.error())), (a, b) -> b));
      return new TaskExecutionResults(results);
    }
  }

  public abstract static class DirectAssembling<Request, Response> extends
      ParallelizationWorkflowAgent<Request, Response> {

    public DirectAssembling(ChatClient chatClient) {
      super(chatClient);
    }

    public DirectAssembling(ChatClient chatClient, @Nullable Type responseType) {
      super(chatClient, responseType);
    }

    @Override
    protected String getPromptTemplate() {
      return "";
    }

    @Override
    public Response call(@Nullable Request request) {
      return assemble(runSubtasks(request));
    }

    protected abstract Response assemble(TaskExecutionResults results);
  }

  public abstract static class PromptBasedAssembling<Request, Response> extends
      ParallelizationWorkflowAgent<Request, Response> {

    public PromptBasedAssembling(ChatClient chatClient) {
      super(chatClient);
    }

    public PromptBasedAssembling(ChatClient chatClient, @Nullable Type responseType) {
      super(chatClient, responseType);
    }

    protected abstract @Nullable Map<String, Object> getSubtasksPromptContext(
        TaskExecutionResults results);

    protected @Nullable Map<String, Object> getParentPromptContext(@Nullable Request request) {
      return Map.of();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
      return Utils.mergeMap(getParentPromptContext(request),
          getSubtasksPromptContext(runSubtasks(request)));
    }
  }
}
