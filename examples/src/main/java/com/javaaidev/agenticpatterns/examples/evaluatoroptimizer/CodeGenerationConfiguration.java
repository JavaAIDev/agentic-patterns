package com.javaaidev.agenticpatterns.examples.evaluatoroptimizer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeGenerationConfiguration {

  @Bean
  public CodeGenerationAgent codeGenerationAgent(ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor) {
    return new CodeGenerationAgent(chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build());
  }
}
