package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Evaluation result
 *
 * @param passed   Passed or not passed
 * @param feedback Feedback if not passed
 */
public record Evaluation(boolean passed, @Nullable String feedback) {

}
