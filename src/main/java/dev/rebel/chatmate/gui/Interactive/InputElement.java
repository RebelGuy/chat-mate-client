package dev.rebel.chatmate.gui.Interactive;

import javax.annotation.Nullable;
import java.util.*;

public abstract class InputElement extends SingleElement {
  private Set<Object> disabledSet;
  private boolean valid;
  private boolean autoFocus;
  private @Nullable Integer tabIndex;
  private boolean isFocusable;

  public InputElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    this.isFocusable = true;
    this.disabledSet = new HashSet<>();
    this.valid = true;
    this.autoFocus = false;
    this.tabIndex = null;
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

  public final boolean getEnabled() {
    return this.disabledSet.size() == 0;
  }

  public final InputElement setValid(boolean valid) {
    this.valid = valid;
    return this;
  }

  public final boolean getValid() {
    return this.valid;
  }

  public final InputElement setFocusable(boolean focusable) {
    this.isFocusable = focusable;
    return this;
  }

  public final boolean getFocusable() {
    return this.isFocusable;
  }

  public final boolean hasFocus() {
    return super.context.focusedElement == this;
  }

  public final InputElement setAutoFocus(boolean autoFocus) {
    this.autoFocus = autoFocus;
    return this;
  }

  public final boolean getAutoFocus() {
    return this.autoFocus;
  }

  public final InputElement setTabIndex(@Nullable Integer tabIndex) {
    this.tabIndex = tabIndex;
    return this;
  }

  public boolean canTabFocus() {
    return this.canFocus() && this.getTabIndex() != null;
  }

  public boolean canFocus() {
    return this.getVisible() && this.getEnabled() && this.getFocusable();
  }

  @Nullable
  public Integer getTabIndex() {
    return this.tabIndex;
  }
}
