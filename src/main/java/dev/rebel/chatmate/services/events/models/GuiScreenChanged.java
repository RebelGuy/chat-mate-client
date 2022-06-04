package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.GuiScreenChanged.In;
import dev.rebel.chatmate.services.events.models.GuiScreenChanged.Options;
import dev.rebel.chatmate.services.events.models.GuiScreenChanged.Out;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenChanged extends EventData<In, Out, Options> {
  public static class In extends EventIn {

    private final GuiScreen fromScreen;
    private final GuiScreen toScreen;

    public In(GuiScreen fromScreen, GuiScreen toScreen) {
      this.fromScreen = fromScreen;
      this.toScreen = toScreen;
    }
  }

  public static class Out extends EventOut { }

  public static class Options extends EventOptions { }
}