package dev.rebel.chatmate.services.util;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

public class Objects {
  public static @Nullable <T, R> R casted(Class<T> type, Object obj, Function<T, R> fn) {
    if (type.isAssignableFrom(obj.getClass())) {
      return fn.apply((T)obj);
    } else {
      return null;
    }
  }

  /** Convenience method for checking an object's type and asserting a condition in an if-statement. */
  public static <T> boolean ifClass(Class<T> type, Object obj, Predicate<T> predicate) {
    return type.isAssignableFrom(obj.getClass()) && predicate.test((T)obj);
  }

  public static @Nullable <T> T castOrNull(Class<T> type, @Nullable Object obj) {
    if (obj == null) {
      return null;
    }

    return type.isAssignableFrom(obj.getClass()) ? (T)obj : null;
  }
}
