package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.Base.EventIn;
import dev.rebel.chatmate.services.events.models.Base.EventOptions;
import dev.rebel.chatmate.services.events.models.Base.EventOut;
import net.minecraft.client.gui.GuiScreen;

public class OpenGui {
  public static class In extends EventIn {
    public GuiScreen gui;

    public In(GuiScreen gui, boolean isDataModified) {
      super(isDataModified);
      this.gui = gui;
    }
  }

  public static class Out extends EventOut {
    public Boolean shouldReplaceGui;
    public GuiScreen gui;

    public Out() { }

    public Out(GuiScreen replaceWithGui) {
      this.shouldReplaceGui = true;
      this.gui = replaceWithGui;
    }
  }

  public static class Options extends EventOptions {
  }
}
