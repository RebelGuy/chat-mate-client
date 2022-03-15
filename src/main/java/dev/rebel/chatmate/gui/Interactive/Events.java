package dev.rebel.chatmate.gui.Interactive;

import javax.annotation.Nullable;

public class Events {
  public static class InteractiveEvent<TData> implements IEvent<TData> {
    private final EventPhase phase;
    private final TData data;
    private final IElement target;
    public boolean stoppedPropagation;

    public InteractiveEvent(EventPhase phase, TData data, IElement target) {
      this.phase = phase;
      this.data = data;
      this.target = target;
      this.stoppedPropagation = false;
    }

    @Override
    public TData getData() {
      return this.data;
    }

    @Override
    public void stopPropagation() {
      this.stoppedPropagation = true;
    }

    @Override
    public EventPhase getPhase() {
      return this.phase;
    }

    @Override
    public IElement getTarget() {
      return this.target;
    }
  }

  public interface IEvent<TData> {
    TData getData();
    /** Does not have an affect when the EventPhase is TARGET. */
    void stopPropagation();
    EventPhase getPhase();
    /** Equal to the element receiving the event when the EventPhase is TARGET. */
    IElement getTarget();
  }

  public enum EventPhase {
    CAPTURE, // the event is currently on its way propagating downwards
    BUBBLE, // the event is currently on its way propagating upwards
    TARGET // the event is at its target and does not propagate.
  }

  public enum EventType {
    MOUSE_DOWN, MOUSE_MOVE, MOUSE_UP, MOUSE_SCROLL, // MouseEventData.In
    KEY_DOWN, // KeyboardEventData.In
    FOCUS, BLUR // FocusEventData
  }

  public static class FocusEventData {
    public final @Nullable IElement fromFocus;
    public final @Nullable IElement toFocus;

    public FocusEventData(@Nullable IElement fromFocus, @Nullable IElement toFocus) {
      this.fromFocus = fromFocus;
      this.toFocus = toFocus;
    }
  }
}
