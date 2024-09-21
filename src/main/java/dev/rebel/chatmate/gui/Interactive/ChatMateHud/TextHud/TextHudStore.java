package dev.rebel.chatmate.gui.Interactive.ChatMateHud.TextHud;

import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;

import java.util.ArrayList;
import java.util.List;

public class TextHudStore {
  private final ChatMateHudStore chatMateHudStore;

  private final List<TextHudElement> hudElements;

  public TextHudStore(ChatMateHudStore chatMateHudStore) {
    this.chatMateHudStore = chatMateHudStore;

    this.hudElements = new ArrayList<>();
  }

  public List<TextHudElement> getElements() {
    return this.hudElements;
  }

  public TextHudElement addElement(InteractiveScreen.ElementFactory<TextHudElement> onCreateElement) {
    TextHudElement element = this.chatMateHudStore.addElement(onCreateElement);
    this.hudElements.add(element);
    return element;
  }

  public void removeElement(TextHudElement element) {
    this.chatMateHudStore.removeElement(element);
    this.hudElements.remove(element);
  }
}
