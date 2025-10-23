package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * A {@linkplain FinalizationStep} does nothing
 *
 * @param <Request>
 * @param <Response>
 */
public class NoopFinalizationStep<Request, Response> implements
    FinalizationStep<Request, Request, Response, Response> {


  @Override
  public Response finalize(@Nullable Request request,
      @Nullable Request request2,
      Response response) {
    return response;
  }
}
