package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.chainworkflow.ChainStepAgent;
import com.javaaidev.agenticpatterns.chainworkflow.ChainWorkflowAgent;
import com.javaaidev.agenticpatterns.chainworkflow.WorkflowChain;
import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingRequest;
import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingResponse;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import java.util.Map;
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

  private record ArticleImprovementRequest(String article) {

  }

  private record ArticleImprovementResponse(String article) {

  }

  private static class ArticleGenerationAgent extends
      TaskExecutionAgent<ArticleWritingRequest, ArticleWritingResponse> {

    protected ArticleGenerationAgent(ChatClient chatClient,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, ArticleWritingResponse.class, observationRegistry);
    }

    @Override
    protected String getPromptTemplate() {
      return """
          Write an article about {topic}
          """;
    }

    @Override
    protected Map<String, Object> getPromptContext(
        @Nullable ArticleWritingRequest request) {
      return Map.of(
          "topic", AgentUtils.safeGet(request, ArticleWritingRequest::topic, "")
      );
    }
  }

  private static class ArticleImprovementChainAgent extends
      ChainWorkflowAgent<ArticleImprovementRequest, ArticleImprovementResponse> {

    protected ArticleImprovementChainAgent(
        ChatClient chatClient,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, ArticleImprovementResponse.class, observationRegistry);
      initStepAgents();
    }

    private void initStepAgents() {
      var instructions = List.of(
          """
              Review the Structure
              - Ensure the article has a clear introduction, body, and conclusion.
              - Check if ideas flow logically from one section to another.
              - Ensure paragraphs are well-organized and each one has a clear purpose.
              """,
          """
              Improve Clarity and Conciseness
              - Remove unnecessary words and redundant phrases.
              - Simplify complex sentences for better readability.
              - Use active voice where possible.
              """,
          """
              Enhance Readability
              - Break long paragraphs into shorter ones.
              - Use bullet points or subheadings for easier scanning.
              - Vary sentence length to maintain reader interest.
              """
      );
      for (int i = 0; i < instructions.size(); i++) {
        addStep(
            new ArticleImprovementAgent(chatClient, observationRegistry, instructions.get(i), i));
      }
    }
  }


  private static class ArticleImprovementAgent extends
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
}
