package com.javaaidev.agenticpatterns.chainworkflow;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

/**
 * Chain to manage steps
 *
 * @param <Request>  Task input type
 * @param <Response> Task output type
 */
public class WorkflowChain<Request, Response> {

  private final Deque<ChainStepAgent<Request, Response>> agents;
  private final Map<String, Object> context = new HashMap<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowChain.class);

  public WorkflowChain(List<ChainStepAgent<Request, Response>> agents) {
    this.agents = new ArrayDeque<>(agents.stream().sorted(OrderComparator.INSTANCE).toList());
    LOGGER.info("Added {} agents to the chain", this.agents.size());
  }

  /**
   * Call next step in the chain
   *
   * @param request      Task input
   * @param lastResponse Last task output
   * @return Task output
   */
  public Response callNext(Request request, @Nullable Response lastResponse) {
    if (agents.isEmpty()) {
      return lastResponse;
    }
    var agent = agents.pop();
    return agent.call(request, context, this);
  }
}
