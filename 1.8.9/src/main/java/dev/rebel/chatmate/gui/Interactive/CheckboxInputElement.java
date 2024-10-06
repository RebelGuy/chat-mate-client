package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.util.Collections;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.Objects.firstOrNull;

public class CheckboxInputElement extends InputElement {
  private final ContainerElement container;
  private final BoxElement boxElement;
  private final LabelElement labelElement;
  private final AnimatedBool isHovering;
  private final AnimatedBool isFocused;
  private final AnimatedBool isChecked;

  private float scale;
  private List<Consumer<Boolean>> onChangeListeners;
  private Colour labelColour;
  private @Nullable Colour checkColour;
  private CheckboxAppearance checkboxAppearance;
  private BiFunction<Boolean, Boolean, Boolean> willSetCheckedValidator;

  public CheckboxInputElement(InteractiveContext context, IElement parent) {
    super(context, parent);
    super.setCursor(CursorType.CLICK);

    this.boxElement = new BoxElement(context, this)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setBorder(new RectExtension(gui(1)))
        .cast();
    this.labelElement = new LabelElement(context, this)
        .setOverflow(TextOverflow.SPLIT)
        .setPadding(new RectExtension(gui(4), ZERO, ZERO, ZERO))
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();
    this.container = new InlineElement(context, this)
        .addElement(this.boxElement)
        .addElement(this.labelElement);

    this.scale = 1;
    this.onChangeListeners = new ArrayList<>();
    this.labelColour = Colour.WHITE;
    this.checkColour = null;
    this.checkboxAppearance = CheckboxAppearance.CROSS;
    this.isHovering = new AnimatedBool(200L, false);
    this.isFocused = new AnimatedBool(100L, false);
    this.isChecked = new AnimatedBool(100L, false);
  }

  /** Does not fire the onChange callback. */
  public CheckboxInputElement setChecked(boolean checked) {
    return this.setChecked(checked, false);
  }

  public CheckboxInputElement setChecked(boolean checked, boolean simulateClick) {
    if (simulateClick) {
      if (this.getChecked() != checked) {
        this.onFlipChecked(false);
      }
    } else {
      this.isChecked.setImmediate(checked);
    }

    return this;
  }

  public CheckboxInputElement onCheckedChanged(Consumer<Boolean> onChange) {
    this.onChangeListeners.add(onChange);
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
    this.boxElement.setBorder(new RectExtension(gui(1 * scale)));
    this.scale = scale;
    return this;
  }

  public CheckboxInputElement setCheckboxBorderColour(Colour colour) {
    this.boxElement.borderColour = colour;
    return this;
  }

  public CheckboxInputElement setCheckboxAppearance(CheckboxAppearance appearance) {
    this.checkboxAppearance = appearance;
    return this;
  }

  /** If null, falls back to the text colour. */
  public CheckboxInputElement setCheckColour(@Nullable Colour colour) {
    this.checkColour = colour;
    return this;
  }

  /** Provide a callback that will be called when the checked state is about to change due to user input.
   * The first argument is the updated check value. The second argument specifies whether we are changing the state
   * due to user input (if false, implies programmatic input).
   * If the callback returns false, the input will be blocked and the checkbox state will not change. */
  public CheckboxInputElement setValidator(BiFunction<Boolean, Boolean, Boolean> willSetCheckedValidator) {
    this.willSetCheckedValidator = willSetCheckedValidator;
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return Collections.list(this.container);
  }

  @Override
  public void onMouseEnter(InteractiveEvent<MouseEventData> e) {
    this.isHovering.set(true);
  }

  @Override
  public void onMouseExit(InteractiveEvent<MouseEventData> e) {
    this.isHovering.set(false);
  }

  @Override
  public void onMouseDown(InteractiveEvent<MouseEventData> e) {
    this.onFlipChecked(true);
    e.stopPropagation();
  }

  @Override
  public void onFocus(InteractiveEvent<FocusEventData> e) {
    this.isFocused.set(true);
  }

  @Override
  public void onBlur(InteractiveEvent<FocusEventData> e) {
    this.isFocused.set(false);
  }

  @Override
  public void onKeyDown(InteractiveEvent<KeyboardEventData> e) {
    if (e.getData().eventKey == Keyboard.KEY_SPACE) {
      this.onFlipChecked(true);
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

  private void onFlipChecked(boolean isUserInput) {
    if (!super.getEnabled() && isUserInput) {
      return;
    }

    if (this.willSetCheckedValidator != null && !this.willSetCheckedValidator.apply(!this.getChecked(), isUserInput)) {
      return;
    }

    boolean isChecked = this.isChecked.flip();
    this.onChangeListeners.forEach(listener -> listener.accept(isChecked));
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.container.calculateSize(maxContentSize);
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);
    this.container.setBox(super.getContentBox());
  }

  @Override
  protected void renderElement() {
    this.container.render(null);
  }

  private class BoxElement extends SingleElement {
    private Colour borderColour;
    private Dim boxSize;

    public BoxElement(InteractiveContext context, IElement parent) {
      super(context, parent);

      this.borderColour = Colour.BLACK;
      this.boxSize = gui(8);
    }

    @Override
    public @Nullable List<IElement> getChildren() {
      return null;
    }

    @Override
    protected DimPoint calculateThisSize(Dim maxContentSize) {
      return new DimPoint(this.boxSize, this.boxSize).scale(CheckboxInputElement.this.scale);
    }

    @Override
    protected void renderElement() {
      Dim cornerRadius = CheckboxInputElement.this.checkboxAppearance == CheckboxAppearance.CROSS ? ZERO : this.boxSize.over(2);
      Dim shadowDistance = gui(1 + CheckboxInputElement.this.isFocused.getFrac());
      Colour shadowColour = Colour.lerp(Colour.BLACK, Colour.CYAN.withBrightness(0.5f), CheckboxInputElement.this.isFocused.getFrac());
      Colour borderColour = this.borderColour;
      Colour background = Colour.BLACK.withAlpha(CheckboxInputElement.this.isHovering.getFrac() * 0.4f);
      Colour checkColour = firstOrNull(CheckboxInputElement.this.checkColour, CheckboxInputElement.this.labelElement.getFont().getColour());

      if (!CheckboxInputElement.super.getEnabled()) {
        background = Colour.TRANSPARENT;
        shadowDistance = ZERO;
        borderColour = borderColour.withBrightness(0.5f);
      }

      Dim borderWidth = super.getBorder().left;
      GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
      RendererHelpers.drawRect(0, super.getContentBox(), background, borderWidth, borderColour, cornerRadius, shadowDistance, shadowColour);
      GL11.glDisable(GL11.GL_POLYGON_SMOOTH);

      RendererHelpers.withMapping(super.getContentBox().getCentre(), CheckboxInputElement.this.isChecked.getFrac() * CheckboxInputElement.this.scale, () -> {
        if (CheckboxInputElement.this.checkboxAppearance == CheckboxAppearance.CROSS) {
          FontEngine fontEngine = super.context.fontEngine;
          Dim height = fontEngine.FONT_HEIGHT_DIM;
          Dim width = fontEngine.getCharWidth('x');
          Font font = CheckboxInputElement.this.labelElement.getFont().withColour(checkColour);
          fontEngine.drawString("x", -width.over(2).minus(screen(1)).getGui(), -height.over(2).getGui(), font);
        } else {
          int zIndex = super.getEffectiveZIndex();
          DimPoint centre = new DimPoint(ZERO, ZERO);
          Dim radius = cornerRadius.minus(borderWidth).minus(gui(1)); // gap of 1 GUI
          RendererHelpers.drawCircle(zIndex, centre, radius, checkColour);
        }
      });
    }
  }

  public enum CheckboxAppearance { CROSS, RADIO }
}
