package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.GuiChatMateHud;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudScreen;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudElementWrapper;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.hud.Observable;
import dev.rebel.chatmate.gui.hud.TextComponent;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.KeyBindingService;
import dev.rebel.chatmate.services.KeyBindingService.ChatMateKeyEvent;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;

public class CounterHandler {
  private final KeyBindingService keyBindingService;
  private final ChatMateHudStore chatMateHudStore;
  private final DimFactory dimFactory;
  private final Minecraft minecraft;
  private final FontEngine fontEngine;
  private Counter counter;

  public CounterHandler(KeyBindingService keyBindingService, ChatMateHudStore chatMateHudStore, DimFactory dimFactory, Minecraft minecraft, FontEngine fontEngine) {
    this.keyBindingService = keyBindingService;
    this.chatMateHudStore = chatMateHudStore;
    this.dimFactory = dimFactory;
    this.minecraft = minecraft;
    this.fontEngine = fontEngine;

    this.keyBindingService.on(ChatMateKeyEvent.DECREMENT_COUNTER, this::decrementCounter);
    this.keyBindingService.on(ChatMateKeyEvent.INCREMENT_COUNTER, this::incrementCounter);
  }

  public void createCounter(int startValue, int incrementValue, float scale, @Nullable String title) {
    this.deleteCounter();
    this.counter = new Counter(this.chatMateHudStore, this.dimFactory, this.minecraft, this.fontEngine, startValue, incrementValue, scale, title);
  }

  public void deleteCounter() {
    if (this.counter != null) {
      this.counter.delete();
      this.counter = null;
    }
  }

  public boolean hasExistingCounter() {
    return this.counter != null;
  }

  private Boolean incrementCounter() {
    if (this.counter != null) {
      this.counter.increment();
    }

    return false;
  }

  private Boolean decrementCounter() {
    if (this.counter != null) {
      this.counter.decrement();
    }

    return false;
  }

  // todo: instead of rendering it here, just add a new component to the hud object, hold on to it, and then remove it when required.
  private static class Counter {
    private final ChatMateHudStore chatMateHudStore;
    private final HudElementWrapper<LabelElement> hudElement;
    private final int incrementValue;
    private final TextComponent textComponent;
    private final String title;
    private final Observable<String> observableString;
    private int value;

    public Counter(ChatMateHudStore chatMateHudStore, DimFactory dimFactory, Minecraft minecraft, FontEngine fontEngine, int startValue, int incrementValue, float scale, @Nullable String title) {
      this.chatMateHudStore = chatMateHudStore;
      this.value = startValue;
      this.incrementValue = incrementValue;
      this.title = title == null ? "" : title + " ";
      this.observableString = new Observable<>(this.getStringToRender());

      DimPoint centre = dimFactory.getMinecraftRect().getCentre();
      Dim x = centre.getX();
      Dim y = centre.getY();
      this.textComponent = new TextComponent(dimFactory, minecraft, fontEngine, x, y, scale, true, true, Anchor.MIDDLE, true, this.observableString);
      this.hudElement = this.chatMateHudStore.addElement(HudElementWrapper::new);
      this.hudElement.setElement(LabelElement::new)
          .setText("This is a test text element.");
    }

    public void increment() {
      this.addToValueSafe(this.incrementValue);
      this.updateString();
    }

    public void decrement() {
      this.addToValueSafe(-this.incrementValue);
      this.updateString();
    }

    public void delete() {
      this.chatMateHudStore.removeElement(this.hudElement);
    }

    private void addToValueSafe(int amount) {
      this.value += amount;

      if (this.value < 0) {
        this.value = 0;
      }
    }

    private void updateString() {
      this.observableString.setValue(this.getStringToRender());
    }

    private String getStringToRender() {
      return this.title + this.value;
    }
  }
}
