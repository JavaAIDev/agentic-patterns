package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * An {@linkplain InitializationStep} does nothing
 *
 * @param <Request>
 */
public class NoopInitializationStep<Request> implements
    InitializationStep<Request, Request> {

  @Override
  @Nullable
  public Request initialize(@Nullable Request request) {
    return request;
  }
}
