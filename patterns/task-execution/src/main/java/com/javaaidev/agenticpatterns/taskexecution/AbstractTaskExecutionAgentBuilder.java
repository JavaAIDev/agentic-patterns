package com.javaaidev.agenticpatterns.taskexecution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaaidev.agenticpatterns.core.McpClientConfiguration;
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

public abstract class AbstractTaskExecutionAgentBuilder<Request, Response, T extends AbstractTaskExecutionAgentBuilder<Request, Response, T>> implements
    TaskExecutionAgent.Builder<Request, Response, T> {

  protected ChatClient chatClient;
  protected String promptTemplate = "";
  protected String name = "<unnamed agent>";
  protected @Nullable Type responseType;
  protected @Nullable ObservationRegistry observationRegistry;
  protected @Nullable Function<Request, Map<String, Object>> promptTemplateContextProvider;
  protected @Nullable Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater;
  protected @Nullable McpClientConfiguration mcpClientConfiguration;
  protected @Nullable Predicate<String> toolFilter;
  protected @Nullable ObjectMapper objectMapper;

  @SuppressWarnings("unchecked")
  protected T self() {
    return (T) this;
  }

  public T chatClient(ChatClient chatClient) {
    Assert.notNull(chatClient, "ChatClient cannot be null");
    this.chatClient = chatClient;
    return self();
  }

  public T promptTemplate(String promptTemplate) {
    Assert.hasText(promptTemplate, "Prompt template cannot be empty");
    this.promptTemplate = promptTemplate;
    return self();
  }

  public T name(String name) {
    Assert.hasText(name, "Agent name cannot be empty");
    this.name = name;
    return self();
  }

  public T responseType(Type responseType) {
    this.responseType = responseType;
    return self();
  }

  public T observationRegistry(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
    return self();
  }

  public T promptTemplateContextProvider(
      Function<Request, Map<String, Object>> promptTemplateContextProvider) {
    this.promptTemplateContextProvider = promptTemplateContextProvider;
    return self();
  }

  public T chatClientRequestSpecUpdater(
      Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater) {
    this.chatClientRequestSpecUpdater = chatClientRequestSpecUpdater;
    return self();
  }

  @Override
  public T mcpClientConfiguration(McpClientConfiguration mcpClientConfiguration) {
    this.mcpClientConfiguration = mcpClientConfiguration;
    return self();
  }

  @Override
  public T toolFilter(Predicate<String> toolFilter) {
    this.toolFilter = toolFilter;
    return self();
  }

  @Override
  public T objectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return self();
  }
}
