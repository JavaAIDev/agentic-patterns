package com.javaaidev.agenticpatterns.parallelizationworkflow;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

/**
 * Task execution context
 *
 * @param job         Job
 * @param maxWaitTime Max wait time for the task to finish
 * @param result      Successful result
 * @param error       error
 */
public record SubtaskExecutionContext(
    Future<?> job,
    Duration maxWaitTime,
    @Nullable Object result,
    @Nullable Throwable error
) {

  public SubtaskExecutionContext(Future<?> job, Duration maxWaitTime) {
    this(job, maxWaitTime, null, null);
  }

  public SubtaskExecutionContext collectResult() {
    try {
      var result = job().get(maxWaitTime().toSeconds(), TimeUnit.SECONDS);
      return new SubtaskExecutionContext(job(), maxWaitTime(), result, null);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      return new SubtaskExecutionContext(job(), maxWaitTime(), null,
          job().exceptionNow());
    }
  }
}
