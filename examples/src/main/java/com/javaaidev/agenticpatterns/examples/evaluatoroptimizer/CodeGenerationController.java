package com.javaaidev.agenticpatterns.examples.evaluatoroptimizer;

import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationAgent.CodeGenerationRequest;
import com.javaaidev.agenticpatterns.examples.evaluatoroptimizer.CodeGenerationAgent.CodeGenerationResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/code_generation")
public class CodeGenerationController {

  private final CodeGenerationAgent codeGenerationAgent;

  public CodeGenerationController(CodeGenerationAgent codeGenerationAgent) {
    this.codeGenerationAgent = codeGenerationAgent;
  }

  @PostMapping
  public CodeGenerationResponse generateCode(@RequestBody CodeGenerationRequest request) {
    return codeGenerationAgent.call(request);
  }
}
