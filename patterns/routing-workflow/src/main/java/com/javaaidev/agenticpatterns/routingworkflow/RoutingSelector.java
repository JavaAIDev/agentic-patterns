package com.javaaidev.agenticpatterns.routingworkflow;

/**
 * A selector for target route
 *
 * @param <Request>
 * @param <Response>
 */
public interface RoutingSelector<Request, Response> {

  /**
   * Select the target route
   *
   * @param request Routing request
   * @return Routing response
   */
  RoutingResponse select(RoutingRequest<Request, Response> request);
}
