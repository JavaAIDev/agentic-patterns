package com.javaaidev.agenticpatterns.examples.taskexecution;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Configuration
public class UserGenerationConfiguration {

  @Bean
  @Description("Generate test user")
  public UserGenerationAgent userGenerationAgent(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry) {
    return new UserGenerationAgent(
        chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build(), observationRegistry);
  }
}
