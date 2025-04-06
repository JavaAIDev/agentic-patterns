package com.javaaidev.agenticpatterns.examples.routingworkflow;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.examples.routingworkflow.CustomerSupportAgent.CustomerSupportRequest;
import com.javaaidev.agenticpatterns.examples.routingworkflow.CustomerSupportAgent.CustomerSupportResponse;
import com.javaaidev.agenticpatterns.routingworkflow.RoutingChoice;
import com.javaaidev.agenticpatterns.routingworkflow.RoutingWorkflowAgent;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

/**
 * A routing workflow agent for customer support
 */
public class CustomerSupportAgent extends
    RoutingWorkflowAgent<CustomerSupportRequest, CustomerSupportResponse> {

  public CustomerSupportAgent(ChatClient chatClient,
      @Nullable Type responseType, @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
    initRoutes();
  }

  public CustomerSupportAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
    initRoutes();
  }

  private void initRoutes() {
    addRoutingChoice(new RoutingChoice<>("payment", "Handle queries about payment and refund",
        new PaymentSupportAgent(chatClient, observationRegistry)));
    addRoutingChoice(new RoutingChoice<>("shipping", "Handle queries about shipping",
        new ShippingSupportAgent(chatClient, observationRegistry)));
    addRoutingChoice(new RoutingChoice<>("general", "Handle general queries",
        new GeneralSupportAgent(chatClient, observationRegistry)));
  }

  @Override
  protected String formatRoutingInput(@Nullable CustomerSupportRequest request) {
    return AgentUtils.safeGet(request, CustomerSupportRequest::question, "");
  }

  public record CustomerSupportRequest(String question) {

  }

  public record CustomerSupportResponse(String answer) {

  }

  private static class PaymentSupportAgent extends
      TaskExecutionAgent<CustomerSupportRequest, CustomerSupportResponse> {

    protected PaymentSupportAgent(ChatClient chatClient,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, observationRegistry);
    }

    @Override
    protected String getPromptTemplate() {
      return "{question}";
    }

    @Override
    protected void updateChatClientRequest(ChatClientRequestSpec spec) {
      spec.system("You are a customer support agent for payment, be polite and helpful");
    }
  }

  private static class ShippingSupportAgent extends
      TaskExecutionAgent<CustomerSupportRequest, CustomerSupportResponse> {

    protected ShippingSupportAgent(ChatClient chatClient,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, observationRegistry);
    }

    @Override
    protected String getPromptTemplate() {
      return "{question}";
    }

    @Override
    protected void updateChatClientRequest(ChatClientRequestSpec spec) {
      spec.system("You are a customer support agent for shipping, be polite and helpful");
    }
  }

  private static class GeneralSupportAgent extends
      TaskExecutionAgent<CustomerSupportRequest, CustomerSupportResponse> {

    protected GeneralSupportAgent(ChatClient chatClient,
        @Nullable ObservationRegistry observationRegistry) {
      super(chatClient, observationRegistry);
    }

    @Override
    protected String getPromptTemplate() {
      return "{question}";
    }

    @Override
    protected void updateChatClientRequest(ChatClientRequestSpec spec) {
      spec.system(
          "You are a customer support agent for general questions, be polite and helpful");
    }
  }
}
