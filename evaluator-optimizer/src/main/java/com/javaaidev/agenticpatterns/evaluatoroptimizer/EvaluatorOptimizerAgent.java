package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.taskexecution.NoLLMTaskExecutionAgent;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Evaluator-Optimizer Agent, refer to the <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/evaluator-optimizer">pattern</a>
 *
 * @param <Request>
 * @param <Response>
 */
public abstract class EvaluatorOptimizerAgent<Request, Response> extends
    NoLLMTaskExecutionAgent<Request, Response> {

  protected ChatClient generationChatClient;
  protected ChatClient evaluationChatClient;

  protected EvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient) {
    this(generationChatClient, evaluationChatClient, null, null);
  }

  public EvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient, @Nullable ObservationRegistry observationRegistry) {
    this(generationChatClient, evaluationChatClient, null, observationRegistry);
  }

  protected EvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient, @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(responseType, observationRegistry);
    this.generationChatClient = generationChatClient;
    this.evaluationChatClient = evaluationChatClient;
    initAgents();
  }

  private void initAgents() {
    initialResultAgent = buildInitialResultAgent(generationChatClient, observationRegistry);
    evaluationAgent = buildEvaluationAgent(evaluationChatClient, observationRegistry);
    optimizationAgent = buildOptimizationAgent(generationChatClient, observationRegistry);
  }

  protected TaskExecutionAgent<Request, Response> initialResultAgent;
  @Nullable
  protected TaskExecutionAgent<Response, Evaluation> evaluationAgent;
  @Nullable
  protected TaskExecutionAgent<OptimizationInput<Response>, Response> optimizationAgent;

  private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorOptimizerAgent.class);

  /**
   * The maximum number of evaluation iterations, default to 3
   *
   * @return Maximum number of iterations
   */
  protected int getMaxIterations() {
    return 3;
  }

  /**
   * Build the agent to generation initial result
   *
   * @return the agent, see {@linkplain TaskExecutionAgent}
   */
  protected abstract TaskExecutionAgent<Request, Response> buildInitialResultAgent(
      ChatClient chatClient, @Nullable ObservationRegistry observationRegistry);

  /**
   * Build the agent to evaluate the result
   *
   * @return the agent, see {@linkplain TaskExecutionAgent}
   */
  protected abstract TaskExecutionAgent<Response, Evaluation> buildEvaluationAgent(
      ChatClient chatClient, @Nullable ObservationRegistry observationRegistry);

  /**
   * Build the agent to optimize the result
   *
   * @return the agent, see {@linkplain TaskExecutionAgent}
   */
  protected abstract TaskExecutionAgent<OptimizationInput<Response>, Response> buildOptimizationAgent(
      ChatClient chatClient, @Nullable ObservationRegistry observationRegistry);

  @Override
  public Response call(@Nullable Request request) {
    return instrumentedCall(request, this::doCall);
  }

  private Response doCall(@Nullable Request request) {
    var initialResult = initialResultAgent.call(request);
    if (evaluationAgent == null || optimizationAgent == null) {
      return initialResult;
    }
    int iteration = 0;
    Response result = initialResult;
    Evaluation evaluation;
    do {
      LOGGER.info("Begin evaluation #{}", iteration);
      evaluation = evaluationAgent.call(result);
      if (evaluation.passed()) {
        LOGGER.info("Evaluation passed in #{}", iteration);
        return result;
      }
      LOGGER.info("Begin optimization #{}", iteration);
      result = optimizationAgent.call(new OptimizationInput<>(result, evaluation));
      LOGGER.info("Optimization finished #{}", iteration);
      iteration++;
    } while (iteration < getMaxIterations());
    return result;
  }

  public record OptimizationInput<Response>(Response response, Evaluation evaluation) {

  }

}
