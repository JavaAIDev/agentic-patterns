package com.javaaidev.agenticpatterns.core;

import org.springframework.ai.chat.client.ChatClient;

public abstract class Agent {

  protected abstract ChatClient getChatClient();
}
