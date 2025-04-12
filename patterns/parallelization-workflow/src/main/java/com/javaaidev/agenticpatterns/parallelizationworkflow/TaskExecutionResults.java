package com.javaaidev.agenticpatterns.parallelizationworkflow;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Execution results of subtasks
 *
 * @param results A map from task id to its result
 */
public record TaskExecutionResults(Map<String, SubtaskResult> results) {

  /**
   * Returns all successful results
   *
   * @return A map from task id to its result
   */
  public Map<String, Object> allSuccessfulResults() {
    return results().entrySet().stream().filter(entry -> entry.getValue().hasResult())
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().result()));
  }
}
