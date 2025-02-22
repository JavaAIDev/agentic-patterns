package com.javaaidev.agenticpatterns.taskexecution;

import java.lang.reflect.Type;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

/**
 * A task execution agent without using LLM
 *
 * @param <Request>
 * @param <Response>
 */
public abstract class NoLLMTaskExecutionAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  protected NoLLMTaskExecutionAgent(ChatClient chatClient) {
    super(chatClient);
  }

  public NoLLMTaskExecutionAgent(ChatClient chatClient,
      @Nullable Type responseType) {
    super(chatClient, responseType);
  }

  @Override
  protected String getPromptTemplate() {
    return "";
  }
}
