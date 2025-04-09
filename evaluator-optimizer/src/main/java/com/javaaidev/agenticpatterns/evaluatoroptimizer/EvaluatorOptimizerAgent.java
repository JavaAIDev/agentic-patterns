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
 * @param <Request>  Type of agent input
 * @param <Result>   Type of intermediate result
 * @param <Response> Type of agent output
 */
public abstract class EvaluatorOptimizerAgent<Request, Result, Response> extends
    NoLLMTaskExecutionAgent<Request, Response> {

  protected ChatClient generationChatClient;
  protected ChatClient evaluationChatClient;
  @Nullable
  private final InitializationStep<Request> initializationStep;
  private final FinalizationStep<Request, Result, Response> finalizationStep;

  protected EvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient,
      FinalizationStep<Request, Result, Response> finalizationStep) {
    this(generationChatClient, evaluationChatClient, null, null, null, finalizationStep);
  }

  protected EvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient,
      @Nullable ObservationRegistry observationRegistry,
      FinalizationStep<Request, Result, Response> finalizationStep) {
    this(generationChatClient, evaluationChatClient, null, observationRegistry, null,
        finalizationStep);
  }

  protected EvaluatorOptimizerAgent(ChatClient generationChatClient,
      ChatClient evaluationChatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry,
      @Nullable InitializationStep<Request> initializationStep,
      FinalizationStep<Request, Result, Response> finalizationStep) {
    super(responseType, observationRegistry);
    this.generationChatClient = generationChatClient;
    this.evaluationChatClient = evaluationChatClient;
    this.initializationStep = initializationStep;
    this.finalizationStep = finalizationStep;
    initAgents();
  }

  private void initAgents() {
    initialResultAgent = buildInitialResultAgent(generationChatClient, observationRegistry);
    evaluationAgent = buildEvaluationAgent(evaluationChatClient, observationRegistry);
    optimizationAgent = buildOptimizationAgent(generationChatClient, observationRegistry);
  }

  protected TaskExecutionAgent<Request, Result> initialResultAgent;
  @Nullable
  protected TaskExecutionAgent<Result, Evaluation> evaluationAgent;
  @Nullable
  protected TaskExecutionAgent<OptimizationInput<Result>, Result> optimizationAgent;

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
  protected abstract TaskExecutionAgent<Request, Result> buildInitialResultAgent(
      ChatClient chatClient, @Nullable ObservationRegistry observationRegistry);

  /**
   * Build the agent to evaluate the result
   *
   * @return the agent, see {@linkplain TaskExecutionAgent}
   */
  protected abstract TaskExecutionAgent<Result, Evaluation> buildEvaluationAgent(
      ChatClient chatClient, @Nullable ObservationRegistry observationRegistry);

  /**
   * Build the agent to optimize the result
   *
   * @return the agent, see {@linkplain TaskExecutionAgent}
   */
  protected abstract TaskExecutionAgent<OptimizationInput<Result>, Result> buildOptimizationAgent(
      ChatClient chatClient, @Nullable ObservationRegistry observationRegistry);

  @Override
  public Response call(@Nullable Request request) {
    return instrumentedCall(request, this::doCall);
  }

  private Response doCall(@Nullable Request originalRequest) {
    var request = originalRequest;
    if (initializationStep != null) {
      request = initializationStep.initialize(originalRequest);
    }
    var initialResult = initialResultAgent.call(request);
    if (evaluationAgent == null || optimizationAgent == null) {
      return finalizationStep.finalize(request, initialResult);
    }
    int iteration = 0;
    Result result = initialResult;
    Evaluation evaluation;
    do {
      LOGGER.info("Begin evaluation #{}", iteration);
      evaluation = evaluationAgent.call(result);
      if (evaluation.passed()) {
        LOGGER.info("Evaluation passed in #{}", iteration);
        break;
      }
      LOGGER.info("Begin optimization #{}", iteration);
      result = optimizationAgent.call(new OptimizationInput<>(result, evaluation));
      LOGGER.info("Optimization finished #{}", iteration);
      iteration++;
    } while (iteration < getMaxIterations());
    return finalizationStep.finalize(request, result);
  }

  public record OptimizationInput<Result>(Result result, Evaluation evaluation) {

  }

}
