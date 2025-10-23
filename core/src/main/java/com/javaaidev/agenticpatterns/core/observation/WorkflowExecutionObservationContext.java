package com.javaaidev.agenticpatterns.core.observation;

import io.micrometer.observation.transport.RequestReplySenderContext;

public class WorkflowExecutionObservationContext extends
    RequestReplySenderContext<Object, Object> {

  private final String workflowName;

  public WorkflowExecutionObservationContext(
      String workflowName, Object input) {
    super((carrier, key, value) -> {

    });
    this.workflowName = workflowName;
    setCarrier(input);
  }

  public String getWorkflowName() {
    return workflowName;
  }
}
