package com.javaaidev.agenticpatterns.chainworkflow;

import com.javaaidev.agenticpatterns.taskexecution.NoLLMTaskExecutionAgent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class ChainWorkflowAgent<Request, Response> extends
    NoLLMTaskExecutionAgent<Request, Response> {

  private final List<ChainStepAgent<Request, Response>> stepAgents = new ArrayList<>();

  protected ChainWorkflowAgent(ChatClient chatClient) {
    super(chatClient);
  }

  public ChainWorkflowAgent(ChatClient chatClient,
      @Nullable Type responseType) {
    super(chatClient, responseType);
  }

  public void addStep(ChainStepAgent<Request, Response> stepAgent) {
    stepAgents.add(stepAgent);
  }

  @Override
  public Response call(@Nullable Request request) {
    var chain = new WorkflowChain<>(stepAgents);
    return chain.callNext(request, null);
  }

}
