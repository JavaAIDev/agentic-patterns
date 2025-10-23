package com.javaaidev.agenticpatterns.chainworkflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaaidev.agenticpatterns.core.McpClientConfiguration;
import com.javaaidev.agenticpatterns.taskexecution.AbstractTaskExecutionAgentBuilder;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.util.Assert;

/**
 * A step in the chain implemented as a {@linkplain TaskExecutionAgent}
 *
 * @param <Request>  Task input type
 * @param <Response> Task output type
 */
public abstract class ChainStepAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> implements ChainStep<Request, Response> {

  protected ChainStepAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  protected ChainStepAgent(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
  }

  public ChainStepAgent(ChatClient chatClient,
      String promptTemplate,
      @Nullable Type responseType,
      @Nullable Function<Request, Map<String, Object>> promptTemplateContextProvider,
      @Nullable Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater,
      @Nullable McpClientConfiguration mcpClientConfiguration,
      @Nullable Predicate<String> toolFilter,
      @Nullable String name,
      @Nullable ObservationRegistry observationRegistry,
      @Nullable ObjectMapper objectMapper) {
    super(chatClient, promptTemplate, responseType,
        promptTemplateContextProvider,
        chatClientRequestSpecUpdater, mcpClientConfiguration, toolFilter,
        name, observationRegistry, objectMapper);
  }

  public static <Req, Res> Builder<Req, Res> builder() {
    return new Builder<>();
  }

  public static class Builder<Request, Response> extends
      AbstractTaskExecutionAgentBuilder<Request, Response, Builder<Request, Response>> {

    private Function<Response, Request> nextRequestPreparer;
    private int order;

    public Builder<Request, Response> nextRequestPreparer(
        Function<Response, Request> nextRequestPreparer) {
      this.nextRequestPreparer = nextRequestPreparer;
      return this;
    }

    public Builder<Request, Response> order(int order) {
      this.order = order;
      return this;
    }

    @Override
    public ChainStepAgent<Request, Response> build() {
      Assert.notNull(chatClient, "ChatClient cannot be null");
      Assert.hasText(promptTemplate, "Prompt template cannot be empty");
      Assert.notNull(nextRequestPreparer, "nextRequestPreparer cannot be null");
      return new ChainStepAgent<>(chatClient,
          promptTemplate,
          responseType,
          promptTemplateContextProvider,
          chatClientRequestSpecUpdater,
          mcpClientConfiguration,
          toolFilter,
          name,
          observationRegistry,
          objectMapper) {
        @Override
        public Response call(Request request, Map<String, Object> context,
            WorkflowChain<Request, Response> chain) {
          var response = this.call(request);
          return chain.callNext(nextRequestPreparer.apply(response), response);
        }

        @Override
        public int getOrder() {
          return order;
        }
      };
    }
  }
}
