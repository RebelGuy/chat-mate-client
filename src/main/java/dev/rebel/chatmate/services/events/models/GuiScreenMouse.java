package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.Base.EventIn;
import dev.rebel.chatmate.services.events.models.Base.EventOptions;
import dev.rebel.chatmate.services.events.models.Base.EventOut;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

public class GuiScreenMouse {
  public static class In extends EventIn {
    public final Type type;
    public final int startX;
    public final int startY;

    public final boolean isDragging;
    public final int currentX;
    public final int currentY;

    /** Click event. */
    public In(boolean isMouseDownType, int startX, int startY, int currentX, int currentY) {
      this.type = isMouseDownType ? Type.DOWN : Type.UP;
      this.startX = startX;
      this.startY = startY;

      this.isDragging = false;
      this.currentX = currentX;
      this.currentY = currentY;
    }

    /** Mouse move event. */
    public In(int startX, int startY, int currentX, int currentY, boolean isDragging) {
      this.type = Type.MOVE;
      this.startX = startX;
      this.startY = startY;

      this.isDragging = isDragging;
      this.currentX = currentX;
      this.currentY = currentY;
    }
  }

  public static class Out extends EventOut {
  }

  public static class Options extends EventOptions {
    /** Only emit if the mouse event occurs for a GuiScreen of the given class. */
    public final Class<? extends GuiScreen> guiScreenClass;

    public Options(Class<? extends GuiScreen> guiScreenClass) {
      this.guiScreenClass = guiScreenClass;
    }
  }

  public enum Type {
    DOWN,
    MOVE,
    UP
  }
}
