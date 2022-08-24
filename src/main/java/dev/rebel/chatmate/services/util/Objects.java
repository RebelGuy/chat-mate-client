package dev.rebel.chatmate.services.util;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class Objects {
  /** Convenience method for checking an object's type and asserting a condition in an if-statement. */
  public static <T> boolean casted(Class<T> type, Object obj,  Predicate<T> predicate) {
    return type.isAssignableFrom(obj.getClass()) && predicate.test((T)obj);
  }

  public static @Nullable <T> T castOrNull(Class<T> type, @Nullable Object obj) {
    if (obj == null) {
      return null;
    }

    return type.isAssignableFrom(obj.getClass()) ? (T)obj : null;
  }
}
