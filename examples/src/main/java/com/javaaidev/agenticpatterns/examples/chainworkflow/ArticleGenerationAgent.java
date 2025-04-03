package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingRequest;
import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingResponse;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

class ArticleGenerationAgent extends
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
}
