package com.javaaidev.agenticpatterns.chainworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.Ordered;

public abstract class ChainStepAgent<Req, Res> extends TaskExecutionAgent<Req, Res> implements
    Ordered {

  protected ChainStepAgent(ChatClient chatClient) {
    super(chatClient);
  }

  protected ChainStepAgent(ChatClient chatClient, @Nullable Type responseType) {
    super(chatClient, responseType);
  }

  protected abstract Res call(Req request, Map<String, Object> context,
      WorkflowChain<Req, Res> chain);
}
