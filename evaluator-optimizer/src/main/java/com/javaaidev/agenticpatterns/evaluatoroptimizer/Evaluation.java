package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

public record Evaluation(boolean passed, @Nullable String feedback) {

}
