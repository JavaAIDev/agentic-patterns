package com.javaaidev.agenticpatterns.taskexecution;

import com.javaaidev.agenticpatterns.core.Agent;
import com.javaaidev.agenticpatterns.core.AgentExecutionException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;

public abstract class TaskExecutionAgent<Request, Response> extends Agent {

  protected abstract String getPromptTemplate();

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
    var type = getResponseType();
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

  private Type getResponseType() {
    var agentType = ResolvableType.forClass(this.getClass()).as(TaskExecutionAgent.class);
    var types = agentType.getGenerics();
    if (types.length < 2) {
      throw new AgentExecutionException("Wrong type");
    }
    var type = types[1].getType();
    if (type instanceof TypeVariable<?> typeVariable) {
      var bounds = typeVariable.getBounds();
      return bounds.length > 0 ? bounds[0] : Object.class;
    }
    return type;
  }
}
