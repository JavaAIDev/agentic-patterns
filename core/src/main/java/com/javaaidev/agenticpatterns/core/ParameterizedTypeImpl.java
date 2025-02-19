package com.javaaidev.agenticpatterns.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jspecify.annotations.Nullable;

public record ParameterizedTypeImpl(@Nullable Type rawType,
                                    Type[] actualTypeArguments,
                                    @Nullable Type ownerType) implements ParameterizedType {

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }
}
