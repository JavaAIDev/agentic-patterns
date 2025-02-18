package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.AlgorithmArticleGenerationAgent.AlgorithmArticleGenerationRequest;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.AlgorithmArticleGenerationAgent.AlgorithmArticleGenerationResponse;
import com.javaaidev.agenticpatterns.parallelizationworkflow.ParallelizationWorkflowAgent;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class AlgorithmArticleGenerationAgent extends
    ParallelizationWorkflowAgent.PromptBasedAssembling<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse> {

  private final ChatClient chatClient;

  public AlgorithmArticleGenerationAgent(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Override
  protected String getPromptTemplate() {
    return """
        Goal: Write an article about {algorithm}.
        
        Requirements:
        - Start with a brief introduction.
        - Include sample code written in different languages.
        - Output the article in markdown.
        
        {sample_code}
        """;
  }

  @Override
  protected ChatClient getChatClient() {
    return this.chatClient;
  }

  @Override
  protected @Nullable Map<String, Object> getAssemblingPromptContext(
      Map<String, PartialResult> results) {
    return Map.of();
  }

  public record AlgorithmArticleGenerationRequest(String algorithm) {

  }

  public record AlgorithmArticleGenerationResponse(String article) {

  }
}
