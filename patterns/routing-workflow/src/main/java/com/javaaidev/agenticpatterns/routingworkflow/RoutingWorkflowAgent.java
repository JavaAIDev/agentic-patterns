package com.javaaidev.agenticpatterns.routingworkflow;

import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

/**
 * Routing Workflow agent, refer to <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/routing-workflow">doc</a>
 *
 * @param <Request>  Type of agent input
 * @param <Response> Type of agent output
 */
public abstract class RoutingWorkflowAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  private final RoutingAgent routingAgent;
  private final CopyOnWriteArrayList<RoutingChoice<Request, Response>> routingChoices = new CopyOnWriteArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(RoutingWorkflowAgent.class);

  protected RoutingWorkflowAgent(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
    routingAgent = new RoutingAgent(chatClient);
  }

  protected RoutingWorkflowAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
    routingAgent = new RoutingAgent(chatClient);
  }

  /**
   * Add a new routing choice
   *
   * @param routingChoice Routing choice
   */
  protected void addRoutingChoice(RoutingChoice<Request, Response> routingChoice) {
    routingChoices.add(routingChoice);
  }

  @Override
  protected String getPromptTemplate() {
    return "";
  }

  /**
   * Get the prompt template for routing
   *
   * @return Prompt template
   */
  protected String getRoutingPromptTemplate() {
    return """
        Goal: Select the best target to handle the input from a list of choices
        
        Choices:
        {choices}
        
        Input:
        {input}
        """;
  }

  /**
   * Generate the input used by the default prompt template
   *
   * @param request Request
   * @return Input for routing prompt template
   */
  protected String formatRoutingInput(@Nullable Request request) {
    return Objects.toString(request, "");
  }

  /**
   * Get values of variables used in the default prompt template
   *
   * @param routingRequest Routing request
   * @return Values of variables
   */
  protected @Nullable Map<String, Object> getRoutingPromptContext(
      @Nullable RoutingRequest<Request, Response> routingRequest) {
    Assert.notNull(routingRequest, "routing request cannot be null");
    var choices = routingChoices.stream().map(choice ->
            "- %s: %s".formatted(choice.name(), choice.description()))
        .collect(Collectors.joining("\n"));
    return Map.of(
        "choices", choices,
        "input", formatRoutingInput(routingRequest.request())
    );
  }

  @Override
  public Response call(@Nullable Request request) {
    return instrumentedCall(request, this::doCall);
  }

  public Response doCall(@Nullable Request request) {
    LOGGER.info("Select the route for request {}", request);
    var routingTarget = routingAgent.call(new RoutingRequest<>(request, routingChoices));
    LOGGER.info("Selected routing target: {}", routingTarget);
    var targetAgent = routingChoices.stream()
        .filter(choice -> Objects.equals(routingTarget.name(), choice.name()))
        .findFirst()
        .map(RoutingChoice::agent)
        .orElseThrow(() -> new AgentExecutionException(
            "No matching choice found for routing target: " + routingTarget.name()));
    return targetAgent.call(request);
  }

  /**
   * Agent for the routing
   */
  protected class RoutingAgent extends
      TaskExecutionAgent<RoutingRequest<Request, Response>, RoutingResponse> {

    protected RoutingAgent(ChatClient chatClient) {
      super(chatClient);
    }

    @Override
    protected String getPromptTemplate() {
      return getRoutingPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(
        @Nullable RoutingRequest<Request, Response> routingRequest) {
      return getRoutingPromptContext(routingRequest);
    }
  }

}
