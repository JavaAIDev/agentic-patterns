package com.javaaidev.agenticpatterns.examples.agentastool;

import com.javaaidev.agenticpatterns.examples.agentastool.RestAPITestCaseGenerationAgent.RestAPITestCaseGenerationResponse;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

public class RestAPITestCaseGenerationAgent extends
    TaskExecutionAgent<Void, RestAPITestCaseGenerationResponse> {

  protected RestAPITestCaseGenerationAgent(ChatClient chatClient) {
    super(chatClient);
  }

  @Override
  protected String getPromptTemplate() {
    return """
        Goal: Write Python code to test a REST API.
        
        Requirements:
        - The API is responsible for creating a new user.
        - Use a POST request with JSON content to create a new user.
        - When creation is successful, the API returns a 201 code.
        - The API url is http://localhost:8080/api/v1/user.
        - User data has a defined structure, don't generate it yourself, use the tool to generate it.
        """;
  }

  @Override
  protected void updateRequest(ChatClientRequestSpec spec) {
    spec.functions("userGenerationAgent");
  }

  public record RestAPITestCaseGenerationResponse(String code) {

  }
}
