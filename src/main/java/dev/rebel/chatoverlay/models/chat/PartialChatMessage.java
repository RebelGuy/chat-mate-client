package dev.rebel.chatoverlay.models.chat;

public class PartialChatMessage {
  private PartialChatMessageType messageType;

  // for text type
  private String text;
  private Boolean isBold;
  private Boolean isItalics;

  // for emoji type
  // the hover-over name
  private String name;
  // short emoji label (e.g. shortcut text/search term)
  private String label;
  private ChatImage image;
}
