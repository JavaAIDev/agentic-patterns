package com.javaaidev.agenticpatterns.examples.chainworkflow;

import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingRequest;
import com.javaaidev.agenticpatterns.examples.chainworkflow.ArticleWritingAgent.ArticleWritingResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/article_writing")
public class ArticleWritingController {

  private final ArticleWritingAgent agent;

  public ArticleWritingController(ArticleWritingAgent agent) {
    this.agent = agent;
  }

  @PostMapping
  public ArticleWritingResponse articleWrite(@RequestBody ArticleWritingRequest request) {
    return agent.call(request);
  }
}
