package com.javaaidev.agenticpatterns.taskexecution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaaidev.agenticpatterns.core.Agent;
import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.core.McpClientConfiguration;
import com.javaaidev.agenticpatterns.core.TypeResolver;
import com.javaaidev.agenticpatterns.core.observation.AgentExecutionObservationContext;
import com.javaaidev.agenticpatterns.core.observation.AgentExecutionObservationDocumentation;
import com.javaaidev.agenticpatterns.core.observation.DefaultAgentExecutionObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.ai.tool.StaticToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.CollectionUtils;

/**
 * Task Execution Agent, refer to the <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/task-execution">pattern</a>
 *
 * @param <Request>  Type of agent input
 * @param <Response> Type of agent output
 */
public abstract class TaskExecutionAgent<Request, Response> extends
    Agent implements Function<Request, Response> {

  @Nullable
  protected Type responseType;
  protected String promptTemplate;
  @Nullable
  protected Function<Request, Map<String, Object>> promptTemplateContextProvider;
  @Nullable
  protected Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater;
  @Nullable
  protected McpClientConfiguration mcpClientConfiguration;
  protected Predicate<String> toolFilter = (name) -> true;
  protected String name = super.getName();
  protected ObjectMapper objectMapper = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(
      TaskExecutionAgent.class);

  protected TaskExecutionAgent(ChatClient chatClient) {
    this(chatClient, (ObservationRegistry) null);
  }

  protected TaskExecutionAgent(ChatClient chatClient,
      @Nullable Type responseType) {
    this(chatClient, responseType, null);
  }

  protected TaskExecutionAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
    this.responseType = TypeResolver.resolveType(this.getClass(),
        TaskExecutionAgent.class, 1);
  }

  protected TaskExecutionAgent(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
    this.responseType = responseType;
  }

  protected TaskExecutionAgent(ChatClient chatClient,
      String promptTemplate,
      @Nullable Type responseType,
      @Nullable Function<Request, Map<String, Object>> promptTemplateContextProvider,
      @Nullable Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater,
      @Nullable McpClientConfiguration mcpClientConfiguration,
      @Nullable Predicate<String> toolFilter,
      @Nullable String name,
      @Nullable ObservationRegistry observationRegistry,
      @Nullable ObjectMapper objectMapper
  ) {
    super(chatClient, observationRegistry);
    this.promptTemplate = promptTemplate;
    this.responseType = responseType;
    this.promptTemplateContextProvider = promptTemplateContextProvider;
    this.chatClientRequestSpecUpdater = chatClientRequestSpecUpdater;
    this.mcpClientConfiguration = mcpClientConfiguration;
    if (toolFilter != null) {
      this.toolFilter = toolFilter;
    }
    if (name != null) {
      this.name = name;
    }
    if (objectMapper != null) {
      this.objectMapper = objectMapper;
    }
  }

  /**
   * Get the prompt template
   *
   * @return prompt template
   */
  protected String getPromptTemplate() {
    return promptTemplate;
  }

  /**
   * Prepare for the values of variables in the prompt template
   *
   * @param request Task input
   * @return Values of values
   */
  @Nullable
  protected Map<String, Object> getPromptContext(@Nullable Request request) {
    if (promptTemplateContextProvider != null) {
      return promptTemplateContextProvider.apply(request);
    }
    return AgentUtils.objectToMap(request);
  }

  /**
   * Customize request sent to LLM
   *
   * @param spec {@linkplain ChatClientRequestSpec} from Spring AI
   */
  protected void updateChatClientRequest(ChatClientRequestSpec spec) {
    if (chatClientRequestSpecUpdater != null) {
      chatClientRequestSpecUpdater.accept(spec);
    }
  }

  @Override
  protected String getName() {
    return this.name;
  }

  @Override
  public Response apply(Request request) {
    return call(request);
  }

  public Response call(@Nullable Request request) {
    return instrumentedCall(request, this::doCall);
  }

  protected Response instrumentedCall(@Nullable Request request,
      Function<Request, Response> action) {
    if (observationRegistry == null) {
      return action.apply(request);
    }
    var observationContext = new AgentExecutionObservationContext(getName(),
        request);
    var observation =
        AgentExecutionObservationDocumentation.AGENT_EXECUTION.observation(
            null,
            new DefaultAgentExecutionObservationConvention(),
            () -> observationContext,
            observationRegistry
        ).start();
    try (var ignored = observation.openScope()) {
      var response = action.apply(request);
      observationContext.setResponse(response);
      return response;
    } catch (Exception e) {
      observation.error(e);
      throw new AgentExecutionException("Error in agent execution", e);
    } finally {
      observation.stop();
    }
  }

  private Response doCall(@Nullable Request request) {
    LOGGER.info("Execute agent with request: {}", request);
    var template = getPromptTemplate();
    if (template.isBlank()) {
      throw new AgentExecutionException("Blank prompt template");
    }
    var type = responseType != null ? responseType : Object.class;
    var context = Optional.ofNullable(getPromptContext(request))
        .map(HashMap::new).orElseGet(HashMap::new);
    var toolCallbackProvider = getToolCallbackProvider();
    var toolNames = Arrays.stream(toolCallbackProvider.getToolCallbacks())
        .map(toolCallback -> toolCallback.getToolDefinition().name())
        .filter(toolName -> toolFilter.test(toolName))
        .toArray(String[]::new);
    LOGGER.info("Tool names to use: {}", toolNames);
    var requestSpec = chatClient.prompt()
        .user(userSpec -> userSpec.text(template).params(context))
        .toolCallbacks(getToolCallbackProvider())
        .toolNames(toolNames);
    updateChatClientRequest(requestSpec);
    var responseSpec = requestSpec.call();
    Response output;
    if (type instanceof Class<?> clazz) {
      output = (Response) responseSpec.entity(clazz);
    } else if (type instanceof ParameterizedType parameterizedType) {
      output = responseSpec.entity(
          ParameterizedTypeReference.forType(parameterizedType));
    } else {
      throw new AgentExecutionException("Invalid type " + type);
    }
    if (output == null) {
      throw new AgentExecutionException("Empty or bad response from LLM");
    }
    LOGGER.info("Execution finished with output: {}", output);
    return output;
  }

  protected ToolCallbackProvider getToolCallbackProvider() {
    if (mcpClientConfiguration == null) {
      return new StaticToolCallbackProvider();
    }
    List<NamedClientMcpTransport> transports = new ArrayList<>();
    var stdioProperties = mcpClientConfiguration.stdioClientProperties();
    if (stdioProperties != null) {
      for (Map.Entry<String, ServerParameters> serverParameters : stdioProperties.toServerParameters()
          .entrySet()) {
        var transport = new StdioClientTransport(serverParameters.getValue());
        transports.add(new NamedClientMcpTransport(serverParameters.getKey(),
            transport));
      }
    }
    var sseProperties = mcpClientConfiguration.sseClientProperties();
    if (sseProperties != null) {
      for (Map.Entry<String, SseParameters> serverParameters : sseProperties.connections()
          .entrySet()) {
        String baseUrl = serverParameters.getValue().url();
        String sseEndpoint = serverParameters.getValue().sseEndpoint() != null
            ? serverParameters.getValue().sseEndpoint() : "/sse";
        var transport = HttpClientSseClientTransport.builder(baseUrl)
            .sseEndpoint(sseEndpoint)
            .clientBuilder(HttpClient.newBuilder())
            .objectMapper(objectMapper)
            .build();
        transports.add(
            new NamedClientMcpTransport(serverParameters.getKey(),
                transport));
      }
    }

    List<McpAsyncClient> mcpAsyncClients = new ArrayList<>();
    if (!CollectionUtils.isEmpty(transports)) {
      for (NamedClientMcpTransport namedTransport : transports) {

        McpSchema.Implementation clientInfo = new McpSchema.Implementation(
            namedTransport.name(),
            "1.0.0");

        McpClient.AsyncSpec spec = McpClient.async(namedTransport.transport())
            .clientInfo(clientInfo)
            .requestTimeout(Duration.ofSeconds(30));

        var client = spec.build();
        client.initialize().block();

        mcpAsyncClients.add(client);
      }
      return new AsyncMcpToolCallbackProvider(mcpAsyncClients);
    }
    return new StaticToolCallbackProvider();
  }

  public static <Req, Res> DefaultTaskExecutionAgentBuilder<Req, Res> defaultBuilder() {
    return new DefaultTaskExecutionAgentBuilder<>();
  }

  interface Builder<Request, Response, T extends TaskExecutionAgent.Builder<Request, Response, T>> {

    T chatClient(ChatClient chatClient);

    T promptTemplate(String promptTemplate);

    T name(String name);

    T responseType(Type responseType);

    T promptTemplateContextProvider(
        Function<Request, Map<String, Object>> promptTemplateContextProvider);

    T chatClientRequestSpecUpdater(
        Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater);

    T observationRegistry(ObservationRegistry observationRegistry);

    T mcpClientConfiguration(McpClientConfiguration mcpClientConfiguration);

    T toolFilter(Predicate<String> toolFilter);

    T objectMapper(ObjectMapper objectMapper);

    TaskExecutionAgent<Request, Response> build();
  }

}
