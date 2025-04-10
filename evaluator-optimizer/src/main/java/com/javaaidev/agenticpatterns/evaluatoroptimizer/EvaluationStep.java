package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

public interface EvaluationStep<GenInput, GenOutput, ER extends EvaluationResult> {

  ER evaluate(@Nullable GenInput genInput, GenOutput genOutput);

  record EvaluationInput<GenIn, GenOut>(@Nullable GenIn genInput, GenOut genOutput) {

  }
}
