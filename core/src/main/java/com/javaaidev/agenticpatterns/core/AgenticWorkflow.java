package com.javaaidev.agenticpatterns.core;

import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;

/**
 * Agentic workflow
 *
 * @param <Request>  Type of request
 * @param <Response> Type of response
 */
public interface AgenticWorkflow<Request, Response> {

  /**
   * Execute this workflow
   *
   * @param request Request
   * @return Response
   */
  Response execute(@Nullable Request request);

  /**
   * Name of this workflow
   *
   * @return name
   */
  default String getName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Builder to create {@linkplain AgenticWorkflow}
   *
   * @param <Request>  Type of request
   * @param <Response> Type of response
   * @param <T>        Type of {@linkplain AgenticWorkflow}
   */
  interface Builder<Request, Response, T extends AgenticWorkflow.Builder<Request, Response, T>> {

    T name(String name);

    T observationRegistry(ObservationRegistry observationRegistry);

    AgenticWorkflow<Request, Response> build();
  }

  static <Request, Response> CustomWorkflowBuilder<Request, Response> custom() {
    return new CustomWorkflowBuilder<>();
  }

}
