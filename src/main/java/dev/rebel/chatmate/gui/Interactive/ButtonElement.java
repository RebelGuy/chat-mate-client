package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.Objects.firstOrNull;

public class ButtonElement extends InputElement {
  private Dim minSize;
  private @Nullable IElement childElement;
  private Dim outerBorderCornerRadius;
  private Dim innerBorderCornerRadius;
  private Colour borderColour;

  public ButtonElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    this.minSize = ZERO;
    this.childElement = null;
    this.outerBorderCornerRadius = gui(3.5f);
    this.innerBorderCornerRadius = null;
    this.borderColour = Colour.BLACK;

    super.setCursor(CursorType.CLICK);
    super.setBorder(new RectExtension(gui(0.5f)));
    super.setPadding(new RectExtension(gui(3)));
  }

  public ButtonElement setChildElement(@Nullable IElement childElement) {
    if (this.childElement != childElement) {
      this.childElement = childElement;
      super.onInvalidateSize();
    }
    return this;
  }

  /** If sizing mode is not FILL, and the provided minimum size is less than the maximum size when calculating the layout,
   * the button will ensure its size does not exceed the provided value. */
  public ButtonElement setMinSize(@Nullable Dim minSize) {
    this.minSize = minSize == null ? ZERO : minSize;
    return this;
  }

  public ButtonElement setBorderCornerRadius(Dim cornerRadius) {
    this.outerBorderCornerRadius = cornerRadius;
    return this;
  }

  /** If null, will be calculated automatically based on the outer border's corner radius. */
  public ButtonElement setInnerBorderCornerRadius(@Nullable Dim cornerRadius) {
    this.innerBorderCornerRadius = cornerRadius;
    return this;
  }

  public ButtonElement setBorderColour(Colour borderColour) {
    this.borderColour = borderColour;
    return this;
  }

  @Override
  public boolean onClickHook(InteractiveEvent<MouseEventData> e) {
    MouseEventData data = e.getData();
    if (data.isClicked(MouseButton.LEFT_BUTTON) && this.getEnabled()) {
      this.context.soundService.playButtonSound();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<IElement> getChildren() {
    return this.childElement == null ? null : Collections.list(this.childElement);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    DimPoint childSize;
    if (this.childElement == null) {
      childSize = new DimPoint(ZERO, ZERO);
    } else {
      childSize = this.childElement.calculateSize(maxContentSize);
    }

    Dim width;
    if (this.getSizingMode() == SizingMode.FILL) {
      width = maxContentSize;
    } else {
      width = Dim.min(childSize.getX(), maxContentSize);

      if (this.minSize.lt(maxContentSize) && width.lt(this.minSize)) {
        width = this.minSize;
      }
    }

    return new DimPoint(width, childSize.getY());
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    if (this.childElement == null) {
      return;
    }

    DimRect childBox = this.alignChild(this.childElement);
    this.childElement.setBox(childBox);
  }

  @Override
  protected void renderElement() {
    // the button border is drawn as the normal border part (around the padding box)
    // the inner border is drawn *around the content box*

    Dim borderWidth = super.getBorder().left;
    RendererHelpers.drawRect(0, this.getPaddingBox(), Colour.TRANSPARENT, borderWidth, this.borderColour, this.outerBorderCornerRadius);

    if (super.isHovering() && this.getEnabled()) {
      Dim borderDistance = super.getPadding().left;
      Dim innerCornerRadius = firstOrNull(this.innerBorderCornerRadius, Dim.max(screen(6), this.outerBorderCornerRadius.minus(borderDistance)));
      RendererHelpers.drawRect(0, this.getContentBox(), Colour.TRANSPARENT, borderWidth, Colour.GREY75, innerCornerRadius);
    }

    if (this.childElement != null) {
      this.childElement.render(null);
    }
  }

  public static class TextButtonElement extends ButtonElement {
    private final LabelElement label;

    public TextButtonElement(InteractiveContext context, IElement parent) {
      super(context, parent);

      this.label = new LabelElement(context, this)
          .setText("")
          .setAlignment(LabelElement.TextAlignment.CENTRE)
          .setOverflow(LabelElement.TextOverflow.TRUNCATE)
          .setSizingMode(SizingMode.MINIMISE)
          .setHorizontalAlignment(HorizontalAlignment.CENTRE)
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .setPadding(new RectExtension(gui(2))) // make sure the text doesn't touch the border
          .cast();
      super.setChildElement(this.label);
    }

    public TextButtonElement setText(String text) {
      this.label.setText(text);
      return this;
    }

    public TextButtonElement setTextScale(float scale) {
      this.label.setFontScale(scale);
      return this;
    }

    public TextButtonElement withLabelUpdated(Consumer<LabelElement> labelUpdater) {
      labelUpdater.accept(this.label);
      return this;
    }

    @Override
    protected void renderElement() {
      int j = 14737632;
      if (!super.getEnabled()) {
        j = 10526880;
      } else if (super.isHovering()) {
        j = 16777120;
      }
      this.label.setColour(new Colour(j));

      super.renderElement();
    }
  }

  public static class IconButtonElement extends ButtonElement {
    private final static Colour DISABLED_COLOUR = new Colour(0.5f, 0.5f, 0.5f, 1);

    public final ImageElement image;

    private @Nullable Colour enabledColour = null;
    private @Nullable Colour disabledColour = null;

    public IconButtonElement(InteractiveContext context, IElement parent) {
      super(context, parent);

      this.image = new ImageElement(context, this)
          .setSizingMode(SizingMode.FILL)
          .setHorizontalAlignment(HorizontalAlignment.CENTRE)
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .setPadding(new RectExtension(gui(2), gui(2))) // make sure the image doesn't touch the border
          .cast();
      super.setChildElement(this.image);
    }

    public IconButtonElement setImage(@Nullable Texture image) {
      this.image.setImage(image);
      if (image != null) {
        this.image.setMaxWidth(gui(image.width));
        this.image.setColour(this.getEnabled() ? null : DISABLED_COLOUR);
      }
      return this;
    }

    /** This determines the width to which the image will be scaled. Defaults to the image's width at 1x scale. */
    @Override
    public IconButtonElement setMaxWidth(@Nullable Dim maxWidth) {
      this.image.setMaxWidth(maxWidth);
      return this;
    }

    /** This determines the width to which the image will be scaled. Defaults to the image's width at 1x scale. */
    @Override
    public IconButtonElement setMaxContentWidth(@Nullable Dim maxContentWidth) {
      this.image.setMaxContentWidth(maxContentWidth);
      return this;
    }

    @Override
    public IElement setTargetHeight(@Nullable Dim targetHeight) {
      this.image.setTargetHeight(targetHeight);
      return this;
    }

    @Override
    public IElement setTargetContentHeight(@Nullable Dim targetContentHeight) {
      this.image.setTargetContentHeight(targetContentHeight);
      return this;
    }

    public IconButtonElement setEnabledColour(@Nullable Colour colour) {
      this.enabledColour = colour;
      return this;
    }

    public IconButtonElement setDisabledColour(@Nullable Colour colour) {
      this.disabledColour = colour;
      return this;
    }

    @Override
    public IconButtonElement setEnabled(Object key, boolean enabled) {
      if (this.image != null) {
        this.image.setColour(enabled ? null : DISABLED_COLOUR);
      }

      super.setEnabled(key, enabled);
      return this;
    }

    @Override
    protected void renderElement() {
      Colour colour;
      if (!super.getEnabled()) {
        colour = this.disabledColour != null ? this.disabledColour : DISABLED_COLOUR;
      } else if (super.isHovering()) {
        colour = new Colour(16777120);
      } else if (this.enabledColour != null) {
        colour = this.enabledColour;
      } else {
        // default enabled colour
        colour = new Colour(14737632);
      }
      this.image.setColour(colour);

      super.renderElement();
    }
  }
}
