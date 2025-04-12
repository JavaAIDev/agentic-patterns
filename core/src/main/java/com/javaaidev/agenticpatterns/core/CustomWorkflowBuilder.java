package com.javaaidev.agenticpatterns.core;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

/**
 * Build a custom {@linkplain AgenticWorkflow}
 *
 * @param <Request>  Type of request
 * @param <Response> Type of response
 */
public class CustomWorkflowBuilder<Request, Response> extends
    AbstractAgenticWorkflowBuilder<Request, Response, CustomWorkflowBuilder<Request, Response>> {

  protected Function<Request, Response> action;

  /**
   * Set the action of this workflow
   *
   * @param action Action to run
   * @return Current builder
   */
  public CustomWorkflowBuilder<Request, Response> action(Function<Request, Response> action) {
    this.action = Objects.requireNonNull(action, "Action cannot be null");
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
