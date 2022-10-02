package dev.rebel.chatmate.util;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Objects {
  public static @Nullable <T> T casted(Class<T> type, Object obj) {
    if (obj != null && type.isAssignableFrom(obj.getClass())) {
      return (T)obj;
    } else {
      return null;
    }
  }

  public static @Nullable <T, R> R casted(Class<T> type, Object obj, Function<T, R> fn) {
    if (obj != null && type.isAssignableFrom(obj.getClass())) {
      return fn.apply((T)obj);
    } else {
      return null;
    }
  }

  // ffs... can't just overload `casted` because java has trouble detecting whether we are providing a consumer of function
  public static <T> void castedVoid(Class<T> type, Object obj, Consumer<T> fn) {
    if (obj != null && type.isAssignableFrom(obj.getClass())) {
      fn.accept((T)obj);
    }
  }

  /** Convenience method for checking an object's type and asserting a condition in an if-statement. */
  public static <T> boolean ifClass(Class<T> type, Object obj, @Nullable Predicate<T> predicate) {
    return obj != null && type.isAssignableFrom(obj.getClass()) && (predicate == null || predicate != null && predicate.test((T)obj));
  }

  public static @Nullable <T> T castOrNull(Class<T> type, @Nullable Object obj) {
    if (obj == null) {
      return null;
    }

    return type.isAssignableFrom(obj.getClass()) ? (T)obj : null;
  }

  public static @Nullable <T> T firstOrNull(T... objects) {
    for (T obj : objects) {
      if (obj != null) {
        return obj;
      }
    }

    return null;
  }
}
