package com.javaaidev.agenticpatterns.core;

import org.jspecify.annotations.Nullable;

public interface AgenticWorkflow<Request, Response> {

  Response execute(@Nullable Request request);

  default String getName() {
    return this.getClass().getSimpleName();
  }
}
