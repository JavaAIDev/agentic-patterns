package com.javaaidev.agenticpatterns.taskexecution;

import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import org.jspecify.annotations.Nullable;

/**
 * A task execution agent without using LLM
 *
 * @param <Request>
 * @param <Response>
 */
public abstract class NoLLMTaskExecutionAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  protected NoLLMTaskExecutionAgent() {
    super(null);
  }
  
  protected NoLLMTaskExecutionAgent(
      @Nullable ObservationRegistry observationRegistry) {
    super(null, null, observationRegistry);
  }

  protected NoLLMTaskExecutionAgent(@Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(null, responseType, observationRegistry);
  }

  @Override
  protected String getPromptTemplate() {
    return "";
  }
}
