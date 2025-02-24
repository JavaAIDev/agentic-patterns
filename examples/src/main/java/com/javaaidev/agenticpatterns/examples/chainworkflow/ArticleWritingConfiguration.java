package com.javaaidev.agenticpatterns.examples.chainworkflow;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArticleWritingConfiguration {

  @Bean
  public ArticleWritingAgent articleWritingAgent(ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry) {
    return new ArticleWritingAgent(chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build(),
        observationRegistry);
  }
}
