package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.AlgorithmArticleGenerationAgent.AlgorithmArticleGenerationRequest;
import com.javaaidev.agenticpatterns.examples.parallelizationworkflow.AlgorithmArticleGenerationAgent.AlgorithmArticleGenerationResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/algorithm_article")
public class AlgorithmArticleGenerationController {

  private final AlgorithmArticleGenerationAgent agent;

  public AlgorithmArticleGenerationController(AlgorithmArticleGenerationAgent agent) {
    this.agent = agent;
  }

  @PostMapping
  public AlgorithmArticleGenerationResponse generateAlgorithmArticle(
      @RequestBody AlgorithmArticleGenerationRequest request) {
    return agent.call(request);
  }
}
