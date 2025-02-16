package com.javaaidev.agenticpatterns.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public abstract class PromptBasedEvaluatorOptimizerAgent<Request, Response> extends
    EvaluatorOptimizerAgent<Request, Response> {

  protected abstract ChatClient getGenerationChatClient();

  protected abstract ChatClient getEvaluationChatClient();

  protected abstract String getInitialResultPromptTemplate();

  protected @Nullable Map<String, Object> buildInitialResultPromptContext(
      @Nullable Request request) {
    return new HashMap<>();
  }

  @Override
  protected TaskExecutionAgent<Request, Response> buildInitialResultAgent() {
    return new TaskExecutionAgent<>() {
      @Override
      protected String getPromptTemplate() {
        return getInitialResultPromptTemplate();
      }

      @Override
      protected @Nullable Map<String, Object> getPromptContext(@Nullable Request request) {
        return buildInitialResultPromptContext(request);
      }

      @Override
      protected ChatClient getChatClient() {
        return getGenerationChatClient();
      }
    };
  }

  protected abstract String getEvaluationPromptTemplate();

  protected @Nullable Map<String, Object> buildEvaluationPromptContext(
      @Nullable Response response) {
    return new HashMap<>();
  }

  @Override
  protected TaskExecutionAgent<Response, Evaluation> buildEvaluationAgent() {
    return new TaskExecutionAgent<>() {
      @Override
      protected String getPromptTemplate() {
        return getEvaluationPromptTemplate();
      }

      @Override
      protected @Nullable Map<String, Object> getPromptContext(@Nullable Response response) {
        return buildEvaluationPromptContext(response);
      }

      @Override
      protected ChatClient getChatClient() {
        return getEvaluationChatClient();
      }
    };
  }

  protected abstract String getOptimizationPromptTemplate();

  protected @Nullable Map<String, Object> buildOptimizationPromptContext(
      @Nullable OptimizationInput<Response> optimizationInput) {
    return new HashMap<>();
  }

  @Override
  protected TaskExecutionAgent<OptimizationInput<Response>, Response> buildOptimizationAgent() {
    return new TaskExecutionAgent<>() {
      @Override
      protected String getPromptTemplate() {
        return getOptimizationPromptTemplate();
      }

      @Override
      protected @Nullable Map<String, Object> getPromptContext(
          @Nullable OptimizationInput<Response> responseOptimizationInput) {
        return buildOptimizationPromptContext(responseOptimizationInput);
      }

      @Override
      protected ChatClient getChatClient() {
        return getGenerationChatClient();
      }
    };
  }
}
