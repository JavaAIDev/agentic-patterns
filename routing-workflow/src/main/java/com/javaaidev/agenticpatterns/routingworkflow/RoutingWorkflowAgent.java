package com.javaaidev.agenticpatterns.routingworkflow;

import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

public abstract class RoutingWorkflowAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  private final RoutingAgent routingAgent;
  private final CopyOnWriteArrayList<RoutingChoice<Request, Response>> routingChoices = new CopyOnWriteArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(RoutingWorkflowAgent.class);

  protected RoutingWorkflowAgent(ChatClient chatClient,
      @Nullable Type responseType) {
    super(chatClient, responseType);
    routingAgent = new RoutingAgent(chatClient);
  }

  protected RoutingWorkflowAgent(ChatClient chatClient) {
    super(chatClient);
    routingAgent = new RoutingAgent(chatClient);
  }

  public record RoutingChoice<Request, Response>(String name, String description,
                                                 TaskExecutionAgent<Request, Response> agent) {

  }

  public record RoutingRequest<Request, Response>(Request request,
                                                  List<RoutingChoice<Request, Response>> choices) {

  }

  public record RoutingResponse(String name, String reason) {

  }

  protected void addRoutingChoice(RoutingChoice<Request, Response> routingChoice) {
    routingChoices.add(routingChoice);
  }

  @Override
  protected String getPromptTemplate() {
    return "";
  }

  protected String getRoutingPromptTemplate() {
    return """
        Goal: Select the best target to handle the input
        
        Choices:
        {choices}
        
        Input:
        {input}
        """;
  }

  protected @Nullable Map<String, Object> getRoutingPromptContext(
      @Nullable RoutingRequest<Request, Response> routingRequest) {
    Assert.notNull(routingRequest, "routing request cannot be null");
    var choices = routingChoices.stream().map(choice ->
            "- %s: %s\n".formatted(choice.name(), choice.description()))
        .collect(Collectors.joining("\n\n"));
    return Map.of(
        "choices", choices,
        "input", Objects.toString(routingRequest.request(), "")
    );
  }

  @Override
  public Response call(@Nullable Request request) {
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

  protected class RoutingAgent extends TaskExecutionAgent<RoutingRequest, RoutingResponse> {

    protected RoutingAgent(ChatClient chatClient) {
      super(chatClient);
    }

    @Override
    protected String getPromptTemplate() {
      return getRoutingPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(
        @Nullable RoutingRequest routingRequest) {
      return getRoutingPromptContext(routingRequest);
    }
  }

}
