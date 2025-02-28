package com.javaaidev.agenticpatterns.routingworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;

/**
 * A routing choice
 *
 * @param name        Name of the route
 * @param description Description of the route
 * @param agent       Task of the route
 * @param <Request>   Task input type
 * @param <Response>  Task output type
 */
public record RoutingChoice<Request, Response>(String name,
                                               String description,
                                               TaskExecutionAgent<Request, Response> agent) {

}
