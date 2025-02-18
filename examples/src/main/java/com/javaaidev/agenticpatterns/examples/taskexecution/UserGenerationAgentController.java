package com.javaaidev.agenticpatterns.examples.taskexecution;

import com.javaaidev.agenticpatterns.examples.taskexecution.UserGenerationAgent.UserGenerationRequest;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users_generation")
public class UserGenerationAgentController {

  private final UserGenerationAgent userGenerationAgent;

  public UserGenerationAgentController(ChatClient.Builder chatClientBuilder) {
    this.userGenerationAgent = new UserGenerationAgent(chatClientBuilder.build());
  }

  @PostMapping
  public List<User> generateUsers(@RequestBody UserGenerationRequest request) {
    return userGenerationAgent.call(request);
  }
}
