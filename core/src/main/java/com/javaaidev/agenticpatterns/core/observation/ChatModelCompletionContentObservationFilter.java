package com.javaaidev.agenticpatterns.core.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import java.util.List;
import java.util.Objects;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.content.Content;
import org.springframework.ai.observation.ObservabilityHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class ChatModelCompletionContentObservationFilter implements ObservationFilter {

  @Override
  public Observation.Context map(Observation.Context context) {
    if (!(context instanceof ChatModelObservationContext chatModelObservationContext)) {
      return context;
    }

    var prompts = processPrompts(chatModelObservationContext);
    var completions = processCompletion(chatModelObservationContext);

    chatModelObservationContext.addHighCardinalityKeyValue(new KeyValue() {
      @Override
      public String getKey() {
        return "gen_ai.prompt";
      }

      @Override
      public String getValue() {
        return ObservabilityHelper.concatenateStrings(prompts);
      }
    });

    chatModelObservationContext.addHighCardinalityKeyValue(new KeyValue() {
      @Override
      public String getKey() {
        return "gen_ai.completion";
      }

      @Override
      public String getValue() {
        return ObservabilityHelper.concatenateStrings(completions);
      }
    });

    return chatModelObservationContext;
  }

  private List<String> processPrompts(ChatModelObservationContext chatModelObservationContext) {
    var instructions = chatModelObservationContext.getRequest().getInstructions();
    return CollectionUtils.isEmpty(instructions)
        ? List.of()
        : instructions.stream().map(Content::getText).toList();
  }

  private List<String> processCompletion(ChatModelObservationContext context) {
    if (context.getResponse() == null) {
      return List.of();
    }

    var results = context.getResponse().getResults();
    if (CollectionUtils.isEmpty(results)) {
      return List.of();
    }

    return results.stream()
        .map(Generation::getOutput)
        .filter(Objects::nonNull)
        .map(AbstractMessage::getText)
        .filter(StringUtils::hasText)
        .toList();
  }
}
