package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.MouseEventData.In;
import dev.rebel.chatmate.util.Collections;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class CheckboxInputElement extends InputElement {
  private final ContainerElement container;
  private final BoxElement boxElement;
  private final LabelElement labelElement;
  private final AnimatedBool isHovering;
  private final AnimatedBool isFocused;
  private final AnimatedBool isChecked;

  private float scale;
  private @Nullable Consumer<Boolean> onChange;
  private Colour labelColour;

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
    this.onChange = null;
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
    this.boxElement.setBorder(new RectExtension(gui(1 * scale)));
    this.scale = scale;
    return this;
  }

  public CheckboxInputElement setCheckboxBorderColour(Colour colour) {
    this.boxElement.borderColour = colour;
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return Collections.list(this.container);
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
      Dim cornerRadius = gui(0);
      Dim shadowDistance = gui(1 + CheckboxInputElement.this.isFocused.getFrac());
      Colour shadowColour = Colour.lerp(Colour.BLACK, Colour.CYAN.withBrightness(0.5f), CheckboxInputElement.this.isFocused.getFrac());
      Colour borderColour = this.borderColour;
      Colour background = Colour.BLACK.withAlpha(CheckboxInputElement.this.isHovering.getFrac() * 0.4f);

      if (!CheckboxInputElement.super.getEnabled()) {
        background = Colour.TRANSPARENT;
        shadowDistance = ZERO;
        borderColour = borderColour.withBrightness(0.5f);
      }

      Dim borderWidth = super.getBorder().left;
      RendererHelpers.drawRect(0, super.getContentBox(), background, borderWidth, borderColour, cornerRadius, shadowDistance, shadowColour);

      RendererHelpers.withMapping(super.getContentBox().getCentre(), CheckboxInputElement.this.isChecked.getFrac() * CheckboxInputElement.this.scale, () -> {
        FontEngine font = super.context.fontEngine;
        Dim height = font.FONT_HEIGHT_DIM;
        Dim width = font.getCharWidth('x');
        font.drawString("x", -width.over(2).minus(screen(1)).getGui(), -height.over(2).getGui(), CheckboxInputElement.this.labelElement.getFont());
      });
    }
  }
}
