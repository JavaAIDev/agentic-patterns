package com.javaaidev.agenticpatterns.core;

import com.javaaidev.agenticpatterns.core.observation.DefaultWorkflowExecutionObservationConvention;
import com.javaaidev.agenticpatterns.core.observation.WorkflowExecutionObservationContext;
import com.javaaidev.agenticpatterns.core.observation.WorkflowExecutionObservationDocumentation;
import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;

public abstract class AbstractAgenticWorkflow<Request, Response> implements
    AgenticWorkflow<Request, Response> {

  @Nullable
  protected String workflowName;
  @Nullable
  protected ObservationRegistry observationRegistry;

  protected AbstractAgenticWorkflow(@Nullable String workflowName,
      @Nullable ObservationRegistry observationRegistry) {
    this.workflowName = workflowName;
    this.observationRegistry = observationRegistry;
  }

  @Override
  public String getName() {
    return this.workflowName != null ? this.workflowName : this.getClass().getSimpleName();
  }

  @Override
  public Response execute(@Nullable Request request) {
    if (observationRegistry == null || observationRegistry.isNoop()) {
      return doExecute(request);
    }
    var observationContext = new WorkflowExecutionObservationContext(getName(), request);
    var observation =
        WorkflowExecutionObservationDocumentation.WORKFLOW_EXECUTION.observation(
            null,
            new DefaultWorkflowExecutionObservationConvention(),
            () -> observationContext,
            observationRegistry
        ).start();
    try (var ignored = observation.openScope()) {
      var response = doExecute(request);
      observationContext.setResponse(response);
      return response;
    } catch (Exception e) {
      observation.error(e);
      throw new WorkflowExecutionException("Error in workflow execution", e);
    } finally {
      observation.stop();
    }
  }

  protected abstract Response doExecute(@Nullable Request request);
}
