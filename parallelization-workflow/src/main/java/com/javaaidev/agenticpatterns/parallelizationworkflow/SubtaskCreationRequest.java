package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.util.function.Function;

/**
 * Request to create a subtask
 *
 * @param taskId             Task id
 * @param task               Task, see {@linkplain TaskExecutionAgent}
 * @param requestTransformer Transform request to task inpu
 * @param <Request>          Request type
 */
public record SubtaskCreationRequest<Request>(
    String taskId,
    TaskExecutionAgent<?, ?> task,
    Function<Request, ?> requestTransformer
) {

}
