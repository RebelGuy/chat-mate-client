package dev.rebel.chatmate.events.models;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MouseEventOptions {
  public final @Nullable Set<MouseEventData.MouseButtonData.MouseButton> listenForButtons;

  /** Only fire button events (`MOUSE_UP`, `MOUSE_DOWN`) for the specified button types. */
  public MouseEventOptions(MouseEventData.MouseButtonData.MouseButton... listenForButtons) {
    this.listenForButtons = listenForButtons.length == 0 ? null : new HashSet<>(Arrays.asList(listenForButtons));
  }
}
