package com.javaaidev.agenticpatterns.routingworkflow;

import com.javaaidev.agenticpatterns.taskexecution.AbstractTaskExecutionAgentBuilder;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

/**
 * A {@linkplain RoutingSelector} implemented using {@linkplain TaskExecutionAgent}
 *
 * @param <Request>
 * @param <Response>
 */
public class DefaultRoutingSelector<Request, Response> extends
    TaskExecutionAgent<RoutingRequest<Request, Response>, RoutingResponse> implements
    RoutingSelector<Request, Response> {

  private final Function<Request, String> defaultRoutingInputFormatter = request -> Objects.toString(
      request, "");
  private Function<Request, String> routingInputFormatter = defaultRoutingInputFormatter;

  public DefaultRoutingSelector(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, RoutingResponse.class, observationRegistry);
  }

  public DefaultRoutingSelector(ChatClient chatClient,
      @Nullable Function<Request, String> routingInputFormatter,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, RoutingResponse.class, observationRegistry);
    this.routingInputFormatter = Objects.requireNonNullElse(
        routingInputFormatter,
        defaultRoutingInputFormatter);
  }

  @Override
  public RoutingResponse select(RoutingRequest<Request, Response> request) {
    return this.call(request);
  }

  @Override
  protected String getPromptTemplate() {
    return """
        Goal: Select the best target to handle the input from a list of choices
        
        Choices:
        {choices}
        
        Input:
        {input}
        """;
  }

  @Override
  protected @Nullable Map<String, Object> getPromptContext(
      @Nullable RoutingRequest<Request, Response> routingRequest) {
    Assert.notNull(routingRequest, "routing request cannot be null");
    var choices = routingRequest.choices().stream().map(choice ->
            "- %s: %s".formatted(choice.name(), choice.description()))
        .collect(Collectors.joining("\n"));
    return Map.of(
        "choices", choices,
        "input", routingInputFormatter.apply(routingRequest.request())
    );
  }

  public static <Request, Response> Builder<Request, Response> builder() {
    return new Builder<>();
  }

  public static class Builder<Request, Response> extends
      AbstractTaskExecutionAgentBuilder<RoutingRequest<Request, Response>, RoutingResponse, Builder<Request, Response>> {

    private Function<Request, String> routingInputFormatter;

    public Builder<Request, Response> routingInputFormatter(
        Function<Request, String> routingInputFormatter) {
      this.routingInputFormatter = routingInputFormatter;
      return this;
    }

    @Override
    public DefaultRoutingSelector<Request, Response> build() {
      return new DefaultRoutingSelector<>(
          chatClient,
          routingInputFormatter,
          observationRegistry);
    }
  }
}
