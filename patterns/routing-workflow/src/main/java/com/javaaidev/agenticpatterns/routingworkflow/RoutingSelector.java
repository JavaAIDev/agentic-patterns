package com.javaaidev.agenticpatterns.routingworkflow;

public interface RoutingSelector<Request, Response> {

  RoutingResponse select(RoutingRequest<Request, Response> request);
}
