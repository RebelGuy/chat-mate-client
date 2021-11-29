package dev.rebel.chatmate.models.chat;

public class PartialChatMessage {
  public PartialChatMessageType type;

  // for text type
  public String text;
  public Boolean isBold;
  public Boolean isItalics;

  // for emoji type
  // the hover-over name
  public String name;
  // short emoji label (e.g. shortcut text/search term)
  public String label;
  public ChatImage image;
}
