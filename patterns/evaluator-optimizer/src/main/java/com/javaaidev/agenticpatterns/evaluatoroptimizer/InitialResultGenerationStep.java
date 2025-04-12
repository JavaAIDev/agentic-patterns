package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import org.jspecify.annotations.Nullable;

/**
 * Generate initial output
 *
 * @param <GenInput>  Type of generation input
 * @param <GenOutput> Type of generation output
 */
public interface InitialResultGenerationStep<GenInput, GenOutput> {

  /**
   * Generation initial output
   *
   * @param genInput Generation input
   * @return Initial generation output
   */
  GenOutput generate(@Nullable GenInput genInput);
}
