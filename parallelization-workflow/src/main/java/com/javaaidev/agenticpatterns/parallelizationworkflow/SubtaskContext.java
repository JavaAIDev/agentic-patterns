package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Subtask context for management
 *
 * @param creationRequest  Subtask creation request
 * @param executionContext Subtask execution context
 * @param <Request>        Request type
 */
public record SubtaskContext<Request>(
    SubtaskCreationRequest<Request> creationRequest,
    @Nullable SubtaskExecutionContext executionContext
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
        new SubtaskExecutionContext(job, maxWaitTime));
  }

  public SubtaskContext<Request> collectResult() {
    return new SubtaskContext<>(creationRequest(),
        Objects.requireNonNull(executionContext(), "task execution context cannot be null")
            .collectResult());
  }

  public String taskId() {
    return creationRequest().taskId();
  }

  public @Nullable Object result() {
    return executionContext() != null ? executionContext().result() : null;
  }

  public @Nullable Throwable error() {
    return executionContext() != null ? executionContext().error() : null;
  }
}
