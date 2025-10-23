package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Evaluate a generation output
 *
 * @param <GenInput>  Type of generation input
 * @param <GenOutput> Type of generation output
 * @param <ER>        Type of evaluation result
 */
public interface EvaluationStep<GenInput, GenOutput, ER extends EvaluationResult> {

  /**
   * Evaluate a generation output
   *
   * @param genInput  Generation input
   * @param genOutput Current generation output
   * @return Evaluation result
   * @see EvaluationResult
   */
  ER evaluate(@Nullable GenInput genInput, GenOutput genOutput);

  record EvaluationInput<GenIn, GenOut>(@Nullable GenIn genInput,
                                        GenOut genOutput) {

  }
}
