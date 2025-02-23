package com.javaaidev.agenticpatterns.core.observation;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.core.observation.AgentExecutionObservationDocumentation.HighCardinalityKeyNames;
import com.javaaidev.agenticpatterns.core.observation.AgentExecutionObservationDocumentation.LowCardinalityKeyNames;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

public class DefaultAgentExecutionObservationConvention implements
    AgentExecutionObservationConvention {

  private String defaultName = "agent.execute";


  @Override
  public String getName() {
    return defaultName;
  }

  @Override
  public KeyValues getLowCardinalityKeyValues(AgentExecutionObservationContext context) {
    return KeyValues.of(agentName(context));
  }

  @Override
  public KeyValues getHighCardinalityKeyValues(AgentExecutionObservationContext context) {
    return KeyValues.of(
        agentExecutionInput(context),
        agentExecutionOutput(context)
    );
  }

  private KeyValue agentName(AgentExecutionObservationContext context) {
    return KeyValue.of(
        LowCardinalityKeyNames.AGENT_NAME, context.getAgentName()
    );
  }

  private KeyValue agentExecutionInput(AgentExecutionObservationContext context) {
    return KeyValue.of(
        HighCardinalityKeyNames.AGENT_EXECUTION_INPUT,
        context.getCarrier() != null ? AgentUtils.toJson(context.getCarrier()) : KeyValue.NONE_VALUE
    );
  }

  private KeyValue agentExecutionOutput(AgentExecutionObservationContext context) {
    return KeyValue.of(
        HighCardinalityKeyNames.AGENT_EXECUTION_OUTPUT,
        context.getResponse() != null ? AgentUtils.toJson(context.getResponse())
            : KeyValue.NONE_VALUE
    );
  }

}
