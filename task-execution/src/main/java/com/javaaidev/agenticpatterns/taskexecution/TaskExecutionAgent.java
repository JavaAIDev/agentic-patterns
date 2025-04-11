package com.javaaidev.agenticpatterns.taskexecution;

import com.javaaidev.agenticpatterns.core.Agent;
import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.core.TypeResolver;
import com.javaaidev.agenticpatterns.core.observation.AgentExecutionObservationContext;
import com.javaaidev.agenticpatterns.core.observation.AgentExecutionObservationDocumentation;
import com.javaaidev.agenticpatterns.core.observation.DefaultAgentExecutionObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Task Execution Agent, refer to the <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/task-execution">pattern</a>
 *
 * @param <Request>  Type of agent input
 * @param <Response> Type of agent output
 */
public abstract class TaskExecutionAgent<Request, Response> extends Agent implements
    Function<Request, Response> {

  @Nullable
  protected Type responseType;
  protected String promptTemplate;
  @Nullable
  protected Function<Request, Map<String, Object>> promptTemplateContextProvider;
  @Nullable
  protected Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater;
  protected String name = super.getName();

  protected TaskExecutionAgent(ChatClient chatClient) {
    this(chatClient, (ObservationRegistry) null);
  }

  protected TaskExecutionAgent(ChatClient chatClient, @Nullable Type responseType) {
    this(chatClient, responseType, null);
  }

  protected TaskExecutionAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
    this.responseType = TypeResolver.resolveType(this.getClass(), TaskExecutionAgent.class, 1);
  }

  protected TaskExecutionAgent(ChatClient chatClient, @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
    this.responseType = responseType;
  }

  protected TaskExecutionAgent(ChatClient chatClient,
      String promptTemplate,
      @Nullable Type responseType,
      @Nullable Function<Request, Map<String, Object>> promptTemplateContextProvider,
      @Nullable Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater,
      @Nullable String name,
      @Nullable ObservationRegistry observationRegistry
  ) {
    super(chatClient, observationRegistry);
    this.promptTemplate = promptTemplate;
    this.responseType = responseType;
    this.promptTemplateContextProvider = promptTemplateContextProvider;
    this.chatClientRequestSpecUpdater = chatClientRequestSpecUpdater;
    if (name != null) {
      this.name = name;
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
    var observationContext = new AgentExecutionObservationContext(getName(), request);
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
    var template = getPromptTemplate();
    if (template.isBlank()) {
      throw new AgentExecutionException("Blank prompt template");
    }
    var type = responseType != null ? responseType : Object.class;
    var context = Optional.ofNullable(getPromptContext(request)).map(HashMap::new).orElseGet(
        HashMap::new);
    var requestSpec = chatClient.prompt().user(userSpec -> userSpec.text(template)
        .params(context));
    updateChatClientRequest(requestSpec);
    var responseSpec = requestSpec.call();
    Response output;
    if (type instanceof Class<?> clazz) {
      output = (Response) responseSpec.entity(clazz);
    } else if (type instanceof ParameterizedType parameterizedType) {
      output = responseSpec.entity(ParameterizedTypeReference.forType(parameterizedType));
    } else {
      throw new AgentExecutionException("Invalid type " + type);
    }
    if (output == null) {
      throw new RuntimeException("Empty or bad response from LLM");
    }
    return output;
  }

  public static <Req, Res> DefaultTaskExecutionAgentBuilder<Req, Res> defaultBuilder() {
    return new DefaultTaskExecutionAgentBuilder<>();
  }

  interface Builder<Request, Response, T extends TaskExecutionAgent.Builder<Request, Response, T>> {

    T chatClient(ChatClient chatClient);

    T promptTemplate(String promptTemplate);

    T name(String agentName);

    T responseType(Type responseType);

    T promptTemplateContextProvider(
        Function<Request, Map<String, Object>> promptTemplateContextProvider);

    T chatClientRequestSpecUpdater(
        Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater);

    T observationRegistry(ObservationRegistry observationRegistry);

    TaskExecutionAgent<Request, Response> build();
  }

}
