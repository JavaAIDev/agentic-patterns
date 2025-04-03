package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.chainworkflow.ChainWorkflowAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

class ArticleImprovementChainAgent extends
    ChainWorkflowAgent<ArticleImprovementRequest, ArticleImprovementResponse> {

  private final ChatClient chatClient;

  protected ArticleImprovementChainAgent(
      ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(ArticleImprovementResponse.class, observationRegistry);
    this.chatClient = chatClient;
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
