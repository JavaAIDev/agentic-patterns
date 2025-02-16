package com.javaaidev.agenticpatterns.examples.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.core.PromptTemplateHelper;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.PromptBasedEvaluatorOptimizerAgent;
import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationAgent.CodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationAgent.CodeGenerationResponse;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

public class CodeGenerationAgent extends
    PromptBasedEvaluatorOptimizerAgent<CodeGenerationRequest, CodeGenerationResponse> {

  private final ChatClient chatClient;

  public CodeGenerationAgent(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @Override
  protected ChatClient getGenerationChatClient() {
    return this.chatClient;
  }

  @Override
  protected ChatClient getEvaluationChatClient() {
    return this.chatClient;
  }

  @Override
  protected String getInitialResultPromptTemplate() {
    return PromptTemplateHelper.loadPromptTemplateFromClasspath(
        "prompt_template/code-generator/initial-result.st");
  }

  @Override
  protected @Nullable Map<String, Object> buildInitialResultPromptContext(
      @Nullable CodeGenerationRequest codeGenerationRequest) {
    var input = Optional.ofNullable(codeGenerationRequest).map(CodeGenerationRequest::input)
        .orElse("");
    return Map.of("input", input);
  }

  @Override
  protected String getEvaluationPromptTemplate() {
    return PromptTemplateHelper.loadPromptTemplateFromClasspath(
        "prompt_template/code-generator/evaluation.st");
  }

  @Override
  protected @Nullable Map<String, Object> buildEvaluationPromptContext(
      @Nullable CodeGenerationResponse codeGenerationResponse) {
    var code = Optional.ofNullable(codeGenerationResponse).map(CodeGenerationResponse::code)
        .orElse("");
    return Map.of("code", code);
  }

  @Override
  protected String getOptimizationPromptTemplate() {
    return PromptTemplateHelper.loadPromptTemplateFromClasspath(
        "prompt_template/code-generator/optimization.st");
  }

  @Override
  protected @Nullable Map<String, Object> buildOptimizationPromptContext(
      @Nullable OptimizationInput<CodeGenerationResponse> optimizationInput) {
    var optionalInput = Optional.ofNullable(optimizationInput);
    return Map.of(
        "code", optionalInput.map(input -> input.response().code()),
        "feedback", optionalInput.map(input -> input.evaluation().feedback())
    );
  }

  public record CodeGenerationRequest(String input) {

  }

  public record CodeGenerationResponse(String code) {

  }
}
