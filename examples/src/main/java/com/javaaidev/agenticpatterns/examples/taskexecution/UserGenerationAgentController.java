package com.javaaidev.agenticpatterns.examples.taskexecution;

import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import com.javaaidev.agenticpatterns.examples.taskexecution.UserGenerationAgentController.UserGenerationRequest;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserGenerationAgentController extends
    TaskExecutionAgent<UserGenerationRequest, List<User>> {

  private final ChatClient chatClient;

  public UserGenerationAgentController(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @PostMapping
  public List<User> generateUsers(@RequestBody UserGenerationRequest request) {
    return this.call(request);
  }

  @Override
  protected String getPromptTemplate() {
    try {
      return new ClassPathResource("prompt_template/generate-user.st").getContentAsString(
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new AgentExecutionException("Template not found", e);
    }
  }

  @Override
  protected @Nullable Map<String, Object> getPromptContext(
      @Nullable UserGenerationRequest userGenerationRequest) {
    var count = Optional.ofNullable(userGenerationRequest).map(UserGenerationRequest::count)
        .orElse(1);
    return Map.of(
        "count", Math.max(1, count)
    );
  }

  @Override
  protected ChatClient getChatClient() {
    return this.chatClient;
  }

  public record UserGenerationRequest(int count) {

  }
}
