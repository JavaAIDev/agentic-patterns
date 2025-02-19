package com.javaaidev.agenticpatterns.examples.taskexecution;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserGenerationConfiguration {

  @Bean
  public UserGenerationAgent userGenerationAgent(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor) {
    return new UserGenerationAgent(
        chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build());
  }
}
