package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

  public record TaskExecutionContext(
      Future<?> job,
      Duration maxWaitTime,
      @Nullable Object result,
      @Nullable Throwable error
  ) {

    public TaskExecutionContext(Future<?> job, Duration maxWaitTime) {
      this(job, maxWaitTime, null, null);
    }

    public TaskExecutionContext collectResult() {
      try {
        var result = job().get(maxWaitTime().toSeconds(), TimeUnit.SECONDS);
        return new TaskExecutionContext(job(), maxWaitTime(), result, null);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        return new TaskExecutionContext(job(), maxWaitTime(), null, job().exceptionNow());
      }
    }
  }

  public record SubtaskContext<Request>(
      SubtaskCreationRequest<Request> creationRequest,
      @Nullable TaskExecutionContext taskExecutionContext
  ) {

    public static <Request, TaskRequest, TaskResponse> SubtaskContext<Request> create(String taskId,
        TaskExecutionAgent<TaskRequest, TaskResponse> task,
        Function<Request, TaskRequest> requestTransformer) {
      return create(new SubtaskCreationRequest<>(taskId, task, requestTransformer));
    }

    public static <Request> SubtaskContext<Request> create(
        SubtaskCreationRequest<Request> creationRequest) {
      return new SubtaskContext<>(creationRequest, null);
    }

    public SubtaskContext<Request> taskStarted(Future<?> job, Duration maxWaitTime) {
      return new SubtaskContext<>(this.creationRequest(),
          new TaskExecutionContext(job, maxWaitTime));
    }

    public SubtaskContext<Request> collectResult() {
      return new SubtaskContext<>(creationRequest(),
          Objects.requireNonNull(taskExecutionContext(), "task execution context cannot be null")
              .collectResult());
    }

    public String taskId() {
      return creationRequest().taskId();
    }

    public @Nullable Object result() {
      return taskExecutionContext() != null ? taskExecutionContext().result() : null;
    }

    public @Nullable Throwable error() {
      return taskExecutionContext() != null ? taskExecutionContext().error() : null;
    }
  }

  public ParallelizationWorkflowAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  public ParallelizationWorkflowAgent(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
  }

  protected final CopyOnWriteArrayList<SubtaskContext> subtasks = new CopyOnWriteArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizationWorkflowAgent.class);

  protected <TaskRequest, TaskResponse> void addSubtask(String taskId,
      TaskExecutionAgent<TaskRequest, TaskResponse> subtask,
      Function<Request, TaskRequest> requestTransformer) {
    subtasks.add(SubtaskContext.create(taskId, subtask, requestTransformer));
  }

  protected Duration getMaxTaskExecutionDuration() {
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

  protected ExecutorService getTaskExecutorService() {
    var executor = Executors.newThreadPerTaskExecutor(
        Thread.ofVirtual().name("agent-task-", 1).factory());
    return ContextExecutorService.wrap(executor,
        ContextSnapshotFactory.builder().clearMissing(true).build());
  }

  protected TaskExecutionResults runSubtasks(@Nullable Request request) {
    var createdTasks = createTasks(request);
    if (createdTasks != null) {
      subtasks.addAll(createdTasks.stream().map(SubtaskContext::create).toList());
    }
    try (var executor = getTaskExecutorService()) {
      var jobs = subtasks.stream().map(context -> {
        var creationRequest = context.creationRequest();
        LOGGER.info("Starting subtask {}", creationRequest.taskId());
        var job = executor.submit(
            () -> creationRequest.task().call(creationRequest.requestTransformer().apply(request)));
        return context.taskStarted(job, getMaxTaskExecutionDuration());
      }).toList();
      LOGGER.info("Waiting for all subtasks to finish");
      jobs.forEach(SubtaskContext::collectResult);
      LOGGER.info("All subtasks completed, assembling the results");
      var results = jobs.stream().map(SubtaskContext::collectResult)
          .collect(Collectors.toMap(SubtaskContext::taskId,
              (task -> new PartialResult(task.result(), task.error())), (a, b) -> b));
      return new TaskExecutionResults(results);
    }
  }

}
