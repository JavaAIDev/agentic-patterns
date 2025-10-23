package com.javaaidev.agenticpatterns.examples.routingworkflow;

import com.javaaidev.agenticpatterns.routingworkflow.RoutingWorkflow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer_support")
public class CustomerSupportController {

  private final RoutingWorkflow<CustomerSupportRequest, CustomerSupportResponse> customerSupportWorkflow;

  public CustomerSupportController(
      @Qualifier("customerSupportWorkflow") RoutingWorkflow<CustomerSupportRequest, CustomerSupportResponse> customerSupportWorkflow) {
    this.customerSupportWorkflow = customerSupportWorkflow;
  }


  @PostMapping
  public CustomerSupportResponse customerSupport(@RequestBody CustomerSupportRequest request) {
    return customerSupportWorkflow.execute(request);
  }
}
