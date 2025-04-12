package com.javaaidev.agenticpatterns.routingworkflow;

/**
 * Routing response
 *
 * @param name   Selected route name
 * @param reason Reason for route selection
 */
public record RoutingResponse(String name, String reason) {

}
