package dev.rebel.chatmate.services.util;

import javax.annotation.Nullable;

public class EnumHelpers {
  /** Use this in the `else`/`default` blocks when exhaustively testing enum values. When extending an enum type, you can then search for usages. */
  public static <T extends Enum<T>> RuntimeException assertUnreachable(T item) {
    return assertUnreachable(item, null);
  }

  /** Use this in the `else`/`default` blocks when exhaustively testing enum values. When extending an enum type, you can then search for usages. */
  public static <T extends Enum<T>> RuntimeException assertUnreachable(T item, @Nullable String message) {
    String customMessage = message == null ? "" : String.format("(%s)", message);
    return new RuntimeException(String.format("Invalid %s enum value: %s%s", item.getClass().getSimpleName(), item, customMessage));
  }
}
