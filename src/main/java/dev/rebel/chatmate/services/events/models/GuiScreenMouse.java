package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.Base.EventIn;
import dev.rebel.chatmate.services.events.models.Base.EventOptions;
import dev.rebel.chatmate.services.events.models.Base.EventOut;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

public class GuiScreenMouse {
  public static class In extends EventIn {
    public final Type type;
    public final Integer x;
    public final Integer y;

    public final @Nullable Integer dx;
    public final @Nullable Integer dy;
    public final @Nullable Boolean isDragging;

    /** Click event. */
    public In(boolean isMouseDownType, int x, int y) {
      this.type = isMouseDownType ? Type.DOWN : Type.UP;
      this.x = x;
      this.y = y;

      this.dx = null;
      this.dy = null;
      this.isDragging = false;
    }

    /** Mouse move event. */
    public In(int x, int y, int dx, int dy, boolean isDragging) {
      this.type = Type.MOVE;
      this.x = x;
      this.y = y;
      this.dx = dx;
      this.dy = dy;
      this.isDragging = isDragging;
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
