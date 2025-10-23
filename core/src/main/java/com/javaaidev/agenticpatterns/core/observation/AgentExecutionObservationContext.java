package com.javaaidev.agenticpatterns.core.observation;

import io.micrometer.observation.transport.RequestReplySenderContext;

public class AgentExecutionObservationContext extends
    RequestReplySenderContext<Object, Object> {

  private final String agentName;

  public AgentExecutionObservationContext(
      String agentName, Object input) {
    super((carrier, key, value) -> {

    });
    this.agentName = agentName;
    setCarrier(input);
  }

  public String getAgentName() {
    return agentName;
  }
}
