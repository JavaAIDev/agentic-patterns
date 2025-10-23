package com.javaaidev.agenticpatterns.routingworkflow;

import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflow;
import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflowBuilder;
import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import io.micrometer.observation.ObservationRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Routing Workflow, refer to <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/routing-workflow">doc</a>
 *
 * @param <Request>  Type of workflow input
 * @param <Response> Type of workflow output
 */
public class RoutingWorkflow<Request, Response> extends
    AbstractAgenticWorkflow<Request, Response> {

  private final List<RoutingChoice<Request, Response>> routingChoices;
  private final RoutingSelector<Request, Response> routingSelector;

  private static final Logger LOGGER = LoggerFactory.getLogger(
      RoutingWorkflow.class);

  public RoutingWorkflow(
      List<RoutingChoice<Request, Response>> routingChoices,
      RoutingSelector<Request, Response> routingSelector,
      @Nullable String name,
      @Nullable ObservationRegistry observationRegistry) {
    super(name, observationRegistry);
    this.routingChoices = routingChoices;
    this.routingSelector = routingSelector;
  }

  @Override
  protected Response doExecute(@Nullable Request request) {
    LOGGER.info("Select the route for request {}", request);
    var routingTarget = routingSelector.select(
        new RoutingRequest<>(request, routingChoices));
    LOGGER.info("Selected routing target: {}", routingTarget);
    var targetAgent = routingChoices.stream()
        .filter(choice -> Objects.equals(routingTarget.name(), choice.name()))
        .findFirst()
        .map(RoutingChoice::agent)
        .orElseThrow(() -> new AgentExecutionException(
            "No matching choice found for routing target: "
                + routingTarget.name()));
    return targetAgent.call(request);
  }

  public static <Request, Response> Builder<Request, Response> builder() {
    return new Builder<>();
  }

  public static class Builder<Request, Response> extends
      AbstractAgenticWorkflowBuilder<Request, Response, Builder<Request, Response>> {

    private final List<RoutingChoice<Request, Response>> routingChoices = new ArrayList<>();
    private RoutingSelector<Request, Response> routingSelector;

    public Builder<Request, Response> routingSelector(
        RoutingSelector<Request, Response> routingSelector) {
      Assert.notNull(routingSelector, "Routing selector cannot be null");
      this.routingSelector = routingSelector;
      return this;
    }

    public Builder<Request, Response> routingSelector(
        DefaultRoutingSelector<Request, Response> routingSelector) {
      Assert.notNull(routingSelector, "Routing selector cannot be null");
      this.routingSelector = routingSelector;
      return this;
    }

    public Builder<Request, Response> addRoutingChoice(
        RoutingChoice<Request, Response> choice) {
      Assert.notNull(choice, "Routing choice cannot be null");
      routingChoices.add(choice);
      return this;
    }

    public Builder<Request, Response> addRoutingChoices(
        List<RoutingChoice<Request, Response>> choices) {
      Assert.notNull(choices, "Routing choices cannot be null");
      routingChoices.addAll(choices);
      return this;
    }

    @Override
    public RoutingWorkflow<Request, Response> build() {
      Assert.notEmpty(routingChoices,
          "At least one routing choice is required");
      return new RoutingWorkflow<>(routingChoices, routingSelector, name,
          observationRegistry);
    }
  }
}
