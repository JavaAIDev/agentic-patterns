package com.javaaidev.agenticpatterns.examples.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.evaluatoroptimizer.BooleanEvaluationResult;
import com.javaaidev.agenticpatterns.evaluatoroptimizer.EvaluatorOptimizerWorkflow;
import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationConfiguration.CodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationConfiguration.CodeGenerationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/code_generation")
public class CodeGenerationController {

  private final EvaluatorOptimizerWorkflow<CodeGenerationRequest, CodeGenerationRequest, CodeGenerationResponse, BooleanEvaluationResult, CodeGenerationResponse> evaluatorOptimizerWorkflow;

  public CodeGenerationController(
      @Qualifier("codeGenerationWorkflow") EvaluatorOptimizerWorkflow<CodeGenerationRequest, CodeGenerationRequest, CodeGenerationResponse, BooleanEvaluationResult, CodeGenerationResponse> codeGenerationAgent) {
    this.evaluatorOptimizerWorkflow = codeGenerationAgent;
  }

  @PostMapping
  public CodeGenerationResponse generateCode(@RequestBody CodeGenerationRequest request) {
    return evaluatorOptimizerWorkflow.execute(request);
  }
}
