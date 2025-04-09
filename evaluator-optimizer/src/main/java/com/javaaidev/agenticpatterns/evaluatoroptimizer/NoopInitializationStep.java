package com.javaaidev.agenticpatterns.evaluatoroptimizer;

/**
 * An {@linkplain InitializationStep} does nothing
 *
 * @param <Request>
 */
public class NoopInitializationStep<Request> implements InitializationStep<Request> {

  @Override
  public Request initialize(Request request) {
    return request;
  }
}
