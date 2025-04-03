package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingRequest;
import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingResponse;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class ArticleWritingAgent extends
    TaskExecutionAgent<ArticleWritingRequest, ArticleWritingResponse> {

  private final ArticleGenerationAgent articleGenerationAgent;
  private final ArticleImprovementChainAgent articleImprovementChainAgent;

  protected ArticleWritingAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, ArticleWritingResponse.class, observationRegistry);
    articleGenerationAgent = new ArticleGenerationAgent(chatClient, observationRegistry);
    articleImprovementChainAgent = new ArticleImprovementChainAgent(chatClient,
        observationRegistry);
  }

  @Override
  protected String getPromptTemplate() {
    return "";
  }

  @Override
  public ArticleWritingResponse call(@Nullable ArticleWritingRequest articleWritingRequest) {
    var initialArticle = articleGenerationAgent.call(articleWritingRequest);
    var improved = articleImprovementChainAgent.call(
        new ArticleImprovementRequest(initialArticle.article()));
    return new ArticleWritingResponse(improved.article());
  }

  public record ArticleWritingRequest(String topic) {

  }

  public record ArticleWritingResponse(String article) {

  }


}
