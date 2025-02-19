package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.AlgorithmArticleGenerationAgent.AlgorithmArticleGenerationRequest;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.AlgorithmArticleGenerationAgent.AlgorithmArticleGenerationResponse;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationResponse;
import com.javaaidev.agenticpatterns.parallelizationworkflow.ParallelizationWorkflowAgent;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.CollectionUtils;

public class AlgorithmArticleGenerationAgent extends
    ParallelizationWorkflowAgent.PromptBasedAssembling<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse> {

  private final ChatClient chatClient;

  public AlgorithmArticleGenerationAgent(ChatClient chatClient) {
    super(AlgorithmArticleGenerationResponse.class);
    this.chatClient = chatClient;
  }

  @Override
  protected String getPromptTemplate() {
    return """
        Goal: Write an article about {algorithm}.
        
        Requirements:
        - Start with a brief introduction.
        - Include only sample code listed below.
        - Output the article in markdown.
        
        {sample_code}
        """;
  }

  @Override
  protected ChatClient getChatClient() {
    return this.chatClient;
  }

  @Override
  protected @Nullable Map<String, Object> getParentPromptContext(
      @Nullable AlgorithmArticleGenerationRequest request) {
    var algorithm = Optional.ofNullable(request).map(AlgorithmArticleGenerationRequest::algorithm)
        .orElse("");
    return Map.of("algorithm", algorithm);
  }

  @Override
  protected @Nullable Map<String, Object> getSubtasksPromptContext(
      TaskExecutionResults results) {
    var sampleCode = results.allSuccessfulResults().entrySet().stream().map(entry -> """
            Language: %s
            Code:
            %s
            """.formatted(entry.getKey(), ((SampleCodeGenerationResponse) entry.getValue()).code()))
        .collect(Collectors.joining("==========\n", "\n----------\n", "=========="));
    return Map.of("sample_code", sampleCode);
  }

  @Override
  protected @Nullable List<SubtaskCreationRequest<AlgorithmArticleGenerationRequest>> createTasks(
      @Nullable AlgorithmArticleGenerationRequest request) {
    var languages = Optional.ofNullable(request).map(AlgorithmArticleGenerationRequest::languages)
        .orElse(List.of());
    if (CollectionUtils.isEmpty(languages)) {
      return List.of();
    }
    var codeGenerationAgent = new SampleCodeGenerationAgent(getChatClient());
    return languages.stream().map(
        language -> new SubtaskCreationRequest<>(language,
            codeGenerationAgent,
            (AlgorithmArticleGenerationRequest req) -> new SampleCodeGenerationRequest(language,
                "Implement algorithm " + req.algorithm()))).toList();

  }

  public record AlgorithmArticleGenerationRequest(String algorithm, List<String> languages) {

  }

  public record AlgorithmArticleGenerationResponse(String article) {

  }
}
