package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.ScreenResizeData.In;
import dev.rebel.chatmate.services.events.models.ScreenResizeData.Options;
import dev.rebel.chatmate.services.events.models.ScreenResizeData.Out;

public class ScreenResizeData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    private final int newScreenWidth;
    private final int newScreenHeight;

    public In(int newScreenWidth, int newScreenHeight) {
      this.newScreenWidth = newScreenWidth;
      this.newScreenHeight = newScreenHeight;
    }
  }

  public static class Out extends EventOut { }

  public static class Options extends EventOptions { }
}
