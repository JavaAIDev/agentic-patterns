package com.javaaidev.agenticpatterns.chainworkflow;

import java.util.Map;
import org.springframework.core.Ordered;

public interface ChainStep<Request, Response> extends Ordered {

  /**
   * Call the current step
   *
   * @param request Task input
   * @param context Shared context between different steps
   * @param chain   The chain, see {@linkplain WorkflowChain}
   * @return Task output
   */
  Response call(Request request, Map<String, Object> context,
      WorkflowChain<Request, Response> chain);
}
