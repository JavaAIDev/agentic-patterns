package com.javaaidev.agenticpatterns.taskexecution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;

@Disabled
class TaskExecutionAgentBuilderTest {

  private final ChatClient chatClient = ChatClient.builder(
      new OpenAiChatModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")))).build();

  @Test
  void testBuilder() {
    var agent = TaskExecutionAgent.<JokeInput, JokeOutput>builder()
        .agentName("test")
        .responseType(JokeOutput.class)
        .chatClient(chatClient)
        .promptTemplate("""
            tell me {count} jokes
            """)
        .build();
    var output = agent.call(new JokeInput(2));
    assertNotNull(output);
    assertEquals(2, output.jokes().size());
  }

  record JokeInput(int count) {

  }

  record JokeOutput(List<String> jokes) {

  }
}