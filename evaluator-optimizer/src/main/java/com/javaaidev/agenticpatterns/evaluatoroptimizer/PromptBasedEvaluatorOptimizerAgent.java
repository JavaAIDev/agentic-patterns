package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.core.TypeResolver;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public abstract class PromptBasedEvaluatorOptimizerAgent<Request, Response> extends
    EvaluatorOptimizerAgent<Request, Response> {

  @Nullable
  protected Type responseType = null;

  private void tryResolveType() {
    responseType = TypeResolver.resolveType(this.getClass(),
        PromptBasedEvaluatorOptimizerAgent.class,
        1);
  }

  protected abstract ChatClient getGenerationChatClient();

  protected abstract ChatClient getEvaluationChatClient();

  protected abstract String getInitialResultPromptTemplate();

  protected @Nullable Map<String, Object> buildInitialResultPromptContext(
      @Nullable Request request) {
    return new HashMap<>();
  }

  public class GenerateInitialResultAgent extends TaskExecutionAgent<Request, Response> {

    public GenerateInitialResultAgent(@Nullable Type responseType) {
      super(getGenerationChatClient(), responseType);
    }

    @Override
    protected String getPromptTemplate() {
      return getInitialResultPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
      return buildInitialResultPromptContext(request);
    }
  }

  @Override
  protected TaskExecutionAgent<Request, Response> buildInitialResultAgent() {
    tryResolveType();
    return new GenerateInitialResultAgent(responseType);
  }

  protected abstract String getEvaluationPromptTemplate();

  protected @Nullable Map<String, Object> buildEvaluationPromptContext(
      @Nullable Response response) {
    return new HashMap<>();
  }

  public class EvaluateAgent extends TaskExecutionAgent<Response, Evaluation> {


    protected EvaluateAgent() {
      super(getEvaluationChatClient());
    }

    @Override
    protected String getPromptTemplate() {
      return getEvaluationPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(@Nullable Response response) {
      return buildEvaluationPromptContext(response);
    }
  }

  @Override
  protected TaskExecutionAgent<Response, Evaluation> buildEvaluationAgent() {
    return new EvaluateAgent();
  }

  protected abstract String getOptimizationPromptTemplate();

  protected @Nullable Map<String, Object> buildOptimizationPromptContext(
      @Nullable OptimizationInput<Response> optimizationInput) {
    return new HashMap<>();
  }

  public class OptimizeAgent extends TaskExecutionAgent<OptimizationInput<Response>, Response> {

    public OptimizeAgent(@Nullable Type responseType) {
      super(getGenerationChatClient(), responseType);
    }

    @Override
    protected String getPromptTemplate() {
      return getOptimizationPromptTemplate();
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(
        @Nullable OptimizationInput<Response> responseOptimizationInput) {
      return buildOptimizationPromptContext(responseOptimizationInput);
    }
  }

  @Override
  protected TaskExecutionAgent<OptimizationInput<Response>, Response> buildOptimizationAgent() {
    tryResolveType();
    return new OptimizeAgent(responseType);
  }
}
