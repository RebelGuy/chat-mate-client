package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.gui.CustomGuiChat;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.TransformedHudElementWrapper;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudFilters;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.hud.Observable;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.services.KeyBindingService;
import dev.rebel.chatmate.services.KeyBindingService.ChatMateKeyEvent;

import javax.annotation.Nullable;

public class CounterHandler {
  private final KeyBindingService keyBindingService;
  private final ChatMateHudStore chatMateHudStore;
  private final DimFactory dimFactory;
  private Counter counter;

  public CounterHandler(KeyBindingService keyBindingService, ChatMateHudStore chatMateHudStore, DimFactory dimFactory) {
    this.keyBindingService = keyBindingService;
    this.chatMateHudStore = chatMateHudStore;
    this.dimFactory = dimFactory;

    this.keyBindingService.on(ChatMateKeyEvent.DECREMENT_COUNTER, this::decrementCounter);
    this.keyBindingService.on(ChatMateKeyEvent.INCREMENT_COUNTER, this::incrementCounter);
  }

  public void createCounter(int startValue, int incrementValue, float scale, @Nullable String title) {
    this.deleteCounter();
    this.counter = new Counter(this.chatMateHudStore, this.dimFactory, startValue, incrementValue, title);
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

  private static class Counter {
    private final ChatMateHudStore chatMateHudStore;
    private final TransformedHudElementWrapper<LabelElement> hudElement;
    private final int incrementValue;

    private final String title;
    private final Observable<String> observableString;
    private int value;

    public Counter(ChatMateHudStore chatMateHudStore, DimFactory dimFactory, int startValue, int incrementValue, @Nullable String title) {
      this.chatMateHudStore = chatMateHudStore;
      this.value = startValue;
      this.incrementValue = incrementValue;
      this.title = title == null ? "" : title + " ";
      this.observableString = new Observable<>(this.getStringToRender());

      this.hudElement = this.chatMateHudStore.addElement(TransformedHudElementWrapper::new)
          .setCanDrag(true)
          .setCanScale(true)
          .setDefaultPosition(dimFactory.getMinecraftRect().getCentre(), Anchor.MIDDLE)
          .setHudElementFilter(
              new HudFilters.HudFilterWhitelistNoScreen(),
              new HudFilters.HudFilterScreenWhitelist(CustomGuiChat.class),
              new HudFilters.HudFilterInteractiveScreenTypeBlacklist(InteractiveScreenType.DASHBOARD)
          ).cast();
      this.hudElement.setElement(LabelElement::new)
          .setText(this.observableString.getValue())
          .setFont(new Font().withShadow(new Shadow(dimFactory)));
      this.observableString.listen(str -> this.hudElement.element.setText(str));
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
