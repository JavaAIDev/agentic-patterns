package com.javaaidev.agenticpatterns.examples.agentastool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestAPITestCaseGenerationConfiguration {

  @Bean
  public RestAPITestCaseGenerationAgent restAPITestCaseGenerationAgent(
      ChatClient.Builder chatClientBuilder) {
    return new RestAPITestCaseGenerationAgent(chatClientBuilder.build());
  }
}
