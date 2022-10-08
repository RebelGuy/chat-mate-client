package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.models.RenderChatGameOverlay.In;
import dev.rebel.chatmate.events.models.RenderChatGameOverlay.Out;
import dev.rebel.chatmate.events.models.RenderChatGameOverlay.Options;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class RenderChatGameOverlay extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public RenderGameOverlayEvent.Chat event;

    public In(RenderGameOverlayEvent.Chat event) {
      this.event = event;
    }
  }

  public static class Out extends EventOut { }

  public static class Options extends EventOptions { }
}
