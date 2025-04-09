package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Initialization step to update request
 *
 * @param <Request> Request type
 */
public interface InitializationStep<Request> {

  /**
   * Initialize the request
   *
   * @param request Request
   * @return Updated request
   */
  @Nullable
  Request initialize(@Nullable Request request);
}
