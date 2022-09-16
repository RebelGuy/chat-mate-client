package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.services.CursorService;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.util.Collections;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class CheckboxInputElement extends InputElement {
  private final LabelElement labelElement;
  private final Dim boxSize;
  private final AnimatedBool isHovering;
  private final AnimatedBool isFocused;
  private final AnimatedBool isChecked;

  private float scale;
  private @Nullable Consumer<Boolean> onChange;
  private Colour labelColour;
  private Colour borderColour;

  public CheckboxInputElement(InteractiveContext context, IElement parent) {
    super(context, parent);
    super.setCursor(CursorType.CLICK);

    this.labelElement = new LabelElement(context, this)
        .setOverflow(TextOverflow.SPLIT)
        .setPadding(new RectExtension(gui(4), ZERO, ZERO, ZERO))
        .cast();
    this.boxSize = gui(8);

    this.scale = 1;
    this.onChange = null;
    this.borderColour = Colour.BLACK;
    this.labelColour = Colour.WHITE;
    this.isHovering = new AnimatedBool(200L, false);
    this.isFocused = new AnimatedBool(100L, false);
    this.isChecked = new AnimatedBool(100L, false);
  }

  /** Does not fire the onChange callback. */
  public CheckboxInputElement setChecked(boolean checked) {
    this.isChecked.setImmediate(checked);
    return this;
  }

  public CheckboxInputElement onCheckedChanged(Consumer<Boolean> onChange) {
    this.onChange = onChange;
    return this;
  }

  public boolean getChecked() {
    return this.isChecked.get();
  }

  public CheckboxInputElement setLabel(String label) {
    this.labelElement.setText(label);
    return this;
  }

  public CheckboxInputElement setScale(float scale) {
    this.labelElement.setFontScale(scale);
    this.scale = scale;
    return this;
  }

  public CheckboxInputElement setCheckboxBorderColour(Colour colour) {
    this.borderColour = colour;
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return Collections.list(this.labelElement);
  }

  @Override
  public void onMouseEnter(IEvent<In> e) {
    this.isHovering.set(true);
  }

  @Override
  public void onMouseExit(IEvent<In> e) {
    this.isHovering.set(false);
  }

  @Override
  public void onMouseDown(IEvent<In> e) {
    this.onFlipChecked();
    e.stopPropagation();
  }

  @Override
  public void onFocus(IEvent<FocusEventData> e) {
    this.isFocused.set(true);
  }

  @Override
  public void onBlur(IEvent<FocusEventData> e) {
    this.isFocused.set(false);
  }

  @Override
  public void onKeyDown(IEvent<KeyboardEventData.In> e) {
    if (e.getData().eventKey == Keyboard.KEY_SPACE) {
      this.onFlipChecked();
      e.stopPropagation();
    }
  }

  @Override
  public InputElement setEnabled(Object key, boolean enabled) {
    super.setEnabled(key, enabled);
    if (super.getEnabled()) {
      this.labelElement.setColour(this.labelColour);
    } else {
      this.labelElement.setColour(this.labelColour.withBrightness(0.5f));
    }
    return this;
  }

  private void onFlipChecked() {
    if (!super.getEnabled()) {
      return;
    }

    boolean isChecked = this.isChecked.flip();
    if (this.onChange != null) {
      this.onChange.accept(isChecked);
    }
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    DimPoint labelSize = this.labelElement.calculateSize(maxContentSize.minus(this.boxSize));
    return new DimPoint(boxSize.plus(labelSize.getX()), Dim.max(boxSize, labelSize.getY()));
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);
    Dim labelLeft = box.getX().plus(this.boxSize);
    Dim labelTop = getContentBox().getY();
    Dim labelHeight = getContentBox().getHeight();
    this.labelElement.setBox(box.withLeft(labelLeft).withTop(labelTop).withHeight(labelHeight));
  }

  @Override
  protected void renderElement() {
    DimRect checkbox = new DimRect(this.getContentBox().getPosition(), new DimPoint(this.boxSize, this.boxSize));
    Colour background = Colour.BLACK.withAlpha(this.isHovering.getFrac() * 0.4f);
    Dim borderWidth = gui(1);
    Dim cornerRadius = gui(0);
    Dim shadowDistance = gui(1 + this.isFocused.getFrac());
    Colour shadowColour = Colour.lerp(Colour.BLACK, Colour.CYAN.withBrightness(0.5f), this.isFocused.getFrac());
    Colour borderColour = this.borderColour;

    if (!super.getEnabled()) {
      background = Colour.TRANSPARENT;
      shadowDistance = ZERO;
      borderColour = borderColour.withBrightness(0.5f);
    }

    RendererHelpers.drawRect(0, checkbox, background, borderWidth, borderColour, cornerRadius, shadowDistance, shadowColour);

    RendererHelpers.withMapping(checkbox.getCentre(), this.isChecked.getFrac(), () -> {
      FontEngine font = super.context.fontEngine;
      Dim height = font.FONT_HEIGHT_DIM;
      Dim width = font.getCharWidth('x');
      font.drawString("x", -width.over(2).minus(screen(1)).getGui(), -height.over(2).getGui(), this.labelElement.getFont());
    });

    this.labelElement.render(null);
  }
}
