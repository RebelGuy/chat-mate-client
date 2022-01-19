package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.services.KeyBindingService;
import dev.rebel.chatmate.services.KeyBindingService.ChatMateKeyEvent;
import dev.rebel.chatmate.services.RenderService;
import dev.rebel.chatmate.services.RenderService.DrawnText;

import javax.annotation.Nullable;

public class CounterHandler {
  private final KeyBindingService keyBindingService;
  private final RenderService renderService;

  private RenderedCounter counter;

  public CounterHandler(KeyBindingService keyBindingService, RenderService renderService) {
    this.keyBindingService = keyBindingService;
    this.renderService = renderService;

    this.keyBindingService.on(ChatMateKeyEvent.DECREMENT_COUNTER, this::decrementCounter);
    this.keyBindingService.on(ChatMateKeyEvent.INCREMENT_COUNTER, this::incrementCounter);
  }

  public void createCounter(int startValue, int incrementValue, float scale, @Nullable String title) {
    this.deleteCounter();
    this.counter = new RenderedCounter(this.renderService, startValue, incrementValue, scale, title);
  }

  public void deleteCounter() {
    if (this.counter != null) {
      this.counter.delete();
      this.counter = null;
    }
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

  private static class RenderedCounter {
    private final RenderService renderService;
    private final int incrementValue;
    private final DrawnText drawnText;
    private final String title;
    private int value;

    public RenderedCounter(RenderService renderService, int startValue, int incrementValue, float scale, @Nullable String title) {
      this.renderService = renderService;
      this.value = startValue;
      this.incrementValue = incrementValue;
      this.title = title == null ? "" : title + " ";

      this.drawnText = this.renderService.drawText(5, 5, scale, this.getStringToRender());
    }

    public void increment() {
      this.addToValueSafe(this.incrementValue);
      this.render();
    }

    public void decrement() {
      this.addToValueSafe(-this.incrementValue);
      this.render();
    }

    public void delete() {
      this.drawnText.isVisible = false;
    }

    private void addToValueSafe(int amount) {
      this.value += amount;

      if (this.value < 0) {
        this.value = 0;
      }
    }

    private void render() {
      this.drawnText.lines[0] = this.getStringToRender();
    }

    private String getStringToRender() {
      return this.title + this.value;
    }
  }
}
