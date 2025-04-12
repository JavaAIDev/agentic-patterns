package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflow;
import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflowBuilder;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.EvaluationStep.EvaluationInput;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.FinalizationStep.FinalizationInput;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.OptimizationStep.OptimizationInput;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Evaluator-Optimizer Workflow, refer to the <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/evaluator-optimizer">pattern</a>
 *
 * @param <Request>   Type of agent input
 * @param <GenInput>  Type of generation input
 * @param <GenOutput> Type of generation output
 * @param <ER>        Type of evaluation result
 * @param <Response>  Type of agent output
 */
public class EvaluatorOptimizerWorkflow<Request, GenInput, GenOutput, ER extends EvaluationResult, Response> extends
    AbstractAgenticWorkflow<Request, Response> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorOptimizerWorkflow.class);

  private final InitializationStep<Request, GenInput> initializationStep;
  private final InitialResultGenerationStep<GenInput, GenOutput> initialResultGenerationStep;
  @Nullable
  private final EvaluationStep<GenInput, GenOutput, ER> evaluationStep;
  @Nullable
  private final OptimizationStep<GenInput, GenOutput, ER> optimizationStep;
  private final FinalizationStep<Request, GenInput, GenOutput, Response> finalizationStep;
  private final Predicate<ER> evaluationPredicate;
  private final int maxNumberOfEvaluations;

  public EvaluatorOptimizerWorkflow(
      InitializationStep<Request, GenInput> initializationStep,
      InitialResultGenerationStep<GenInput, GenOutput> initialResultGenerationStep,
      @Nullable EvaluationStep<GenInput, GenOutput, ER> evaluationStep,
      @Nullable OptimizationStep<GenInput, GenOutput, ER> optimizationStep,
      FinalizationStep<Request, GenInput, GenOutput, Response> finalizationStep,
      Predicate<ER> evaluationPredicate,
      int maxNumberOfEvaluations,
      @Nullable String name,
      @Nullable ObservationRegistry observationRegistry) {
    super(name, observationRegistry);
    this.initializationStep = initializationStep;
    this.initialResultGenerationStep = initialResultGenerationStep;
    this.evaluationStep = evaluationStep;
    this.optimizationStep = optimizationStep;
    this.finalizationStep = finalizationStep;
    this.evaluationPredicate = evaluationPredicate;
    this.maxNumberOfEvaluations = Math.max(1, maxNumberOfEvaluations);
  }

  public Response doExecute(@Nullable Request request) {
    LOGGER.info("Execute evaluator-optimizer workflow");
    LOGGER.info("Initialize generation input");
    var genInput = initializationStep.initialize(request);
    LOGGER.info("Generate initial result");
    var genOutput = initialResultGenerationStep.generate(genInput);
    if (evaluationStep == null || optimizationStep == null) {
      LOGGER.info("No evaluation step or optimization step, skip the evaluation");
      return finalizationStep.finalize(request, genInput, genOutput);
    }
    int iteration = 0;
    ER evaluationResult;
    do {
      LOGGER.info("Begin evaluation #{}", iteration);
      evaluationResult = evaluationStep.evaluate(genInput, genOutput);
      LOGGER.info("Finish evaluation #{}", iteration);
      if (evaluationPredicate.test(evaluationResult)) {
        LOGGER.info("Evaluation passed in #{}", iteration);
        break;
      }
      LOGGER.info("Begin optimization #{}", iteration);
      genOutput = optimizationStep.optimize(genInput, genOutput, evaluationResult);
      LOGGER.info("Finish optimization #{}", iteration);
      iteration++;
    } while (iteration < maxNumberOfEvaluations);
    return finalizationStep.finalize(request, genInput, genOutput);
  }

  public static <Req, GenIn, GenOut, ER extends EvaluationResult, Res> Builder<Req, GenIn, GenOut, ER, Res> builder() {
    return new Builder<>();
  }

  public static class Builder<Req, GenIn, GenOut, ER extends EvaluationResult, Res> extends
      AbstractAgenticWorkflowBuilder<Req, Res, Builder<Req, GenIn, GenOut, ER, Res>> {

    private InitializationStep<Req, GenIn> initializationStep;
    private InitialResultGenerationStep<GenIn, GenOut> initialResultGenerationStep;
    @Nullable
    private EvaluationStep<GenIn, GenOut, ER> evaluationStep;
    @Nullable
    private OptimizationStep<GenIn, GenOut, ER> optimizationStep;
    private FinalizationStep<Req, GenIn, GenOut, Res> finalizationStep;
    private Predicate<ER> evaluationPredicate;
    private int maxNumberOfEvaluations = 3;

    public Builder<Req, GenIn, GenOut, ER, Res> initializationStep(
        InitializationStep<Req, GenIn> initializationStep) {
      Assert.notNull(initializationStep, "InitializationStep cannot be null");
      this.initializationStep = initializationStep;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> initializationStep(
        TaskExecutionAgent<Req, GenIn> taskExecutionAgent) {
      Assert.notNull(taskExecutionAgent, "TaskExecutionAgent cannot be null");
      this.initializationStep = taskExecutionAgent::call;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> initialResultGenerationStep(
        InitialResultGenerationStep<GenIn, GenOut> initialResultGenerationStep) {
      Assert.notNull(initialResultGenerationStep, "InitialResultGenerationStep cannot be null");
      this.initialResultGenerationStep = initialResultGenerationStep;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> initialResultGenerationStep(
        TaskExecutionAgent<GenIn, GenOut> taskExecutionAgent) {
      Assert.notNull(taskExecutionAgent, "TaskExecutionAgent cannot be null");
      this.initialResultGenerationStep = taskExecutionAgent::call;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> evaluationStep(
        EvaluationStep<GenIn, GenOut, ER> evaluationStep) {
      this.evaluationStep = evaluationStep;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> evaluationStep(
        TaskExecutionAgent<EvaluationInput<GenIn, GenOut>, ER> taskExecutionAgent) {
      Assert.notNull(taskExecutionAgent, "TaskExecutionAgent cannot be null");
      this.evaluationStep = (genIn, genOut) ->
          taskExecutionAgent.call(
              new EvaluationInput<>(genIn, genOut));
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> optimizationStep(
        OptimizationStep<GenIn, GenOut, ER> optimizationStep) {
      this.optimizationStep = optimizationStep;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> optimizationStep(
        TaskExecutionAgent<OptimizationInput<GenIn, GenOut, ER>, GenOut> taskExecutionAgent) {
      Assert.notNull(taskExecutionAgent, "TaskExecutionAgent cannot be null");
      this.optimizationStep = (genIn, genOut, evaluationResult) -> taskExecutionAgent.call(
          new OptimizationInput<>(genIn, genOut, evaluationResult));
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> finalizationStep(
        FinalizationStep<Req, GenIn, GenOut, Res> finalizationStep) {
      Assert.notNull(finalizationStep, "FinalizationStep cannot be null");
      this.finalizationStep = finalizationStep;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> finalizationStep(
        TaskExecutionAgent<FinalizationInput<Req, GenIn, GenOut>, Res> taskExecutionAgent) {
      Assert.notNull(taskExecutionAgent, "TaskExecutionAgent cannot be null");
      this.finalizationStep = (req, genIn, genOut) -> taskExecutionAgent.call(
          new FinalizationInput<>(req, genIn, genOut));
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> evaluationPredicate(
        Predicate<ER> evaluationPredicate) {
      Assert.notNull(evaluationPredicate, "evaluationPredicate cannot be null");
      this.evaluationPredicate = evaluationPredicate;
      return this;
    }

    public Builder<Req, GenIn, GenOut, ER, Res> maxNumberOfEvaluations(int maxNumberOfEvaluations) {
      this.maxNumberOfEvaluations = Math.max(1, maxNumberOfEvaluations);
      return this;
    }

    @Override
    public EvaluatorOptimizerWorkflow<Req, GenIn, GenOut, ER, Res> build() {
      Assert.notNull(initializationStep, "InitializationStep cannot be null");
      Assert.notNull(initialResultGenerationStep, "InitialResultGenerationStep cannot be null");
      Assert.notNull(finalizationStep, "FinalizationStep cannot be null");
      Assert.notNull(evaluationPredicate, "evaluationPredicate cannot be null");
      return new EvaluatorOptimizerWorkflow<>(
          initializationStep,
          initialResultGenerationStep,
          evaluationStep,
          optimizationStep,
          finalizationStep,
          evaluationPredicate,
          maxNumberOfEvaluations,
          name,
          observationRegistry);
    }
  }
}
