package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationResponse;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class SampleCodeGenerationAgent extends
    TaskExecutionAgent<SampleCodeGenerationRequest, SampleCodeGenerationResponse> {

  public SampleCodeGenerationAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  @Override
  protected String getPromptTemplate() {
    return """
        Write {language} code to meet the requirement.
        {description}
        """;
  }

  @Override
  protected @Nullable Map<String, Object> getPromptContext(
      @Nullable SampleCodeGenerationRequest request) {
    return AgentUtils.objectToMap(request);
  }

  public record SampleCodeGenerationRequest(String language, String description) {

  }

  public record SampleCodeGenerationResponse(String code) {

  }


}
