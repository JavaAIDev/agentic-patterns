package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationResponse;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class SampleCodeGenerationAgent extends
    TaskExecutionAgent<SampleCodeGenerationRequest, SampleCodeGenerationResponse> {

  private final ChatClient chatClient;

  public SampleCodeGenerationAgent(ChatClient chatClient) {
    super(chatClient);
    this.chatClient = chatClient;
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
    var language = Optional.ofNullable(request)
        .map(SampleCodeGenerationRequest::language).orElse("java");
    var description = Optional.ofNullable(request)
        .map(SampleCodeGenerationRequest::description).orElse("");
    return Map.of(
        "language", language,
        "description", description
    );
  }

  public record SampleCodeGenerationRequest(String language, String description) {

  }

  public record SampleCodeGenerationResponse(String code) {

  }


}
