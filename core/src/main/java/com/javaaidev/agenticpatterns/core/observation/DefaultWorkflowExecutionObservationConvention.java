package com.javaaidev.agenticpatterns.core.observation;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.core.observation.WorkflowExecutionObservationDocumentation.HighCardinalityKeyNames;
import com.javaaidev.agenticpatterns.core.observation.WorkflowExecutionObservationDocumentation.LowCardinalityKeyNames;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

public class DefaultWorkflowExecutionObservationConvention implements
    WorkflowExecutionObservationConvention {

  private String defaultName = "workflow.execute";

  @Override
  public String getName() {
    return defaultName;
  }

  @Override
  public KeyValues getLowCardinalityKeyValues(WorkflowExecutionObservationContext context) {
    return KeyValues.of(workflowName(context));
  }

  @Override
  public KeyValues getHighCardinalityKeyValues(WorkflowExecutionObservationContext context) {
    return KeyValues.of(
        workflowExecutionInput(context),
        workflowExecutionOutput(context)
    );
  }

  private KeyValue workflowName(WorkflowExecutionObservationContext context) {
    return KeyValue.of(
        LowCardinalityKeyNames.WORKFLOW_NAME, context.getWorkflowName()
    );
  }

  private KeyValue workflowExecutionInput(WorkflowExecutionObservationContext context) {
    return KeyValue.of(
        HighCardinalityKeyNames.WORKFLOW_EXECUTION_INPUT,
        context.getCarrier() != null ? AgentUtils.toJson(context.getCarrier()) : KeyValue.NONE_VALUE
    );
  }

  private KeyValue workflowExecutionOutput(WorkflowExecutionObservationContext context) {
    return KeyValue.of(
        HighCardinalityKeyNames.WORKFLOW_EXECUTION_OUTPUT,
        context.getResponse() != null ? AgentUtils.toJson(context.getResponse())
            : KeyValue.NONE_VALUE
    );
  }

}
