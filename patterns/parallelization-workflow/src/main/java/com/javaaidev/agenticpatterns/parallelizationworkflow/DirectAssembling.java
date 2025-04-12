package com.javaaidev.agenticpatterns.parallelizationworkflow;

import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

/**
 * A {@linkplain ParallelizationWorkflowAgent} which directly assembles subtask execution results
 * without using LLM
 *
 * @param <Request>  Task input type
 * @param <Response> Task output type
 */
public abstract class DirectAssembling<Request, Response> extends
    ParallelizationWorkflowAgent<Request, Response> {

  public DirectAssembling(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  public DirectAssembling(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
  }

  @Override
  protected String getPromptTemplate() {
    return "";
  }

  @Override
  public Response call(@Nullable Request request) {
    return instrumentedCall(request, this::doCall);
  }

  private Response doCall(@Nullable Request request) {
    return assemble(runSubtasks(request));
  }

  /**
   * Assemble subtask execution results into the response
   *
   * @param results Results of subtask execution, see {@linkplain TaskExecutionResults}
   * @return Response
   */
  protected abstract Response assemble(TaskExecutionResults results);
}
