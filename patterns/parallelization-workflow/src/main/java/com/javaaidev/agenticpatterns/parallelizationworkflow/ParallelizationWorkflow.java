package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflow;
import com.javaaidev.agenticpatterns.core.AbstractAgenticWorkflowBuilder;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelizationWorkflow<Request, Response> extends
    AbstractAgenticWorkflow<Request, Response> {

  private List<SubtaskContext> subtasks;
  private ResponseAssembler<Request, Response> responseAssembler;
  private Function<Request, @Nullable List<SubtaskCreationRequest<Request>>> subtasksCreator;
  private Duration maxTaskExecutionDuration;
  private ExecutorService taskExecutorService;

  private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizationWorkflow.class);

  public ParallelizationWorkflow(
      List<SubtaskContext> subtasks,
      ResponseAssembler<Request, Response> responseAssembler,
      @Nullable Function<Request, List<SubtaskCreationRequest<Request>>> subtasksCreator,
      @Nullable Duration maxTaskExecutionDuration,
      @Nullable ExecutorService taskExecutorService,
      @Nullable String name,
      @Nullable ObservationRegistry observationRegistry) {
    super(name, observationRegistry);
    this.subtasks = Objects.requireNonNull(subtasks, "Subtasks cannot be null");
    this.responseAssembler = Objects.requireNonNull(responseAssembler,
        "ResponseAssembler cannot be null");
    this.subtasksCreator = Objects.requireNonNullElse(subtasksCreator, (request) -> List.of());
    this.maxTaskExecutionDuration = Objects.requireNonNullElse(maxTaskExecutionDuration,
        Duration.ofMinutes(3));
    this.taskExecutorService = Objects.requireNonNullElseGet(taskExecutorService,
        this::getDefaultTaskExecutorService);
  }

  private ExecutorService getDefaultTaskExecutorService() {
    var executor = Executors.newThreadPerTaskExecutor(
        Thread.ofVirtual().name(name + "-task-", 1).factory());
    return ContextExecutorService.wrap(executor,
        ContextSnapshotFactory.builder().clearMissing(true).build());
  }

  @Override
  protected Response doExecute(@Nullable Request request) {
    var subtaskResults = runSubtasks(request);
    return responseAssembler.assemble(request, subtaskResults);
  }

  protected TaskExecutionResults runSubtasks(@Nullable Request request) {
    var createdTasks = subtasksCreator.apply(request);
    if (createdTasks != null) {
      subtasks.addAll(createdTasks.stream().map(SubtaskContext::create).toList());
    }
    LOGGER.info("{} subtasks to run", subtasks.size());
    var jobs = subtasks.stream().map(context -> {
      var creationRequest = context.creationRequest();
      LOGGER.info("Starting subtask {}", creationRequest.taskId());
      var job = taskExecutorService.submit(
          () -> creationRequest.task().call(creationRequest.requestTransformer().apply(request)));
      return context.taskStarted(job, maxTaskExecutionDuration);
    }).toList();
    LOGGER.info("Waiting for all subtasks to finish");
    jobs.forEach(SubtaskContext::collectResult);
    LOGGER.info("All subtasks completed, assembling the results");
    var results = jobs.stream().map(SubtaskContext::collectResult)
        .collect(Collectors.toMap(SubtaskContext::taskId,
            (task -> new SubtaskResult(task.result(), task.error())), (a, b) -> b));
    return new TaskExecutionResults(results);
  }

  public static <Request, Response> Builder<Request, Response> builder() {
    return new Builder<>();
  }

  public static class Builder<Request, Response> extends
      AbstractAgenticWorkflowBuilder<Request, Response, Builder<Request, Response>> {

    private List<SubtaskContext> subtasks = new ArrayList<>();
    private ResponseAssembler<Request, Response> responseAssembler;
    private Function<Request, List<SubtaskCreationRequest<Request>>> subtasksCreator;
    private Duration maxTaskExecutionDuration;
    private ExecutorService taskExecutorService;

    public Builder<Request, Response> addSubtask(SubtaskContext subtask) {
      subtasks.add(Objects.requireNonNull(subtask, "subtask cannot be null"));
      return this;
    }

    /**
     * Add new subtask
     *
     * @param taskId             Task id
     * @param subtask            Subtask implemented as {@linkplain TaskExecutionAgent}
     * @param requestTransformer Transform request to task's input
     * @param <TaskRequest>      Task input type
     * @param <TaskResponse>     Task output type
     */
    public <TaskRequest, TaskResponse> Builder<Request, Response> addSubtask(String taskId,
        TaskExecutionAgent<TaskRequest, TaskResponse> subtask,
        Function<Request, TaskRequest> requestTransformer) {
      subtasks.add(SubtaskContext.create(taskId, subtask, requestTransformer));
      return this;
    }

    public Builder<Request, Response> subtasksCreator(
        Function<Request, List<SubtaskCreationRequest<Request>>> subtasksCreator) {
      this.subtasksCreator = Objects.requireNonNull(subtasksCreator,
          "subtasksCreator cannot be null");
      return this;
    }

    public Builder<Request, Response> responseAssembler(
        ResponseAssembler<Request, Response> responseAssembler) {
      this.responseAssembler = Objects.requireNonNull(responseAssembler,
          "responseAssembler cannot be null");
      return this;
    }

    public Builder<Request, Response> maxTaskExecutionDuration(
        Duration maxTaskExecutionDuration) {
      this.maxTaskExecutionDuration = Objects.requireNonNull(maxTaskExecutionDuration,
          "maxTaskExecutionDuration cannot be null");
      return this;
    }

    public Builder<Request, Response> taskExecutorService(
        ExecutorService taskExecutorService) {
      this.taskExecutorService = Objects.requireNonNull(taskExecutorService,
          "taskExecutorService cannot be null");
      return this;
    }

    @Override
    public ParallelizationWorkflow<Request, Response> build() {
      return new ParallelizationWorkflow<>(
          subtasks,
          responseAssembler,
          subtasksCreator,
          maxTaskExecutionDuration,
          taskExecutorService,
          name,
          observationRegistry
      );
    }
  }
}
