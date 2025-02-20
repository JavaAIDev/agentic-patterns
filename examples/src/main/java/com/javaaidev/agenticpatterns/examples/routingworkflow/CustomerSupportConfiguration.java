package com.javaaidev.agenticpatterns.examples.routingworkflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerSupportConfiguration {

  @Bean
  public CustomerSupportAgent customerSupportAgent(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor) {
    return new CustomerSupportAgent(
        chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build());
  }
}
