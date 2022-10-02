package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.models.RenderGameOverlay.In;
import dev.rebel.chatmate.events.models.RenderGameOverlay.Out;
import dev.rebel.chatmate.events.models.RenderGameOverlay.Options;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.util.Arrays;
import java.util.List;

public class RenderGameOverlay extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final ElementType renderType;

    public In(ElementType renderType) {
      this.renderType = renderType;
    }
  }

  public static class Out extends EventOut {
  }

  public static class Options extends EventOptions {
    public final List<ElementType> subscribeToTypes;

    public Options(ElementType... subscribeToTypes) {
      this.subscribeToTypes = Arrays.asList(subscribeToTypes);
    }
  }
}
