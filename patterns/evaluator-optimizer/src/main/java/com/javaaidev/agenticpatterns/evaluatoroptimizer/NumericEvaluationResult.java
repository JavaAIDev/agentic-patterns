package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

/**
 * Numeric evaluation result
 *
 * @param score    Evaluation score
 * @param feedback Feedback if not passed
 */
public record NumericEvaluationResult(
    @JsonPropertyDescription("Evaluation score, value from 0 to 100") int score,
    @JsonPropertyDescription("Feedback of evaluation") @Nullable String feedback) implements
    EvaluationResult {

  @Override
  public @Nullable String getFeedback() {
    return feedback();
  }
}
