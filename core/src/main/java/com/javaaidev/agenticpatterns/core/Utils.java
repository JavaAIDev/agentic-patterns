package com.javaaidev.agenticpatterns.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;

public class Utils {

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
}
