package com.javaaidev.agenticpatterns.chainworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.Ordered;

/**
 * A step in the chain
 *
 * @param <Request>  Task input type
 * @param <Response> Task output type
 */
public abstract class ChainStepAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> implements
    Ordered {

  protected ChainStepAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  protected ChainStepAgent(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
  }

  /**
   * Call the current step
   *
   * @param request Task input
   * @param context Shared context between different steps
   * @param chain   The chain, see {@linkplain WorkflowChain}
   * @return Task output
   */
  protected abstract Response call(Request request, Map<String, Object> context,
      WorkflowChain<Request, Response> chain);
}
