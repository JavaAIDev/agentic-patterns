package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

/**
 * A {@linkplain EvaluatorOptimizerAgent} implementation uses prompts for subtask agents
 *
 * @param <Request>
 * @param <Response>
 */
public abstract class PromptBasedEvaluatorOptimizerAgent<Request, Response> extends
    EvaluatorOptimizerAgent<Request, Response> {


  protected PromptBasedEvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient) {
    this(generationChatClient, evaluationChatClient, null, null);
  }

  protected PromptBasedEvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(generationChatClient, evaluationChatClient, responseType, observationRegistry);
  }

  /**
   * Prompt template for the agent to generate initial result
   *
   * @return Prompt template
   */
  protected abstract String getInitialResultPromptTemplate();

  /**
   * Prepare for the values of variables in the prompt template to generate initial result
   *
   * @param request Request
   * @return Values of variables
   */
  protected @Nullable Map<String, Object> buildInitialResultPromptContext(
      @Nullable Request request) {
    return new HashMap<>();
  }

  public class GenerateInitialResultAgent extends TaskExecutionAgent<Request, Response> {

    public GenerateInitialResultAgent(ChatClient chatClient, @Nullable Type responseType,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, responseType, observationRegistry);
    }

    @Override
    protected String getPromptTemplate() {
      return getInitialResultPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
      return buildInitialResultPromptContext(request);
    }
  }

  @Override
  protected TaskExecutionAgent<Request, Response> buildInitialResultAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    return new GenerateInitialResultAgent(chatClient, responseType, observationRegistry);
  }

  /**
   * Prompt template for the agent to evaluate a result
   *
   * @return Prompt template
   */
  protected abstract String getEvaluationPromptTemplate();

  /**
   * Prepare for the values of variables in the prompt template to evaluate a result
   *
   * @param response Response from a previous generation
   * @return Values of variables
   */
  protected @Nullable Map<String, Object> buildEvaluationPromptContext(
      @Nullable Response response) {
    return new HashMap<>();
  }

  public class EvaluateAgent extends TaskExecutionAgent<Response, Evaluation> {

    protected EvaluateAgent(ChatClient chatClient,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, Evaluation.class, observationRegistry);
    }

    @Override
    protected String getPromptTemplate() {
      return getEvaluationPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(@Nullable Response response) {
      return buildEvaluationPromptContext(response);
    }
  }

  @Override
  protected TaskExecutionAgent<Response, Evaluation> buildEvaluationAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    return new EvaluateAgent(chatClient, observationRegistry);
  }

  /**
   * Prompt template for the agent to optimize a result
   *
   * @return Prompt template
   */
  protected abstract String getOptimizationPromptTemplate();

  /**
   * Prepare for the values of variables in the prompt template to optimize a result
   *
   * @param optimizationInput Input for optimization
   * @return Values of variables
   */
  protected @Nullable Map<String, Object> buildOptimizationPromptContext(
      @Nullable OptimizationInput<Response> optimizationInput) {
    return new HashMap<>();
  }

  public class OptimizeAgent extends TaskExecutionAgent<OptimizationInput<Response>, Response> {

    public OptimizeAgent(ChatClient chatClient, @Nullable Type responseType,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, responseType, observationRegistry);
    }

    @Override
    protected String getPromptTemplate() {
      return getOptimizationPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(
        @Nullable OptimizationInput<Response> responseOptimizationInput) {
      return buildOptimizationPromptContext(responseOptimizationInput);
    }
  }

  @Override
  protected TaskExecutionAgent<OptimizationInput<Response>, Response> buildOptimizationAgent(
      ChatClient chatClient, @Nullable ObservationRegistry observationRegistry) {
    return new OptimizeAgent(chatClient, responseType, observationRegistry);
  }
}
