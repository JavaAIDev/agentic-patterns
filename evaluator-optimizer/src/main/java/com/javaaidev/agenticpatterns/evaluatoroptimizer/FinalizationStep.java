package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Finalize the response
 *
 * @param <Request>  Type of request
 * @param <Result>   Type of intermediate result
 * @param <Response> Type of response
 */
public interface FinalizationStep<Request, Result, Response> {

  /**
   * Finalize the response
   *
   * @param request Request
   * @param result  Intermediate result
   * @return Response
   */
  Response finalize(@Nullable Request request, Result result);
}
