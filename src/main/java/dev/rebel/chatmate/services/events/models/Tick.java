package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.Base.EventIn;
import dev.rebel.chatmate.services.events.models.Base.EventOptions;
import dev.rebel.chatmate.services.events.models.Base.EventOut;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.util.Arrays;
import java.util.List;

public class Tick {
  public static class In extends EventIn {
  }

  public static class Out extends EventOut {
  }

  public static class Options extends EventOptions {
  }
}
