package com.javaaidev.agenticpatterns.core.observation;

import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;

public interface WorkflowExecutionObservationConvention extends
    ObservationConvention<WorkflowExecutionObservationContext> {

  @Override
  default boolean supportsContext(Context context) {
    return context instanceof WorkflowExecutionObservationContext;
  }
}
