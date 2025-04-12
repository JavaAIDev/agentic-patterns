package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.parallelizationworkflow.DefaultResponseAssembler.AssemblingInput;
import com.javaaidev.agenticpatterns.taskexecution.AbstractTaskExecutionAgentBuilder;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

/**
 * A {@linkplain ResponseAssembler} implemented using {@linkplain TaskExecutionAgent}
 *
 * @param <Request>
 * @param <Response>
 */
public class DefaultResponseAssembler<Request, Response> extends
    TaskExecutionAgent<AssemblingInput<Request>, Response> implements
    ResponseAssembler<Request, Response> {

  public DefaultResponseAssembler(ChatClient chatClient,
      String promptTemplate, @Nullable Type responseType,
      @Nullable Function<AssemblingInput<Request>, Map<String, Object>> promptTemplateContextProvider,
      @Nullable Consumer<ChatClientRequestSpec> chatClientRequestSpecUpdater,
      @Nullable String name,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, promptTemplate, responseType, promptTemplateContextProvider,
        chatClientRequestSpecUpdater, name, observationRegistry);
  }

  @Override
  public Response assemble(@Nullable Request request, TaskExecutionResults results) {
    return this.call(new AssemblingInput<>(request, results));
  }

  public static <Request, Response> Builder<Request, Response> builder() {
    return new Builder<>();
  }

  public record AssemblingInput<Request>(@Nullable Request request,
                                         TaskExecutionResults results) {

  }

  public static class Builder<Request, Response> extends
      AbstractTaskExecutionAgentBuilder<AssemblingInput<Request>, Response, Builder<Request, Response>> {

    @Override
    public DefaultResponseAssembler<Request, Response> build() {
      return new DefaultResponseAssembler<>(
          chatClient,
          promptTemplate,
          responseType,
          promptTemplateContextProvider,
          chatClientRequestSpecUpdater,
          name,
          observationRegistry
      );
    }
  }
}
