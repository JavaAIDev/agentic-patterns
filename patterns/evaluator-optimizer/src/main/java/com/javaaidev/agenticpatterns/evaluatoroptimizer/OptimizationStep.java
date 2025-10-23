package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Optimize a generation with feedback from an evaluator
 *
 * @param <GenInput>  Type of generation input
 * @param <GenOutput> Type of generation output
 * @param <ER>        Type of evaluation result
 */
public interface OptimizationStep<GenInput, GenOutput, ER extends EvaluationResult> {

  /**
   * Optimize the generation output
   *
   * @param genInput         Generation input
   * @param genOutput        Previous generation output
   * @param evaluationResult Evaluation result
   * @return Optimized generation output
   */
  GenOutput optimize(@Nullable GenInput genInput, GenOutput genOutput, ER evaluationResult);

  record OptimizationInput<GenIn, GenOut, ER>(
      @Nullable GenIn genInput,
      GenOut genOutput,
      ER evaluationResult) {

  }
}
