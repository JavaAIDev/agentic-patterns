package com.javaaidev.agenticpatterns.core;

import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public abstract class Agent {

  protected final ChatClient chatClient;
  @Nullable
  protected final ObservationRegistry observationRegistry;

  protected Agent(ChatClient chatClient) {
    this.chatClient = chatClient;
    this.observationRegistry = null;
  }

  protected Agent(ChatClient chatClient, @Nullable ObservationRegistry observationRegistry) {
    this.chatClient = chatClient;
    this.observationRegistry = observationRegistry;
  }

  protected String getName() {
    return this.getClass().getSimpleName();
  }
}
