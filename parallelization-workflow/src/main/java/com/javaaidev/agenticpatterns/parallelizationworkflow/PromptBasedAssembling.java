package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

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

  protected abstract @Nullable Map<String, Object> getSubtasksPromptContext(
      TaskExecutionResults results);

  protected @Nullable Map<String, Object> getParentPromptContext(@Nullable Request request) {
    return Map.of();
  }

  @Override
  protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
    return AgentUtils.mergeMap(getParentPromptContext(request),
        getSubtasksPromptContext(runSubtasks(request)));
  }

}
