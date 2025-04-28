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
      OpenAiChatModel.builder()
          .openAiApi(OpenAiApi.builder()
              .apiKey(System.getenv("OPENAI_API_KEY"))
              .build())
          .build()).build();

  @Test
  void testBuilder() {
    var agent = TaskExecutionAgent.<JokeInput, JokeOutput>defaultBuilder()
        .name("test")
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