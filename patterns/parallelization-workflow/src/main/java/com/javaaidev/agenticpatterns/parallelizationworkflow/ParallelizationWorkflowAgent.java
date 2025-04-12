package com.javaaidev.agenticpatterns.parallelizationworkflow;

import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Parallelization Workflow agent, refer to <a
 * href="https://javaaidev.com/docs/agentic-patterns/patterns/parallelization-workflow">doc</a>
 *
 * @param <Request>  Type of agent input
 * @param <Response> Type of agent output
 */
public abstract class ParallelizationWorkflowAgent<Request, Response> extends
    TaskExecutionAgent<Request, Response> {

  public ParallelizationWorkflowAgent(ChatClient chatClient,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, observationRegistry);
  }

  public ParallelizationWorkflowAgent(ChatClient chatClient,
      @Nullable Type responseType,
      @Nullable ObservationRegistry observationRegistry) {
    super(chatClient, responseType, observationRegistry);
  }

  protected final CopyOnWriteArrayList<SubtaskContext> subtasks = new CopyOnWriteArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizationWorkflowAgent.class);

  /**
   * Add new subtask
   *
   * @param taskId             Task id
   * @param subtask            Subtask implemented as {@linkplain TaskExecutionAgent}
   * @param requestTransformer Transform request to task's input
   * @param <TaskRequest>      Task input type
   * @param <TaskResponse>     Task output type
   */
  protected <TaskRequest, TaskResponse> void addSubtask(String taskId,
      TaskExecutionAgent<TaskRequest, TaskResponse> subtask,
      Function<Request, TaskRequest> requestTransformer) {
    subtasks.add(SubtaskContext.create(taskId, subtask, requestTransformer));
  }

  /**
   * Max duration of a subtask execution
   *
   * @return Max duration
   */
  protected Duration getMaxTaskExecutionDuration() {
    return Duration.ofMinutes(3);
  }

  /**
   * Create a list of subtasks from request. Subtasks added by
   * {@linkplain #addSubtask(String, TaskExecutionAgent, Function)} will be merged into this list.
   *
   * @param request Request
   * @return Subtasks
   */
  @Nullable
  protected List<SubtaskCreationRequest<Request>> createTasks(@Nullable Request request) {
    return List.of();
  }

  /**
   * Create the {@linkplain ExecutorService} to execute subtasks.
   * <p>
   * The default executor service uses virtual threads.
   *
   * @return executor service
   */
  protected ExecutorService getTaskExecutorService() {
    var executor = Executors.newThreadPerTaskExecutor(
        Thread.ofVirtual().name("agent-task-", 1).factory());
    return ContextExecutorService.wrap(executor,
        ContextSnapshotFactory.builder().clearMissing(true).build());
  }

  protected TaskExecutionResults runSubtasks(@Nullable Request request) {
    var createdTasks = createTasks(request);
    if (createdTasks != null) {
      subtasks.addAll(createdTasks.stream().map(SubtaskContext::create).toList());
    }
    try (var executor = getTaskExecutorService()) {
      var jobs = subtasks.stream().map(context -> {
        var creationRequest = context.creationRequest();
        LOGGER.info("Starting subtask {}", creationRequest.taskId());
        var job = executor.submit(
            () -> creationRequest.task().call(creationRequest.requestTransformer().apply(request)));
        return context.taskStarted(job, getMaxTaskExecutionDuration());
      }).toList();
      LOGGER.info("Waiting for all subtasks to finish");
      jobs.forEach(SubtaskContext::collectResult);
      LOGGER.info("All subtasks completed, assembling the results");
      var results = jobs.stream().map(SubtaskContext::collectResult)
          .collect(Collectors.toMap(SubtaskContext::taskId,
              (task -> new SubtaskResult(task.result(), task.error())), (a, b) -> b));
      return new TaskExecutionResults(results);
    }
  }

}
