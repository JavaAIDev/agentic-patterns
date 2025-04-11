package com.javaaidev.agenticpatterns.core;

import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

public class SingleStepWorkflowBuilder<Request, Response> extends
    AbstractAgenticWorkflowBuilder<Request, Response, SingleStepWorkflowBuilder<Request, Response>> {

  protected Function<Request, Response> action;

  public SingleStepWorkflowBuilder<Request, Response> action(Function<Request, Response> action) {
    Assert.notNull(action, "Action cannot be null");
    this.action = action;
    return this;
  }

  @Override
  public AgenticWorkflow<Request, Response> build() {
    Assert.notNull(action, "Action cannot be null");
    return new AbstractAgenticWorkflow<>(name, observationRegistry) {
      @Override
      protected Response doExecute(@Nullable Request request) {
        return action.apply(request);
      }
    };
  }
}
