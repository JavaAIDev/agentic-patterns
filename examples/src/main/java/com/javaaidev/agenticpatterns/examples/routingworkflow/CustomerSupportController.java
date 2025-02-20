package com.javaaidev.agenticpatterns.examples.routingworkflow;

import com.javaaidev.agenticpatterns.examples.routingworkflow.CustomerSupportAgent.CustomerSupportRequest;
import com.javaaidev.agenticpatterns.examples.routingworkflow.CustomerSupportAgent.CustomerSupportResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer_support")
public class CustomerSupportController {

  private final CustomerSupportAgent customerSupportAgent;

  public CustomerSupportController(CustomerSupportAgent customerSupportAgent) {
    this.customerSupportAgent = customerSupportAgent;
  }

  @PostMapping
  public CustomerSupportResponse customerSupport(@RequestBody CustomerSupportRequest request) {
    return customerSupportAgent.call(request);
  }
}
