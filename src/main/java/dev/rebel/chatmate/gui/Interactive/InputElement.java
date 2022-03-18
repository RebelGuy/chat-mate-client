package dev.rebel.chatmate.gui.Interactive;

import java.util.*;

public abstract class InputElement extends SingleElement {
  private Set<Object> disabledSet;
  private boolean valid;

  public InputElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    this.setFocusable(true);
    this.disabledSet = new HashSet<>();
    this.valid = true;
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  /** This way of doing things allows multiple-source data validation (e.g. the input is valid in an isolated scope, but the form it is part of is disabled). */
  public InputElement setEnabled(Object key, boolean enabled) {
    if (enabled) {
      this.disabledSet.remove(key);
    } else {
      this.disabledSet.add(key);
    }

    this.setFocusable(this.getEnabled());

    return this;
  }

  public boolean getEnabled() {
    return this.disabledSet.size() == 0;
  }

  public InputElement setValid(boolean valid) {
    this.valid = valid;
    return this;
  }

  public boolean getValid() {
    return this.valid;
  }
}
