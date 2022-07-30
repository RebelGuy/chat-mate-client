package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.models.DimPoint;

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
    /** The element to which the event is propagating to/from. If the EventPhase is TARGET, it is equal to the element receiving the event. */
    IElement getTarget();
  }

  public enum EventPhase {
    CAPTURE, // the event is currently on its way propagating downwards
    BUBBLE, // the event is currently on its way propagating upwards
    TARGET // the event is at its target and does not propagate (i.e. it fires only for this element - calling stopPropagate() has no effect)
  }

  public enum EventType {
    MOUSE_DOWN, MOUSE_MOVE, MOUSE_UP, MOUSE_SCROLL, MOUSE_ENTER, MOUSE_EXIT, // MouseEventData.In
    KEY_DOWN, // KeyboardEventData.In
    FOCUS, BLUR, // FocusEventData
    WINDOW_RESIZE // SizeData, does not propagate. This will fire BEFORE layouts are recalculated and is sent to all elements from top to bottom.
    // if you add more events, be sure to update the onEvent handler of the ElementBase class
  }

  public static class FocusEventData {
    public final @Nullable InputElement fromFocus;
    public final @Nullable InputElement toFocus;
    public final FocusReason reason;

    public FocusEventData(@Nullable InputElement fromFocus, @Nullable InputElement toFocus, FocusReason reason) {
      this.fromFocus = fromFocus;
      this.toFocus = toFocus;
      this.reason = reason;
    }
  }

  public enum FocusReason {
    CLICK, TAB,
    AUTO // can only fire when the InteractiveScreen is first shown - never again after that
  }

  public static class SizeData {
    public final DimPoint size;

    public SizeData(DimPoint size) {
      this.size = size;
    }
  }
}
