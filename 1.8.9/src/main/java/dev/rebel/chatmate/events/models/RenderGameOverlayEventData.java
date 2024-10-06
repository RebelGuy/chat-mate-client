package dev.rebel.chatmate.events.models;

import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

public class RenderGameOverlayEventData {
  public final ElementType renderType;

  public RenderGameOverlayEventData(ElementType renderType) {
    this.renderType = renderType;
  }
}
