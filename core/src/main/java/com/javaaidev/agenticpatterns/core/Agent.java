package com.javaaidev.agenticpatterns.core;

import org.springframework.ai.chat.client.ChatClient;

public abstract class Agent {

  protected final ChatClient chatClient;

  protected Agent(ChatClient chatClient) {
    this.chatClient = chatClient;
  }
}
