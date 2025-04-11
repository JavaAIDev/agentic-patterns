package com.javaaidev.agenticpatterns.taskexecution;

import org.springframework.util.Assert;

public class DefaultTaskExecutionAgentBuilder<Request, Response> extends
    AbstractTaskExecutionAgentBuilder<Request, Response, DefaultTaskExecutionAgentBuilder<Request, Response>> {

  public TaskExecutionAgent<Request, Response> build() {
    Assert.notNull(chatClient, "ChatClient cannot be null");
    Assert.hasText(promptTemplate, "Prompt template cannot be empty");
    return new TaskExecutionAgent<>(
        chatClient,
        promptTemplate,
        responseType,
        promptTemplateContextProvider,
        chatClientRequestSpecUpdater,
        name,
        observationRegistry) {
    };
  }
}
