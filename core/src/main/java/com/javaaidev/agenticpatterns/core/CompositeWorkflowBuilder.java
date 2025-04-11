package com.javaaidev.agenticpatterns.core;

import java.util.ArrayDeque;
import java.util.Deque;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

public class CompositeWorkflowBuilder<Request, Response> extends
    AbstractAgenticWorkflowBuilder<Request, Response, CompositeWorkflowBuilder<Request, Response>> {

  private final Deque<AgenticWorkflow> deque = new ArrayDeque<>();

  public CompositeWorkflowBuilder<Request, Response> addNext(AgenticWorkflow workflow) {
    Assert.notNull(workflow, "Workflow cannot be null");
    deque.addLast(workflow);
    return this;
  }

  @Override
  public AgenticWorkflow<Request, Response> build() {
    Assert.notEmpty(deque, "At least one workflow is required");
    return new AbstractAgenticWorkflow<>(name, observationRegistry) {
      @Override
      @SuppressWarnings("unchecked")
      protected Response doExecute(@Nullable Request request) {
        Object result = request;
        do {
          var workflow = deque.pollFirst();
          result = workflow.execute(result);
        } while (!deque.isEmpty());
        return (Response) result;
      }
    };
  }
}
