package dev.rebel.chatmate.gui.StateManagement;

import javax.annotation.Nullable;
import java.util.WeakHashMap;

/** Represents an animated list of items from which one can be selected at a time, where the non-selected items will
 * "cool off" over time. */
public class AnimatedSelection<T> {
  private final long animationDuration;
  private final WeakHashMap<T, AnimatedBool> selectionMap;
  private @Nullable T selected;

  public AnimatedSelection(long animationDuration) {
    this.animationDuration = animationDuration;
    this.selectionMap = new WeakHashMap<>();
  }

  /** Deselects all other items. */
  public void setSelected(@Nullable T item) {
    if (item != null && !this.selectionMap.containsKey(item)) {
      AnimatedBool animatedBool = new AnimatedBool(this.animationDuration, false);
      animatedBool.set(true); // start the animation
      this.selectionMap.put(item, animatedBool);
    }

    this.selected = item;
    this.selectionMap.forEach((x, animated) -> animated.set(x == this.selected));
  }

  public @Nullable T getSelected() {
    return this.selected;
  }

  /** Returns 1 for a "hot" item, 0 for a cooled off item, and anything in between. */
  public float getFrac(T item) {
    if (this.selectionMap.containsKey(item)) {
      return this.selectionMap.get(item).getFrac();
    } else {
      return 0;
    }
  }
}
