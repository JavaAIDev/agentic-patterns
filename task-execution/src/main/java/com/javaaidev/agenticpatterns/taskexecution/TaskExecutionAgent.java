package com.javaaidev.agenticpatterns.taskexecution;

import com.javaaidev.agenticpatterns.core.Agent;
import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import com.javaaidev.agenticpatterns.core.TypeResolver;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Task Execution Agent, refer to the <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/task-execution">pattern</a>
 *
 * @param <Request>  Type of task input
 * @param <Response> Type of task output
 */
public abstract class TaskExecutionAgent<Request, Response> extends Agent {

  /**
   * Get the prompt template
   *
   * @return prompt template
   */
  protected abstract String getPromptTemplate();

  @Nullable
  protected final Type responseType;

  protected TaskExecutionAgent() {
    responseType = TypeResolver.resolveType(this.getClass(), TaskExecutionAgent.class, 1);
  }

  protected TaskExecutionAgent(@Nullable Type responseType) {
    this.responseType = responseType;
  }

  /**
   * Prepare for the values of variables in the prompt template
   *
   * @param request Task input
   * @return Values of values
   */
  @Nullable
  protected Map<String, Object> getPromptContext(@Nullable Request request) {
    return new HashMap<>();
  }

  protected void updateRequest(ChatClientRequestSpec spec) {
  }

  public Response call(@Nullable Request request) {
    var template = getPromptTemplate();
    if (template.isBlank()) {
      throw new AgentExecutionException("Blank prompt template");
    }
    var type = responseType != null ? responseType : Object.class;
    var context = Optional.ofNullable(getPromptContext(request)).map(HashMap::new).orElseGet(
        HashMap::new);
    var requestSpec = getChatClient().prompt().user(userSpec -> userSpec.text(template)
        .params(context));
    updateRequest(requestSpec);
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

}
