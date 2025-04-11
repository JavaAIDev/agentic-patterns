package com.javaaidev.agenticpatterns.core;

import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;

public interface AgenticWorkflow<Request, Response> {

  Response execute(@Nullable Request request);

  default String getName() {
    return this.getClass().getSimpleName();
  }

  interface Builder<Request, Response, T extends AgenticWorkflow.Builder<Request, Response, T>> {

    T name(String name);

    T observationRegistry(ObservationRegistry observationRegistry);

    AgenticWorkflow<Request, Response> build();
  }

  static <Request, Response> SingleStepWorkflowBuilder<Request, Response> single() {
    return new SingleStepWorkflowBuilder<>();
  }

  static <Request, Response> CompositeWorkflowBuilder<Request, Response> composite() {
    return new CompositeWorkflowBuilder<>();
  }
}
