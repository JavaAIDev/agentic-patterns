package com.javaaidev.agenticpatterns.parallelizationworkflow;

import org.jspecify.annotations.Nullable;

public interface ResponseAssembler<Request, Response> {

  Response assemble(@Nullable Request request, TaskExecutionResults results);
}
