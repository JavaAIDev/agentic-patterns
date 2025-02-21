package com.javaaidev.agenticpatterns.examples.taskexecution;

import com.javaaidev.agenticpatterns.core.Utils;
import com.javaaidev.agenticpatterns.examples.taskexecution.UserGenerationAgent.UserGenerationRequest;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class UserGenerationAgent extends TaskExecutionAgent<UserGenerationRequest, List<User>> {

  public UserGenerationAgent(ChatClient chatClient) {
    super(chatClient);
  }

  @Override
  protected String getPromptTemplate() {
    return Utils.loadPromptTemplateFromClasspath("prompt_template/generate-user.st");
  }

  @Override
  protected @Nullable Map<String, Object> getPromptContext(
      @Nullable UserGenerationRequest userGenerationRequest) {
    var count = Utils.safeGet(userGenerationRequest, UserGenerationRequest::count,
        1);
    return Map.of(
        "count", Math.max(1, count)
    );
  }

  public record UserGenerationRequest(int count) {

  }
}
