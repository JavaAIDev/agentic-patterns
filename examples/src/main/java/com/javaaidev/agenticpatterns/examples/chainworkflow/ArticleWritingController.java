package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.core.AgenticWorkflow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/article_writing")
public class ArticleWritingController {

  private final AgenticWorkflow<ArticleWritingRequest, ArticleWritingResponse> workflow;

  public ArticleWritingController(
      @Qualifier("articleWritingWorkflow") AgenticWorkflow<ArticleWritingRequest, ArticleWritingResponse> workflow) {
    this.workflow = workflow;
  }

  @PostMapping
  public ArticleWritingResponse articleWrite(
      @RequestBody ArticleWritingRequest request) {
    return workflow.execute(request);
  }
}
