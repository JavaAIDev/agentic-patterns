package com.javaaidev.agenticpatterns.core;

import io.modelcontextprotocol.client.transport.ServerParameters;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStdioClientProperties.Parameters;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStreamableHttpClientProperties.ConnectionParameters;

public record McpClientConfiguration(
    @Nullable StdioClientProperties stdioClientProperties,
    @Nullable SseClientProperties sseClientProperties,
    @Nullable StreamableHttpClientProperties streamableHttpClientProperties
) {

  public McpClientConfiguration(StdioClientProperties stdioClientProperties) {
    this(stdioClientProperties, null, null);
  }

  public McpClientConfiguration(SseClientProperties sseClientProperties) {
    this(null, sseClientProperties, null);
  }

  public McpClientConfiguration(StreamableHttpClientProperties streamableHttpClientProperties) {
    this(null, null, streamableHttpClientProperties);
  }

  public record StdioClientProperties(Map<String, Parameters> connections) {

    public Map<String, ServerParameters> toServerParameters() {
      Map<String, ServerParameters> serverParameters = new HashMap<>();
      for (Map.Entry<String, Parameters> entry : this.connections.entrySet()) {
        serverParameters.put(entry.getKey(), entry.getValue().toServerParameters());
      }
      return serverParameters;
    }
  }

  public record SseClientProperties(Map<String, SseParameters> connections) {

  }

  public record StreamableHttpClientProperties(Map<String, ConnectionParameters> connections) {

  }
}
