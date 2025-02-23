package com.javaaidev.agenticpatterns.core.observation;

import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;

public interface AgentExecutionObservationConvention extends
    ObservationConvention<AgentExecutionObservationContext> {

  @Override
  default boolean supportsContext(Context context) {
    return context instanceof AgentExecutionObservationContext;
  }
}
