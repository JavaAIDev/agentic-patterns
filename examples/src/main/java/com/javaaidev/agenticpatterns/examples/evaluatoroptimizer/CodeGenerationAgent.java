package com.javaaidev.agenticpatterns.examples.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.PromptBasedEvaluatorOptimizerAgent;
import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationAgent.CodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationAgent.CodeGenerationResponse;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

/**
 * An agent to generate code with evaluation feedbacks
 */
public class CodeGenerationAgent extends
    PromptBasedEvaluatorOptimizerAgent<CodeGenerationRequest, CodeGenerationResponse> {

  public CodeGenerationAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, chatClient, CodeGenerationResponse.class, observationRegistry);
  }

  @Override
  protected String getInitialResultPromptTemplate() {
    return AgentUtils.loadPromptTemplateFromClasspath(
        "prompt_template/code-generator/initial-result.st");
  }

  @Override
  protected @Nullable Map<String, Object> buildInitialResultPromptContext(
      @Nullable CodeGenerationRequest request) {
    return Map.of("input",
        AgentUtils.safeGet(request, CodeGenerationRequest::input, ""));
  }

  @Override
  protected String getEvaluationPromptTemplate() {
    return AgentUtils.loadPromptTemplateFromClasspath(
        "prompt_template/code-generator/evaluation.st");
  }

  @Override
  protected @Nullable Map<String, Object> buildEvaluationPromptContext(
      @Nullable CodeGenerationResponse codeGenerationResponse) {
    return Map.of("code",
        AgentUtils.safeGet(codeGenerationResponse, CodeGenerationResponse::code, ""));
  }

  @Override
  protected String getOptimizationPromptTemplate() {
    return AgentUtils.loadPromptTemplateFromClasspath(
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
