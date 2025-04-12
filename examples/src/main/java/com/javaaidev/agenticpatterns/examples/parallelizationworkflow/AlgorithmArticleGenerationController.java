package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.parallelizationworkflow.ParallelizationWorkflow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/algorithm_article")
public class AlgorithmArticleGenerationController {

  private final ParallelizationWorkflow<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse> workflow;

  public AlgorithmArticleGenerationController(
      @Qualifier("algorithmArticleGenerationWorkflow") ParallelizationWorkflow<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse> workflow) {
    this.workflow = workflow;
  }


  @PostMapping
  public AlgorithmArticleGenerationResponse generateAlgorithmArticle(
      @RequestBody AlgorithmArticleGenerationRequest request) {
    return workflow.execute(request);
  }
}
