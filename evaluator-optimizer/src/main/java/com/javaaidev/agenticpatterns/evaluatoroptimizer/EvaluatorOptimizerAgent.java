package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluator-Optimizer Agent, refer to the <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/evaluator-optimizer">pattern</a>
 *
 * @param <Request>
 * @param <Response>
 */
public abstract class EvaluatorOptimizerAgent<Request, Response> {

  protected TaskExecutionAgent<Request, Response> initialResultAgent = buildInitialResultAgent();
  @Nullable
  protected TaskExecutionAgent<Response, Evaluation> evaluationAgent = buildEvaluationAgent();
  @Nullable
  protected TaskExecutionAgent<OptimizationInput<Response>, Response> optimizationAgent = buildOptimizationAgent();

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
  protected abstract TaskExecutionAgent<Request, Response> buildInitialResultAgent();

  /**
   * Build the agent to evaluate the result
   *
   * @return the agent, see {@linkplain TaskExecutionAgent}
   */
  protected abstract TaskExecutionAgent<Response, Evaluation> buildEvaluationAgent();

  /**
   * Build the agent to optimize the result
   *
   * @return the agent, see {@linkplain TaskExecutionAgent}
   */
  protected abstract TaskExecutionAgent<OptimizationInput<Response>, Response> buildOptimizationAgent();

  public Response call(@Nullable Request request) {
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
