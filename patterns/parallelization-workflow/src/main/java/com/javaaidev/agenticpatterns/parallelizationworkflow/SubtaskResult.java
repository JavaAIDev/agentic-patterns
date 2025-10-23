package com.javaaidev.agenticpatterns.parallelizationworkflow;

import org.jspecify.annotations.Nullable;

/**
 * Execution result of a subtask
 *
 * @param result Successful result
 * @param error  Error
 */
public record SubtaskResult(@Nullable Object result,
                            @Nullable Throwable error) {

  /**
   * Has successful result
   *
   * @return
   */
  public boolean hasResult() {
    return result() != null;
  }

  /**
   * Has error
   *
   * @return
   */
  public boolean hasError() {
    return error() != null;
  }
}
