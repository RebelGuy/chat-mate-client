package dev.rebel.chatmate.services.util;

import javax.annotation.Nullable;
import java.util.List;

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

  /** Returns the item with the highest value, where value is specified by the ordered of the additional values (the first ordered item is the most valuable). */
  public static @Nullable <T extends Enum<T>> T getFirst(List<T> items, T... ordered) {
    if (items == null || items.size() == 0) {
      return null;
    }

    for (T toCheck : ordered) {
      if (items.contains(toCheck)) {
        return toCheck;
      }
    }

    // none of the ordered items were found in the list
    return null;
  }
}
