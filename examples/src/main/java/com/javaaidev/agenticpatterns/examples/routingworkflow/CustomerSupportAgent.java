package com.javaaidev.agenticpatterns.examples.routingworkflow;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.examples.routingworkflow.CustomerSupportAgent.CustomerSupportRequest;
import com.javaaidev.agenticpatterns.examples.routingworkflow.CustomerSupportAgent.CustomerSupportResponse;
import com.javaaidev.agenticpatterns.routingworkflow.RoutingWorkflowAgent;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

public class CustomerSupportAgent extends
    RoutingWorkflowAgent<CustomerSupportRequest, CustomerSupportResponse> {

  protected CustomerSupportAgent(ChatClient chatClient,
      @Nullable Type responseType) {
    super(chatClient, responseType);
    initRoutes();
  }

  protected CustomerSupportAgent(ChatClient chatClient) {
    super(chatClient);
    initRoutes();
  }

  private void initRoutes() {
    addRoutingChoice(new RoutingChoice<>("payment", "Handle queries about payment and refund",
        new PaymentSupportAgent(chatClient)));
    addRoutingChoice(new RoutingChoice<>("shipping", "Handle queries about shipping",
        new ShippingSupportAgent(chatClient)));
    addRoutingChoice(new RoutingChoice<>("general", "Handle general queries",
        new GeneralSupportAgent(chatClient)));
  }

  @Override
  protected String formatRequest(@Nullable CustomerSupportRequest request) {
    return AgentUtils.safeGet(request, CustomerSupportRequest::question, "");
  }

  public record CustomerSupportRequest(String question) {

  }

  public record CustomerSupportResponse(String answer) {

  }

  private static class PaymentSupportAgent extends
      TaskExecutionAgent<CustomerSupportRequest, CustomerSupportResponse> {

    protected PaymentSupportAgent(ChatClient chatClient) {
      super(chatClient);
    }

    @Override
    protected String getPromptTemplate() {
      return "{question}";
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(
        @Nullable CustomerSupportRequest customerSupportRequest) {
      return Map.of(
          "question",
          AgentUtils.safeGet(customerSupportRequest, CustomerSupportRequest::question, "")
      );
    }

    @Override
    protected void updateRequest(ChatClientRequestSpec spec) {
      spec.system("You are a customer support agent for payment, be polite and helper");
    }
  }

  private static class ShippingSupportAgent extends
      TaskExecutionAgent<CustomerSupportRequest, CustomerSupportResponse> {

    protected ShippingSupportAgent(ChatClient chatClient) {
      super(chatClient);
    }

    @Override
    protected String getPromptTemplate() {
      return "{question}";
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(
        @Nullable CustomerSupportRequest customerSupportRequest) {
      return Map.of(
          "question",
          AgentUtils.safeGet(customerSupportRequest, CustomerSupportRequest::question, "")
      );
    }

    @Override
    protected void updateRequest(ChatClientRequestSpec spec) {
      spec.system("You are a customer support agent for shipping, be polite and helper");
    }
  }

  private static class GeneralSupportAgent extends
      TaskExecutionAgent<CustomerSupportRequest, CustomerSupportResponse> {

    protected GeneralSupportAgent(ChatClient chatClient) {
      super(chatClient);
    }

    @Override
    protected String getPromptTemplate() {
      return "{question}";
    }

    @Override
    protected @Nullable Map<String, Object> getPromptContext(
        @Nullable CustomerSupportRequest customerSupportRequest) {
      return Map.of(
          "question",
          AgentUtils.safeGet(customerSupportRequest, CustomerSupportRequest::question, "")
      );
    }

    @Override
    protected void updateRequest(ChatClientRequestSpec spec) {
      spec.system(
          "You are a customer support agent for general questions, be polite and helper");
    }
  }
}
