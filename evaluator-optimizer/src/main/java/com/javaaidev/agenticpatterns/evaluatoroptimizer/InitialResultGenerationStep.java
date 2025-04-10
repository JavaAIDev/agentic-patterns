package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

public interface InitialResultGenerationStep<GenInput, GenOutput> {

  GenOutput generate(@Nullable GenInput genInput);
}
