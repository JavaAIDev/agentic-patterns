package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Finalize the response
 *
 * @param <Request>   Type of request
 * @param <GenInput>  Type of generation input
 * @param <GenOutput> Type of generation output
 * @param <Response>  Type of response
 */
public interface FinalizationStep<Request, GenInput, GenOutput, Response> {

  /**
   * Finalize the response
   *
   * @param request   Request
   * @param genInput  Generation input
   * @param genOutput Generation output
   * @return Response
   */
  Response finalize(@Nullable Request request, @Nullable GenInput genInput,
      GenOutput genOutput);

  record FinalizationInput<Req, GenIn, GenOut>(
      @Nullable Req request,
      @Nullable GenIn genInput,
      GenOut genOutput) {

  }
}
