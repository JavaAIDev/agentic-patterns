package com.javaaidev.agenticpatterns.chainworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class ChainWorkflowAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  private final List<ChainStepAgent<Request, Response>> stepAgents = new ArrayList<>();

  protected ChainWorkflowAgent(
      ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, null, observationRegistry);
  }

  public ChainWorkflowAgent(
      ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
  }

  public void addStep(ChainStepAgent<Request, Response> stepAgent) {
    stepAgents.add(stepAgent);
  }

  @Override
  public Response call(@Nullable Request request) {
    return instrumentedCall(request, this::doCall);
  }

  private Response doCall(@Nullable Request request) {
    var chain = new WorkflowChain<>(stepAgents);
    return chain.callNext(request, null);
  }

  @Override
  protected String getPromptTemplate() {
    return "";
  }
}
