package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ParallelizationWorkflowAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  public record SubtaskContext<Request>(
      String taskId,
      TaskExecutionAgent<?, ?> task,
      Function<Request, ?> requestTransformer,
      @Nullable Subtask<?> job,
      @Nullable Object result,
      @Nullable Throwable error
  ) {

    public static <Request, TaskRequest, TaskResponse> SubtaskContext<Request> create(String taskId,
        TaskExecutionAgent<TaskRequest, TaskResponse> task,
        Function<Request, TaskRequest> requestTransformer) {
      return new SubtaskContext<>(taskId, task, requestTransformer, null, null, null);
    }

    public SubtaskContext<Request> taskStarted(Subtask<?> job) {
      return new SubtaskContext<>(this.taskId(), this.task(), this.requestTransformer(), job, null,
          null);
    }

    public SubtaskContext<Request> collectResult() {
      if (this.job() == null) {
        return this;
      }
      return new SubtaskContext<>(this.taskId(), this.task(), this.requestTransformer(), this.job(),
          this.job().get(),
          this.job().exception());
    }
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

  public record PartialResult(@Nullable Object result, @Nullable Throwable error) {

  }

  protected Map<String, PartialResult> runSubtasks(@Nullable Request request) {
    try (var scope = new StructuredTaskScope<>()) {
      var jobs = subtasks.stream().map(context -> {
        LOGGER.info("Starting subtask {}", context.taskId());
        var job = scope.fork(
            () -> context.task().call(context.requestTransformer().apply(request)));
        return context.taskStarted(job);
      });
      try {
        LOGGER.info("Waiting for all subtasks to finish, timeout in {}", getMaxExecutionDuration());
        scope.joinUntil(Instant.now().plus(getMaxExecutionDuration()));
      } catch (InterruptedException | TimeoutException e) {
        throw new AgentExecutionException("Failed to execute task", e);
      }
      LOGGER.info("All subtasks completed, assembling the results");
      return jobs.map(SubtaskContext::collectResult)
          .collect(Collectors.toMap(SubtaskContext::taskId,
              (task -> new PartialResult(task.result(), task.error()))));
    }
  }

  public abstract static class DirectAssembling<Request, Response> extends
      ParallelizationWorkflowAgent<Request, Response> {

    @Override
    protected String getPromptTemplate() {
      return "";
    }

    @Override
    public Response call(@Nullable Request request) {
      return assemble(runSubtasks(request));
    }

    protected abstract Response assemble(Map<String, PartialResult> results);
  }

  public abstract static class PromptBasedAssembling<Request, Response> extends
      ParallelizationWorkflowAgent<Request, Response> {

    protected abstract @Nullable Map<String, Object> getAssemblingPromptContext(
        Map<String, PartialResult> results);

    @Override
    protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
      var results = runSubtasks(request);
      return getAssemblingPromptContext(results);
    }
  }
}
