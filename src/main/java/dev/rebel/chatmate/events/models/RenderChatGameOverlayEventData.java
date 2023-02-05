package dev.rebel.chatmate.events.models;

public class RenderChatGameOverlayEventData {
  public final int posX;
  public final int posY;

  public RenderChatGameOverlayEventData(int posX, int posY) {
    this.posX = posX;
    this.posY = posY;
  }
}
