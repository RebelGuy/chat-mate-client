package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.Base.EventIn;
import dev.rebel.chatmate.services.events.models.Base.EventOptions;
import dev.rebel.chatmate.services.events.models.Base.EventOut;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.util.Arrays;
import java.util.List;

public class RenderGameOverlay {
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
