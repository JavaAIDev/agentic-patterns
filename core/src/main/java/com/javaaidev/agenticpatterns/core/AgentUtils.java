package com.javaaidev.agenticpatterns.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;

public class AgentUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper().enable(
      SerializationFeature.INDENT_OUTPUT);

  public static String loadPromptTemplateFromClasspath(String resource) {
    try {
      return new ClassPathResource(resource).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new AgentExecutionException("Prompt template not found: " + resource, e);
    }
  }

  public static Map<String, Object> mergeMap(@Nullable Map<String, Object> map1,
      @Nullable Map<String, Object> map2) {
    var m1 = Optional.ofNullable(map1)
        .orElseGet(HashMap::new);
    var m2 = Optional.ofNullable(map2)
        .orElseGet(HashMap::new);
    var result = new HashMap<String, Object>();
    result.putAll(m1);
    result.putAll(m2);
    return result;
  }

  public static <T, R> R safeGet(@Nullable T obj, Function<T, R> extractor, R defaultValue) {
    return Optional.ofNullable(obj).map(extractor).orElse(defaultValue);
  }

  public static String toJson(@Nullable Object input) {
    if (input == null) {
      return "{}";
    }
    try {
      return objectMapper.writeValueAsString(input);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  public static Map<String, Object> objectToMap(@Nullable Object object) {
    if (object == null) {
      return new HashMap<>();
    }
    var json = toJson(object);
    try {
      return objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      return new HashMap<>();
    }
  }
}
