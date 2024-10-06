package dev.rebel.chatmate.gui.Interactive.Events;

import dev.rebel.chatmate.gui.Interactive.IElement;

public class InteractiveEvent<TData> {
  private final EventPhase phase;
  private final TData data;
  private IElement target;
  private final boolean supportsTargetOverriding;
  public boolean stoppedPropagation;
  public boolean overriddenTarget;

  public InteractiveEvent(EventPhase phase, TData data, IElement target) {
    this(phase, data, target, false);
  }

  public InteractiveEvent(EventPhase phase, TData data, IElement target, boolean supportsTargetOverriding) {
    this.phase = phase;
    this.data = data;
    this.target = target;
    this.supportsTargetOverriding = supportsTargetOverriding;
    this.stoppedPropagation = false;
    this.overriddenTarget = false;
  }

  public TData getData() {
    return this.data;
  }

  /** Does not have an affect when the EventPhase is TARGET. */
  public void stopPropagation() {
    this.stoppedPropagation = true;
  }

  public EventPhase getPhase() {
    return this.phase;
  }

  /** The element to which the event is propagating to/from. If the EventPhase is TARGET, it is equal to the element receiving the event. */
  public IElement getTarget() {
    return this.target;
  }

  /** For events in the CAPTURE phase, it is possible to modify the target element to the current element.
   * This will set the bottom of the event propagation chain, and the BUBBLE phase will commence instantly with the new target. */
  public void overrideTarget() {
    if (!this.supportsTargetOverriding) {
      throw new RuntimeException("The event does not support overriding the target");
    }

    this.overriddenTarget = true;
  }

  public enum EventPhase {
    CAPTURE, // the event is currently on its way propagating downwards towards the current target
    BUBBLE, // the event is currently on its way propagating upwards away from the target
    TARGET // the event is at its target and does not propagate (i.e. it fires only for this element - calling stopPropagate() has no effect)
  }

  public enum EventType {
    MOUSE_DOWN, MOUSE_MOVE, MOUSE_UP, MOUSE_SCROLL, MOUSE_ENTER, MOUSE_EXIT, // MouseEventData.In
    KEY_DOWN, KEY_UP, // KeyboardEventData.In
    FOCUS, BLUR, // FocusEventData
    WINDOW_RESIZE // SizeData, does not propagate. This will fire BEFORE layouts are recalculated and is sent to all elements from top to bottom.
    // if you add more events, be sure to update the onEvent handler of the ElementBase class
  }
}
