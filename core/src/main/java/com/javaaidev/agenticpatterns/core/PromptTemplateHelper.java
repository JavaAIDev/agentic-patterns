package com.javaaidev.agenticpatterns.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

public class PromptTemplateHelper {

  public static String loadPromptTemplateFromClasspath(String resource) {
    try {
      return new ClassPathResource(resource).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new AgentExecutionException("Prompt template not found: " + resource, e);
    }
  }
}
