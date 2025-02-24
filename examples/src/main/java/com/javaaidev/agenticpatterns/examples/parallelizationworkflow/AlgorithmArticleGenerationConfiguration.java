package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgorithmArticleGenerationConfiguration {

  @Bean
  public AlgorithmArticleGenerationAgent algorithmArticleGenerationAgent(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry) {
    return new AlgorithmArticleGenerationAgent(
        chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build(),
        observationRegistry);
  }
}
