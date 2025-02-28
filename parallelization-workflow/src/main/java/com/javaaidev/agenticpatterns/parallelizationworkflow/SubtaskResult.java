package com.javaaidev.agenticpatterns.parallelizationworkflow;

import org.jspecify.annotations.Nullable;

/**
 * Execution result of a subtask
 *
 * @param result Successful result
 * @param error  Error
 */
public record SubtaskResult(@Nullable Object result, @Nullable Throwable error) {

  public boolean hasResult() {
    return result() != null;
  }

  public boolean hasError() {
    return error() != null;
  }
}
