package com.javaaidev.agenticpatterns.examples.parallelizationworkflow;

import com.javaaidev.agenticpatterns.core.AgentUtils;
import com.javaaidev.agenticpatterns.parallelizationworkflow.DefaultResponseAssembler;
import com.javaaidev.agenticpatterns.parallelizationworkflow.ParallelizationWorkflow;
import com.javaaidev.agenticpatterns.parallelizationworkflow.SubtaskCreationRequest;
import com.javaaidev.agenticpatterns.taskexecution.TaskExecutionAgent;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
public class AlgorithmArticleGenerationConfiguration {

  @Bean
  @Qualifier("algorithmArticleGenerationWorkflow")
  public ParallelizationWorkflow<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse> algorithmArticleGenerationWorkflow(
      ChatClient.Builder chatClientBuilder,
      ObservationRegistry observationRegistry
  ) {
    var chatClient = chatClientBuilder.build();
    return ParallelizationWorkflow.<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse>builder()
        .subtasksCreator(request -> {
          var languages = AgentUtils.safeGet(request, AlgorithmArticleGenerationRequest::languages,
              List.<String>of());
          if (CollectionUtils.isEmpty(languages)) {
            return List.of();
          }
          return languages.stream().map(
              language -> new SubtaskCreationRequest<>(language,
                  TaskExecutionAgent.<SampleCodeGenerationRequest, AlgorithmArticleGenerationResponse>defaultBuilder()
                      .promptTemplate("""
                          Write {language} code to meet the requirement.
                          {description}
                          """
                      )
                      .responseType(SampleCodeGenerationResponse.class)
                      .name("CodeGeneration_" + language)
                      .chatClient(chatClient)
                      .observationRegistry(observationRegistry)
                      .build(),
                  (AlgorithmArticleGenerationRequest req) -> new SampleCodeGenerationRequest(
                      language,
                      "Implement algorithm " + req.algorithm()))).toList();
        })
        .responseAssembler(
            DefaultResponseAssembler.<AlgorithmArticleGenerationRequest, AlgorithmArticleGenerationResponse>builder()
                .promptTemplate(
                    """
                        Goal: Write an article about {algorithm}.
                        
                        Requirements:
                        - Start with a brief introduction.
                        - Include only sample code listed below.
                        - Output the article in markdown.
                        
                        {sample_code}
                        """)
                .responseType(AlgorithmArticleGenerationResponse.class)
                .promptTemplateContextProvider(input -> {
                  var request = input.request();
                  var results = input.results();
                  var algorithm = AgentUtils.safeGet(request,
                      AlgorithmArticleGenerationRequest::algorithm, "");
                  var sampleCode = results.allSuccessfulResults().entrySet()
                      .stream()
                      .map(entry -> """
                          Language: %s
                          Code:
                          %s
                          """.formatted(entry.getKey(),
                          ((SampleCodeGenerationResponse) entry.getValue()).code()))
                      .collect(
                          Collectors.joining("==========\n", "\n----------\n",
                              "=========="));
                  return Map.of("algorithm", algorithm, "sample_code",
                      sampleCode);
                })
                .name("AlgorithmArticleGeneration")
                .chatClient(chatClient)
                .observationRegistry(observationRegistry)
                .build())
        .name("algorithmArticleGenerationWorkflow")
        .observationRegistry(observationRegistry)
        .build();
  }
}
