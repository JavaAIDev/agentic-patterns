package com.javaaidev.agenticpatterns.core;

import java.lang.reflect.Type;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;

public class TypeResolver {

  public static Type resolveType(Type base, Class<?> target, int index) {
    ResolvableType resolvableType = ResolvableType.forType(base).as(target);
    var type = resolvableType.getGeneric(index);
    if (ResolvableType.NONE.equals(type)) {
      return Object.class;
    }
    return doResolveType(type);
  }

  @Nullable
  private static Type doResolveType(final ResolvableType type) {
    if (type.getType() instanceof Class<?>) {
      return type.resolve();
    }
    if (!type.hasGenerics()) {
      return Object.class;
    }
    var generics = type.getGenerics();
    var paraType = new ParameterizedTypeImpl(type.resolve(),
        new Type[generics.length],
        null);
    for (int i = 0; i < generics.length; i++) {
      var nestedType = generics[i];
      paraType.getActualTypeArguments()[i] = doResolveType(nestedType);
    }
    return paraType;
  }

}
