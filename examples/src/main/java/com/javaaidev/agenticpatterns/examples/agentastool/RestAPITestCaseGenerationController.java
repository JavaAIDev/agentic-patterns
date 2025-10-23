package com.javaaidev.agenticpatterns.examples.agentastool;

import com.javaaidev.agenticpatterns.examples.agentastool.RestAPITestCaseGenerationAgent.RestAPITestCaseGenerationResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restapi_testcase")
public class RestAPITestCaseGenerationController {

  private final RestAPITestCaseGenerationAgent agent;

  public RestAPITestCaseGenerationController(
      RestAPITestCaseGenerationAgent agent) {
    this.agent = agent;
  }

  @PostMapping
  public RestAPITestCaseGenerationResponse generateTestCase() {
    return agent.call(null);
  }
}
