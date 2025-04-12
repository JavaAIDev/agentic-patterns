package com.javaaidev.agenticpatterns.examples.taskexecution;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class UserGenerationAgent extends
    TaskExecutionAgent<UserGenerationRequest, List<User>> implements
    Function<UserGenerationRequest, List<User>> {

  public UserGenerationAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  @Override
  protected String getPromptTemplate() {
    return AgentUtils.loadPromptTemplateFromClasspath("prompt_template/generate-user.st");
  }

}
