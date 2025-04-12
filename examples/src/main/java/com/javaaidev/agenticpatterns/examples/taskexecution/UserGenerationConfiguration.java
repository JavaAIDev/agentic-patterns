package com.javaaidev.agenticpatterns.examples.taskexecution;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Configuration
public class UserGenerationConfiguration {

  @Bean
  @Description("Generate test users")
  @Qualifier("userGenerationAgent")
  public TaskExecutionAgent<UserGenerationRequest, UserGenerationResponse> userGenerationAgent(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry
  ) {
    var chatClient = chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build();
    return TaskExecutionAgent.<UserGenerationRequest, UserGenerationResponse>defaultBuilder()
        .chatClient(chatClient)
        .responseType(UserGenerationResponse.class)
        .promptTemplate(
            AgentUtils.loadPromptTemplateFromClasspath("prompt_template/generate-user.st"))
        .name("UserGeneration")
        .observationRegistry(observationRegistry)
        .build();
  }
}
