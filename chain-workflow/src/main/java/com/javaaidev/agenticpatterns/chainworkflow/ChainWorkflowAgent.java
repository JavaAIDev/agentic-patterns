package com.javaaidev.agenticpatterns.chainworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Chain Workflow agent, refer to <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/chain-workflow">doc</a>
 *
 * @param <Request>  Type of agent input
 * @param <Response> Type of agent output
 */
public class ChainWorkflowAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  private final CopyOnWriteArrayList<ChainStepAgent<Request, Response>> stepAgents = new CopyOnWriteArrayList<>();

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

  /**
   * Add a step in the chain
   *
   * @param stepAgent A step in the chain
   */
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
