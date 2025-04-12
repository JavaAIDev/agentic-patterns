package com.javaaidev.agenticpatterns.examples.routingworkflow;

import com.javaaidev.agenticpatterns.routingworkflow.DefaultRoutingSelector;
import com.javaaidev.agenticpatterns.routingworkflow.RoutingChoice;
import com.javaaidev.agenticpatterns.routingworkflow.RoutingWorkflow;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerSupportConfiguration {

  @Bean
  @Qualifier("customerSupportWorkflow")
  public RoutingWorkflow<CustomerSupportRequest, CustomerSupportResponse> customerSupportWorkflow(
      ChatClient.Builder chatClientBuilder,
      SimpleLoggerAdvisor simpleLoggerAdvisor,
      ObservationRegistry observationRegistry
  ) {
    var chatClient = chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor).build();
    var routes = List.of(
        new CustomerSupportRoute("payment", "Handle queries about payment and refund",
            "You are a customer support agent for payment, be polite and helpful"),
        new CustomerSupportRoute("shipping", "Handle queries about shipping",
            "You are a customer support agent for shipping, be polite and helpful"),
        new CustomerSupportRoute("general", "Handle general queries",
            "You are a customer support agent for general questions, be polite and helpful")
    );
    var routingChoices = routes.stream().map(route -> new RoutingChoice<>(
        route.name(),
        route.description(),
        TaskExecutionAgent.<CustomerSupportRequest, CustomerSupportResponse>defaultBuilder()
            .chatClient(chatClient)
            .promptTemplate("{question}")
            .responseType(CustomerSupportResponse.class)
            .chatClientRequestSpecUpdater(spec -> spec.system(route.agentSystemText()))
            .name("CustomerSupport_" + route.name)
            .observationRegistry(observationRegistry)
            .build()
    )).toList();
    return RoutingWorkflow.<CustomerSupportRequest, CustomerSupportResponse>builder()
        .addRoutingChoices(routingChoices)
        .routingSelector(
            DefaultRoutingSelector.<CustomerSupportRequest, CustomerSupportResponse>builder()
                .chatClient(chatClient)
                .name("RoutingSelector")
                .observationRegistry(observationRegistry)
                .build())
        .name("CustomerSupportWorkflow")
        .observationRegistry(observationRegistry)
        .build();
  }

  record CustomerSupportRoute(String name, String description, String agentSystemText) {

  }
}
