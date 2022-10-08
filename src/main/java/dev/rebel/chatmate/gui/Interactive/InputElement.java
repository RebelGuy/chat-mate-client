package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.services.CursorService.CursorType;

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

    // initialise this element first before setting cursor
    super.setCursor(CursorType.CLICK);
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  @Override
  protected boolean shouldUseCursor() {
    return this.getEnabled() && super.shouldUseCursor();
  }

  /** This way of doing things allows multiple-source data validation (e.g. the input is valid in an isolated scope, but the form it is part of is disabled). */
  public InputElement setEnabled(Object key, boolean enabled) {
    if (enabled && this.disabledSet.contains(key)) {
      this.disabledSet.remove(key);
    } else if (!enabled && !this.disabledSet.contains(key)) {
      this.disabledSet.add(key);
    } else {
      // no changes made
      return this;
    }

    this.setFocusable(this.getEnabled());
    super.updateCursor();

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

  public @Nullable Integer getTabIndex() {
    return this.tabIndex;
  }
}
