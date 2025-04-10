package com.javaaidev.agenticpatterns.core.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum WorkflowExecutionObservationDocumentation implements ObservationDocumentation {
  WORKFLOW_EXECUTION {
    @Override
    public Class<? extends ObservationConvention<? extends Context>> getDefaultConvention() {
      return DefaultWorkflowExecutionObservationConvention.class;
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
    WORKFLOW_NAME {
      @Override
      public String asString() {
        return "workflow.name";
      }
    }
  }

  public enum HighCardinalityKeyNames implements KeyName {
    WORKFLOW_EXECUTION_INPUT {
      @Override
      public String asString() {
        return "workflow.execution.input";
      }
    },

    WORKFLOW_EXECUTION_OUTPUT {
      @Override
      public String asString() {
        return "workflow.execution.output";
      }
    }
  }
}
