package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.DropElement.IDropElementListener;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.events.models.MouseEventData.In.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ValueSliderElement extends InputElement implements IDropElementListener {
  private final LabelElement label;
  private @Nullable DropElement dropElement;

  private @Nullable DimPoint dragPositionStart;
  private @Nullable Float dragValueStart;

  private int decimals;
  private float minValue;
  private float maxValue;
  private @Nonnull String suffix;
  private float value;
  private @Nullable Consumer<Float> onChange;

  public ValueSliderElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);
    super.setBorder(new RectExtension(screen(2)));
    super.setMaxContentWidth(gui(150));
    super.setCursor(CursorType.DEFAULT);

    this.label = new LabelElement(context, this)
        .setText("")
        .setOverflow(TextOverflow.TRUNCATE)
        .setAlignment(TextAlignment.CENTRE)
        .setFontScale(0.75f)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setSizingMode(SizingMode.FILL)
        .cast();
    this.dropElement = null;

    this.dragPositionStart = null;
    this.dragValueStart = null;

    this.decimals = 2;
    this.minValue = 0;
    this.maxValue = 2;
    this.suffix = "";
    this.value = 0;
    this.onChange = null;
  }

  public ValueSliderElement setDecimals(int decimals) {
    this.decimals = decimals;
    this.value = this.autoFixValue(this.value);
    this.label.setText(this.getText());
    return this;
  }

  public ValueSliderElement setMinValue(float minValue) {
    this.minValue = minValue;
    this.value = this.autoFixValue(this.value);
    this.label.setText(this.getText());
    return this;
  }

  public ValueSliderElement setMaxValue(float maxValue) {
    this.maxValue = maxValue;
    this.value = this.autoFixValue(this.value);
    this.label.setText(this.getText());
    return this;
  }

  public ValueSliderElement setSuffix(@Nonnull String suffix) {
    this.suffix = suffix;
    this.label.setText(this.getText());
    return this;
  }

  public ValueSliderElement setValue(float value) {
    this.value = this.autoFixValue(value);
    this.label.setText(this.getText());
    return this;
  }

  /** Only triggered by user input, NOT when the value/constrains are changed via the setters. */
  public ValueSliderElement onChange(Consumer<Float> onChange) {
    this.onChange = onChange;
    return this;
  }

  /** Returns the draggable handle's rect. */
  private DimRect getSliderRect() {
    float valueFrac = (this.value - this.minValue) / (this.maxValue - this.minValue);
    DimRect contentBox = super.getContentBox();
    Dim sliderWidth = gui(3);
    Dim sliderXRel = contentBox.getWidth().minus(sliderWidth).times(valueFrac);
    return new DimRect(contentBox.getX().plus(sliderXRel), contentBox.getY(), sliderWidth, contentBox.getHeight());
  }

  private String getText() {
    String valueString;
    if (this.decimals > 0) {
      valueString = String.format(String.format("%%.%df", this.decimals), this.value);
    } else {
      valueString = String.format("%d", (int)this.value);
    }

    return valueString + this.suffix;
  }

  private float autoFixValue(float value) {
    float rounded = (float)(Math.round(value * Math.pow(10, -this.decimals)) * Math.pow(10, this.decimals));
    if (rounded < this.minValue) {
      return this.minValue;
    } else if (rounded > this.maxValue) {
      return this.maxValue;
    } else {
      return rounded;
    }
  }

  @Override
  public void onMouseDown(IEvent<MouseEventData.In> e) {
    DimPoint position = e.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON && this.getSliderRect().checkCollision(position)) {
      this.dropElement = new DropElement(context, this, true, this);
      this.dragPositionStart = position;
      this.dragValueStart = this.value;
      super.onInvalidateSize();
    }
  }

  @Override
  public void onMouseMove(IEvent<MouseEventData.In> e) {
    if (this.getSliderRect().checkCollision(e.getData().mousePositionData.point)) {
      super.setCursor(CursorType.CLICK);
    } else {
      super.setCursor(CursorType.DEFAULT);
    }
  }

  @Override
  public void onDrag(DimPoint position) {
    Dim deltaMouse = position.minus(this.dragPositionStart).getX();
    Dim sliderWidth = this.getSliderRect().getWidth();
    float deltaRatio = deltaMouse.over(super.getContentBox().getWidth().minus(sliderWidth));
    float deltaValue = deltaRatio * (this.maxValue - this.minValue);
    this.value = this.autoFixValue(this.dragValueStart + deltaValue);
    this.label.setText(this.getText());

    if (this.onChange != null) {
      this.onChange.accept(this.value);
    }
  }

  @Override
  public void onDrop(DimPoint position) {
    this.dropElement = null;
    this.dragPositionStart = null;
    this.dragValueStart = null;
    super.onInvalidateSize();
  }

  @Override
  public void onMouseScroll(IEvent<MouseEventData.In> e) {
    int multiplier = e.getData().mouseScrollData.scrollDirection == ScrollDirection.UP ? -1 : 1;
    float delta = (float)Math.pow(10, this.decimals) * multiplier;
    this.value = this.autoFixValue(this.value + delta);
    this.label.setText(this.getText());
    if (this.onChange != null) {
      this.onChange.accept(this.value);
    }
    e.stopPropagation();
  }

  @Override
  public List<IElement> getChildren() {
    if (this.dropElement == null) {
      return Collections.list(this.label);
    } else {
      return Collections.list(this.label, this.dropElement);
    }
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    DimPoint labelSize = this.label.calculateSize(maxContentSize);
    if (this.dropElement != null) {
      this.dropElement.calculateSize(maxContentSize);
    }
    return new DimPoint(maxContentSize, labelSize.getY().times(1.5f));
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);
    this.label.setBox(super.getContentBox());
    if (this.dropElement != null) {
      this.dropElement.setBox(super.getContentBox());
    }
  }

  @Override
  protected void renderElement() {
    Colour lineColour = Colour.GREY75;
    Dim borderWidth = super.getBorder().left;
    Dim cornerRadius = screen(2);
    DimRect sliderRect = this.getSliderRect();

    if (this.dropElement != null) {
      this.dropElement.render(null);
    }
    RendererHelpers.drawRect(this.getEffectiveZIndex(), super.getPaddingBox(), Colour.TRANSPARENT, borderWidth, lineColour, cornerRadius);
    this.label.render(null);
    RendererHelpers.drawRect(this.getEffectiveZIndex(), sliderRect, Colour.TRANSPARENT, borderWidth, lineColour, cornerRadius);
  }
}
