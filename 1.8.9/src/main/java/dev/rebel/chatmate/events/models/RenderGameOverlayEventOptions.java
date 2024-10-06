package dev.rebel.chatmate.events.models;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.Arrays;
import java.util.List;

public class RenderGameOverlayEventOptions {
  public final List<RenderGameOverlayEvent.ElementType> subscribeToTypes;

  public RenderGameOverlayEventOptions(RenderGameOverlayEvent.ElementType... subscribeToTypes) {
    this.subscribeToTypes = Arrays.asList(subscribeToTypes);
  }
}
