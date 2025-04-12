package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.chainworkflow.ChainStepAgent;
import com.javaaidev.agenticpatterns.chainworkflow.ChainWorkflow;
import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.core.AgenticWorkflow;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArticleWritingConfiguration {

  @Bean
  @Qualifier("articleWritingWorkflow")
  public AgenticWorkflow<ArticleWritingRequest, ArticleWritingResponse> articleWritingWorkflow(
      ObservationRegistry observationRegistry,
      @Qualifier("articleGenerationAgent") TaskExecutionAgent<ArticleWritingRequest, ArticleWritingResponse> articleGenerationAgent,
      @Qualifier("articleImprovementWorkflow") ChainWorkflow<ArticleImprovementRequest, ArticleImprovementResponse> articleImprovementWorkflow
  ) {
    return AgenticWorkflow.<ArticleWritingRequest, ArticleWritingResponse>custom()
        .name("ArticleWritingWorkflow")
        .observationRegistry(observationRegistry)
        .action(request -> {
          var initialArticle = articleGenerationAgent.call(request);
          var improved = articleImprovementWorkflow.execute(
              new ArticleImprovementRequest(initialArticle.article()));
          return new ArticleWritingResponse(improved.article());
        })
        .build();
  }

  @Bean
  @Qualifier("articleGenerationAgent")
  public TaskExecutionAgent<ArticleWritingRequest, ArticleWritingResponse> articleGenerationAgent(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry) {
    var chatClient = chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build();
    return TaskExecutionAgent.<ArticleWritingRequest, ArticleWritingResponse>defaultBuilder()
        .name("ArticleGeneration")
        .chatClient(chatClient)
        .responseType(ArticleWritingResponse.class)
        .observationRegistry(observationRegistry)
        .promptTemplate("Write an article about {topic}")
        .build();
  }

  @Bean
  @Qualifier("articleImprovementWorkflow")
  public ChainWorkflow<ArticleImprovementRequest, ArticleImprovementResponse> articleImprovementWorkflow(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry) {
    var chatClient = chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build();
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

    var builder = ChainWorkflow.<ArticleImprovementRequest, ArticleImprovementResponse>builder();
    for (int i = 0; i < instructions.size(); i++) {
      var instruction = instructions.get(i);
      builder.addStepAgent(
          ChainStepAgent.<ArticleImprovementRequest, ArticleImprovementResponse>builder()
              .name("ArticleImprovement#" + i)
              .chatClient(chatClient)
              .responseType(ArticleImprovementResponse.class)
              .nextRequestPreparer(response -> new ArticleImprovementRequest(response.article()))
              .promptTemplate("""
                  Goal: Improve an article by following the instruction:
                  
                  {instruction}
                  
                  Article content:
                  {article}
                  """)
              .promptTemplateContextProvider(request -> Map.of(
                  "instruction", instruction,
                  "article",
                  AgentUtils.safeGet(request, ArticleImprovementRequest::article, "")
              ))
              .order(100 + i)
              .observationRegistry(observationRegistry)
              .build());
    }
    return builder
        .name("ArticleWritingWorkflow")
        .observationRegistry(observationRegistry)
        .build();
  }
}
