package com.javaaidev.agenticpatterns.taskexecution;

import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.util.Assert;

public class TaskExecutionAgentBuilder<Request, Response> {

  private ChatClient chatClient;
  private String promptTemplate = "";
  private String agentName = "<unnamed agent>";
  private @Nullable Type responseType;
  private @Nullable ObservationRegistry observationRegistry;
  private @Nullable Function<Request, Map<String, Object>> promptTemplateContextProvider;
  private @Nullable Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater;

  public TaskExecutionAgentBuilder<Request, Response> chatClient(ChatClient chatClient) {
    Assert.notNull(chatClient, "ChatClient cannot be null");
    this.chatClient = chatClient;
    return this;
  }

  public TaskExecutionAgentBuilder<Request, Response> promptTemplate(String promptTemplate) {
    Assert.hasText(promptTemplate, "Prompt template cannot be empty");
    this.promptTemplate = promptTemplate;
    return this;
  }

  public TaskExecutionAgentBuilder<Request, Response> agentName(String agentName) {
    Assert.hasText(agentName, "Agent name cannot be empty");
    this.agentName = agentName;
    return this;
  }

  public TaskExecutionAgentBuilder<Request, Response> responseType(Type responseType) {
    this.responseType = responseType;
    return this;
  }

  public TaskExecutionAgentBuilder<Request, Response> observationRegistry(
      ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
    return this;
  }

  public TaskExecutionAgentBuilder<Request, Response> promptTemplateContextProvider(
      Function<Request, Map<String, Object>> promptTemplateContextProvider) {
    this.promptTemplateContextProvider = promptTemplateContextProvider;
    return this;
  }

  public TaskExecutionAgentBuilder<Request, Response> chatClientRequestSpecUpdater(
      Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater) {
    this.chatClientRequestSpecUpdater = chatClientRequestSpecUpdater;
    return this;
  }

  public TaskExecutionAgent<Request, Response> build() {
    Assert.notNull(chatClient, "ChatClient cannot be null");
    Assert.hasText(promptTemplate, "Prompt template cannot be empty");
    return new TaskExecutionAgent<>(chatClient, responseType, observationRegistry) {
      @Override
      protected String getName() {
        return agentName;
      }

      @Override
      protected String getPromptTemplate() {
        return promptTemplate;
      }

      @Override
      protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
        if (promptTemplateContextProvider != null) {
          return promptTemplateContextProvider.apply(request);
        }
        return super.getPromptContext(request);
      }

      @Override
      protected void updateChatClientRequest(ChatClientRequestSpec spec) {
        if (chatClientRequestSpecUpdater != null) {
          chatClientRequestSpecUpdater.accept(spec);
        }
      }
    };
  }
}
