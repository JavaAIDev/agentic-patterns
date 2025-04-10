package com.javaaidev.agenticpatterns.examples.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.BooleanEvaluationResult;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.EvaluationStep.EvaluationInput;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.EvaluatorOptimizerWorkflow;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.NoopFinalizationStep;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.NoopInitializationStep;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.OptimizationStep.OptimizationInput;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import java.util.Objects;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeGenerationConfiguration {

  @Bean
  public EvaluatorOptimizerWorkflow<CodeGenerationRequest, CodeGenerationRequest, CodeGenerationResponse, BooleanEvaluationResult, CodeGenerationResponse> codeGenerationWorkflow(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry
  ) {
    var chatClient = chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build();
    return EvaluatorOptimizerWorkflow.<CodeGenerationRequest, CodeGenerationRequest, CodeGenerationResponse, BooleanEvaluationResult, CodeGenerationResponse>builder()
        .initializationStep(new NoopInitializationStep<>())
        .initialResultGenerationStep(
            TaskExecutionAgent.<CodeGenerationRequest, CodeGenerationResponse>builder()
                .agentName("CodeGeneration_InitialResult")
                .chatClient(chatClient)
                .observationRegistry(observationRegistry)
                .responseType(CodeGenerationResponse.class)
                .promptTemplate(AgentUtils.loadPromptTemplateFromClasspath(
                    "prompt_template/code-generator/initial-result.st"))
                .build())
        .evaluationStep(
            TaskExecutionAgent.<EvaluationInput<CodeGenerationRequest, CodeGenerationResponse>, BooleanEvaluationResult>builder()
                .agentName("CodeGeneration_Evaluation")
                .chatClient(chatClient)
                .observationRegistry(observationRegistry)
                .responseType(BooleanEvaluationResult.class)
                .promptTemplate(AgentUtils.loadPromptTemplateFromClasspath(
                    "prompt_template/code-generator/evaluation.st"))
                .promptTemplateContextProvider(evaluationInput -> Map.of(
                    "code", evaluationInput.genOutput().code()
                ))
                .build())
        .optimizationStep(
            TaskExecutionAgent.<OptimizationInput<CodeGenerationRequest, CodeGenerationResponse, BooleanEvaluationResult>, CodeGenerationResponse>builder()
                .agentName("CodeGeneration_Optimization")
                .chatClient(chatClient)
                .observationRegistry(observationRegistry)
                .responseType(CodeGenerationResponse.class)
                .promptTemplate(AgentUtils.loadPromptTemplateFromClasspath(
                    "prompt_template/code-generator/optimization.st"))
                .promptTemplateContextProvider(optimizationInput -> Map.of(
                    "code", optimizationInput.genOutput().code(),
                    "feedback",
                    Objects.requireNonNullElse(optimizationInput.evaluationResult().feedback(), "")
                ))
                .build())
        .finalizationStep(new NoopFinalizationStep<>())
        .evaluationPredicate(BooleanEvaluationResult::passed)
        .name("CodeGeneration")
        .observationRegistry(observationRegistry)
        .build();
  }

  public record CodeGenerationRequest(String input) {

  }

  public record CodeGenerationResponse(String code) {

  }
}
