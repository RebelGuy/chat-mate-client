package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.OpenGui.In;
import dev.rebel.chatmate.services.events.models.OpenGui.Out;
import dev.rebel.chatmate.services.events.models.OpenGui.Options;
import net.minecraft.client.gui.GuiScreen;

public class OpenGui extends EventData<In, Out, Options> {
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
