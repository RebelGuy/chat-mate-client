package dev.rebel.chatmate.events;

public class Event<TData> {
  private TData data;
  private final boolean canStopPropagation;
  public boolean stoppedPropagation;
  public boolean hasModifiedData = false;

  /** Only for empty events. */
  public Event() {
    this(null);
  }

  public Event(TData data) {
    this(data, true);
  }

  public Event(TData data, boolean canStopPropagation) {
    this.data = data;
    this.canStopPropagation = canStopPropagation;
  }

  public TData getData() {
    return this.data;
  }

  public void stopPropagation() {
    if (!this.canStopPropagation) {
      throw new RuntimeException("This event does not support propagation to be stopped.");
    }

    this.stoppedPropagation = true;
  }

  public void modifyData(TData newData) {
    this.data = newData;
    this.hasModifiedData = true;
  }
}
