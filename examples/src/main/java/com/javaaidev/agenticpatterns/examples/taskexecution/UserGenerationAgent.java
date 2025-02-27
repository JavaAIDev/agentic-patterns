package com.javaaidev.agenticpatterns.examples.taskexecution;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.examples.taskexecution.UserGenerationAgent.UserGenerationRequest;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import java.util.Map;
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

  @Override
  protected Map<String, Object> getPromptContext(
      @Nullable UserGenerationRequest userGenerationRequest) {
    var count = AgentUtils.safeGet(userGenerationRequest, UserGenerationRequest::count,
        1);
    return Map.of(
        "count", Math.max(1, count)
    );
  }

  public record UserGenerationRequest(int count) {

  }
}
