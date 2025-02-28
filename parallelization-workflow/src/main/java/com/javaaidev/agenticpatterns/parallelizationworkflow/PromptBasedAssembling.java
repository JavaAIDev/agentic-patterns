package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

/**
 * A {@linkplain ParallelizationWorkflowAgent} which uses an LLM to generate the final result using
 * results from subtasks
 *
 * @param <Request>  Task input type
 * @param <Response> Task output type
 */
public abstract class PromptBasedAssembling<Request, Response> extends
    ParallelizationWorkflowAgent<Request, Response> {

  public PromptBasedAssembling(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  public PromptBasedAssembling(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
  }

  /**
   * Get values for prompt template variables from results of subtasks
   *
   * @param results Subtask execution results
   * @ Values of template variables
   */
  protected abstract @Nullable Map<String, Object> getSubtasksPromptContext(
      TaskExecutionResults results);

  /**
   * Get values for prompt template variables from request
   *
   * @param request Request
   * @return Values of template variables
   */
  protected @Nullable Map<String, Object> getRequestPromptContext(@Nullable Request request) {
    return Map.of();
  }

  @Override
  protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
    return AgentUtils.mergeMap(getRequestPromptContext(request),
        getSubtasksPromptContext(runSubtasks(request)));
  }

}
