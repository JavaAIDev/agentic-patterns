package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgorithmArticleGenerationConfiguration {

  @Bean
  public AlgorithmArticleGenerationAgent algorithmArticleGenerationAgent(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor) {
    return new AlgorithmArticleGenerationAgent(
        chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build());
  }
}
