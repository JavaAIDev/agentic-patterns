package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.SampleCodeGenerationAgent.SampleCodeGenerationResponse;
import com.javaaidev.agenticpatterns.parallelizationworkflow.PromptBasedAssembling;
import com.javaaidev.agenticpatterns.parallelizationworkflow.SubtaskCreationRequest;
import com.javaaidev.agenticpatterns.parallelizationworkflow.TaskExecutionResults;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.CollectionUtils;

public class AlgorithmArticleGenerationAgent extends
    PromptBasedAssembling<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse> {

  public AlgorithmArticleGenerationAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, AlgorithmArticleGenerationResponse.class, observationRegistry);
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
  protected @Nullable Map<String, Object> getRequestPromptContext(
      @Nullable AlgorithmArticleGenerationRequest request) {
    return Map.of("algorithm",
        AgentUtils.safeGet(request, AlgorithmArticleGenerationRequest::algorithm, ""));
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
  protected List<SubtaskCreationRequest<AlgorithmArticleGenerationRequest>> createTasks(
      @Nullable AlgorithmArticleGenerationRequest request) {
    var languages = AgentUtils.safeGet(request,
        AlgorithmArticleGenerationRequest::languages, List.<String>of());
    if (CollectionUtils.isEmpty(languages)) {
      return List.of();
    }
    var codeGenerationAgent = new SampleCodeGenerationAgent(chatClient, observationRegistry);
    return languages.stream().map(
        language -> new SubtaskCreationRequest<>(language,
            codeGenerationAgent,
            (AlgorithmArticleGenerationRequest req) -> new SampleCodeGenerationRequest(language,
                "Implement algorithm " + req.algorithm()))).toList();

  }

}
