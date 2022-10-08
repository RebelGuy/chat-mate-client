package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.models.OpenGui.In;
import dev.rebel.chatmate.events.models.OpenGui.Out;
import dev.rebel.chatmate.events.models.OpenGui.Options;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

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
    public @Nullable GuiScreen gui;

    public Out() { }

    public Out(@Nullable GuiScreen replaceWithGui) {
      this.shouldReplaceGui = true;
      this.gui = replaceWithGui;
    }
  }

  public static class Options extends EventOptions {
  }
}
