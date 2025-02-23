package com.javaaidev.agenticpatterns.core.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum AgentExecutionObservationDocumentation implements ObservationDocumentation {
  AGENT_EXECUTION {
    @Override
    public Class<? extends ObservationConvention<? extends Context>> getDefaultConvention() {
      return DefaultAgentExecutionObservationConvention.class;
    }

    @Override
    public KeyName[] getLowCardinalityKeyNames() {
      return LowCardinalityKeyNames.values();
    }

    @Override
    public KeyName[] getHighCardinalityKeyNames() {
      return HighCardinalityKeyNames.values();
    }
  };

  public enum LowCardinalityKeyNames implements KeyName {
    AGENT_NAME {
      @Override
      public String asString() {
        return "agent.name";
      }
    }
  }

  public enum HighCardinalityKeyNames implements KeyName {
    AGENT_EXECUTION_INPUT {
      @Override
      public String asString() {
        return "agent.execution.input";
      }
    },

    AGENT_EXECUTION_OUTPUT {
      @Override
      public String asString() {
        return "agent.execution.output";
      }
    }
  }
}
