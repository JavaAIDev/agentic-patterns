package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jspecify.annotations.Nullable;

/**
 * Boolean evaluation result
 *
 * @param passed   Passed or not passed
 * @param feedback Feedback if not passed
 */
public record BooleanEvaluationResult(
    @JsonPropertyDescription("If evaluation passed") boolean passed,
    @JsonPropertyDescription("Feedback of evaluation") @Nullable String feedback) implements
    EvaluationResult {

  @Override
  @Nullable
  public String getFeedback() {
    return feedback();
  }
}
