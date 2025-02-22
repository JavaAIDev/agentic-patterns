package com.javaaidev.agenticpatterns.examples.chainworkflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArticleWritingConfiguration {

  @Bean
  public ArticleWritingAgent articleWritingAgent(ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor) {
    return new ArticleWritingAgent(chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build());
  }
}
