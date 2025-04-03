package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.chainworkflow.ChainStepAgent;
import com.javaaidev.agenticpatterns.chainworkflow.WorkflowChain;
import com.javaaidev.agenticpatterns.core.AgentUtils;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

class ArticleImprovementAgent extends
    ChainStepAgent<ArticleImprovementRequest, ArticleImprovementResponse> {

  private final String instruction;
  private final int order;

  protected ArticleImprovementAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry, String instruction, int order) {
    super(chatClient, ArticleImprovementResponse.class, observationRegistry);
    this.instruction = instruction;
    this.order = order;
  }

  @Override
  protected String getPromptTemplate() {
    return """
        Goal: Improve an article by following the instruction:
        
        {instruction}
        
        Article content:
        {article}
        """;
  }

  @Override
  protected ArticleImprovementResponse call(ArticleImprovementRequest request,
      Map<String, Object> context,
      WorkflowChain<ArticleImprovementRequest, ArticleImprovementResponse> chain) {
    var response = this.call(request);
    return chain.callNext(new ArticleImprovementRequest(response.article()), response);
  }

  @Override
  protected Map<String, Object> getPromptContext(
      @Nullable ArticleImprovementRequest request) {
    return Map.of(
        "instruction", instruction,
        "article",
        AgentUtils.safeGet(request, ArticleImprovementRequest::article, "")
    );
  }

  @Override
  public int getOrder() {
    return order;
  }

}
