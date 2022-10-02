package dev.rebel.chatmate.gui.StateManagement;

import dev.rebel.chatmate.util.Collections;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;

/** Represents a fade-out state of one or more instantaneous events. */
public class AnimatedEvent<TEvent> {
  private final long duration;

  private final Object lock = new Object();
  private List<Animated<TEvent>> items;

  public AnimatedEvent(long duration) {
    this.duration = duration;
    this.items = java.util.Collections.synchronizedList(new ArrayList<>());
  }

  public void onEvent(TEvent event) {
    synchronized (this.lock) {
      this.purgeOldEntries();

      Animated<TEvent> newAnimated = new Animated<>(this.duration, null);
      newAnimated.set(event);
      this.items.add(newAnimated);
    }
  }

  public List<Tuple<TEvent, Float>> getAllFracs() {
    synchronized (this.lock) {
      this.purgeOldEntries();
      return Collections.map(this.items, item -> new Tuple<>(item.value, item.getFrac()));
    }
  }

  private void purgeOldEntries() {
    this.items = Collections.filter(this.items, item -> item.getFrac() < 1);
  }
}
