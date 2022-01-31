package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay.In;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay.Out;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay.Options;

public class RenderChatGameOverlay extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final int posX;
    public final int posY;

    public In(int posX, int posY) {
      this.posX = posX;
      this.posY = posY;
    }
  }

  public static class Out extends EventOut {
    public final Integer newPosX;
    public final Integer newPosY;

    public Out() {
      this(null, null);
    }

    public Out(Integer newPosX, Integer newPosY) {
      this.newPosX = newPosX;
      this.newPosY = newPosY;
    }
  }

  public static class Options extends EventOptions { }
}
