package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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

  protected int getMaxIterations() {
    return 3;
  }

  protected abstract TaskExecutionAgent<Request, Response> buildInitialResultAgent();

  protected abstract TaskExecutionAgent<Response, Evaluation> buildEvaluationAgent();

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
