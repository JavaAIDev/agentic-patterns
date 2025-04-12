package com.javaaidev.agenticpatterns.chainworkflow;

import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflow;
import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflowBuilder;
import io.micrometer.observation.ObservationRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A chain workflow
 *
 * @param <Request>
 * @param <Response>
 */
public class ChainWorkflow<Request, Response> extends AbstractAgenticWorkflow<Request, Response> {

  private final List<ChainStep<Request, Response>> steps;

  ChainWorkflow(List<ChainStep<Request, Response>> steps,
      @Nullable String workflowName,
      @Nullable ObservationRegistry observationRegistry) {
    super(workflowName, observationRegistry);
    this.steps = new ArrayList<>(steps);
  }

  @Override
  protected Response doExecute(@Nullable Request request) {
    var chain = new WorkflowChain<>(steps);
    return chain.callNext(request, null);
  }

  public static <Req, Res> Builder<Req, Res> builder() {
    return new Builder<>();
  }

  public static final class Builder<Request, Response> extends
      AbstractAgenticWorkflowBuilder<Request, Response, Builder<Request, Response>> {

    private final List<ChainStep<Request, Response>> steps = new ArrayList<>();

    public Builder<Request, Response> addStep(ChainStep<Request, Response> step) {
      steps.add(Objects.requireNonNull(step, "Chain step cannot be null"));
      return this;
    }

    public Builder<Request, Response> addStepAgent(
        ChainStepAgent<Request, Response> agent) {
      steps.add(Objects.requireNonNull(agent, "Chain step agent cannot be null"));
      return this;
    }

    public ChainWorkflow<Request, Response> build() {
      return new ChainWorkflow<>(steps, name, observationRegistry);
    }
  }
}
