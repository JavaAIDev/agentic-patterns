package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Initialization step to update request
 *
 * @param <Request> Request type
 */
public interface InitializationStep<Request, GenInput> {

  /**
   * Initialize the request
   *
   * @param request Request
   * @return Generation input
   */
  @Nullable
  GenInput initialize(@Nullable Request request);
}
