package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

public interface OptimizationStep<GenInput, GenOutput, ER extends EvaluationResult> {

  GenOutput optimize(@Nullable GenInput genInput, GenOutput genOutput,
      ER evaluationResult);

  record OptimizationInput<GenIn, GenOut, ER>(
      @Nullable GenIn genInput,
      GenOut genOutput,
      ER evaluationResult) {

  }
}
