package com.javaaidev.agenticpatterns.parallelizationworkflow;

import org.jspecify.annotations.Nullable;

/**
 * An Assembler to assemble execution results of subtasks to create the final response
 *
 * @param <Request>
 * @param <Response>
 */
public interface ResponseAssembler<Request, Response> {

  /**
   * Assemble execution results of subtasks to create the final response
   *
   * @param request Request of workflow
   * @param results Execution results of subtasks
   * @return Response
   */
  Response assemble(@Nullable Request request, TaskExecutionResults results);
}
