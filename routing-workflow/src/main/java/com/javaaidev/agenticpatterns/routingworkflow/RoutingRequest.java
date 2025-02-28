package com.javaaidev.agenticpatterns.routingworkflow;

import java.util.List;

/**
 * Routing request
 *
 * @param request    Original task input
 * @param choices    Routing choices, see {@linkplain RoutingChoice}
 * @param <Request>  Task input type
 * @param <Response> Task output type
 */
public record RoutingRequest<Request, Response>(Request request,
                                                List<RoutingChoice<Request, Response>> choices) {

}
