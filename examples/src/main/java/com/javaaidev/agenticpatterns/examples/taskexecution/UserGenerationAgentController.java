package com.javaaidev.agenticpatterns.examples.taskexecution;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users_generation")
public class UserGenerationAgentController {

  private final TaskExecutionAgent<UserGenerationRequest, UserGenerationResponse> userGenerationAgent;

  public UserGenerationAgentController(
      @Qualifier("userGenerationAgent") TaskExecutionAgent<UserGenerationRequest, UserGenerationResponse> userGenerationAgent) {
    this.userGenerationAgent = userGenerationAgent;
  }

  @PostMapping
  public UserGenerationResponse generateUsers(@RequestBody UserGenerationRequest request) {
    return userGenerationAgent.call(request);
  }
}
