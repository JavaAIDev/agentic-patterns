package com.javaaidev.agenticpatterns.core;

import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;

public abstract class AbstractAgenticWorkflowBuilder<Request, Response, T extends AbstractAgenticWorkflowBuilder<Request, Response, T>> implements
    AgenticWorkflow.Builder<Request, Response, T> {

  @Nullable
  protected String name;
  @Nullable
  protected ObservationRegistry observationRegistry;

  @SuppressWarnings("unchecked")
  protected T self() {
    return (T) this;
  }

  public T name(String name) {
    this.name = name;
    return self();
  }

  public T observationRegistry(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
    return self();
  }
}
