package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Evaluation result
 */
public interface EvaluationResult {

  /**
   * Feedback of evaluation
   *
   * @return feedback
   */
  @Nullable
  String getFeedback();
}
