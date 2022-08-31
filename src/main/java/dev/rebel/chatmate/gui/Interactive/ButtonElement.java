package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class ButtonElement extends InputElement {
  private Dim minSize;
  private @Nullable IElement childElement;
  private boolean hovered;
  private @Nullable Runnable onClick;
  private Dim borderCornerRadius;

  public ButtonElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    this.minSize = ZERO;
    this.childElement = null;
    this.hovered = false;
    this.onClick = null;
    this.borderCornerRadius = gui(3.5f);

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

  public ButtonElement setOnClick(@Nullable Runnable onClick) {
    this.onClick = onClick;
    return this;
  }

  /** If sizing mode is not FILL, and the provided minimum size is less than the maximum size when calculating the layout,
   * the button will ensure its size does not exceed the provided value. */
  public ButtonElement setMinSize(@Nullable Dim minSize) {
    this.minSize = minSize == null ? ZERO : minSize;
    return this;
  }

  public ButtonElement setBorderCornerRadius(Dim cornerRadius) {
    this.borderCornerRadius = cornerRadius;
    return this;
  }

  @Override
  public void onMouseDown(IEvent<MouseEventData.In> e) {
    MouseEventData.In data = e.getData();
    if (data.isClicked(MouseButton.LEFT_BUTTON) && this.getEnabled() && this.onClick != null) {
      this.context.soundService.playButtonSound();
      this.onClick.run();
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseEnter(IEvent<In> e) {
    this.hovered = true;
    super.context.cursorService.toggleCursor(super.getEnabled() ? CursorType.CLICK : CursorType.DEFAULT, this);
  }

  @Override
  public void onMouseExit(IEvent<In> e) {
    this.hovered = false;
    super.context.cursorService.untoggleCursor(this);
  }

  @Override
  public InputElement setEnabled(Object key, boolean enabled) {
    if (this.hovered) {
      super.context.cursorService.toggleCursor(enabled ? CursorType.CLICK : CursorType.DEFAULT, this);
    }
    return super.setEnabled(key, enabled);
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
    int hoverState = !this.getEnabled() ? 0 : this.hovered ? 2 : 1; // 0 if disabled, 1 if normal, 2 if hovering

    // the button border is drawn as the normal border part (around the padding box)
    // the inner border is drawn *around the content box*

    Dim borderWidth = super.getBorder().left;
    RendererHelpers.drawRect(0, this.getPaddingBox(), Colour.TRANSPARENT, borderWidth, Colour.BLACK, this.borderCornerRadius);

    if (this.hovered && this.getEnabled()) {
      Dim borderDistance = super.getPadding().left;
      Dim innerCornerRadius = Dim.max(screen(6), this.borderCornerRadius.minus(borderDistance));
      RendererHelpers.drawRect(0, this.getContentBox(), Colour.TRANSPARENT, borderWidth, Colour.LTGREY, innerCornerRadius);
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

    @Override
    protected void renderElement() {
      int j = 14737632;
      if (!super.getEnabled()) {
        j = 10526880;
      } else if (super.hovered) {
        j = 16777120;
      }
      this.label.setColour(new Colour(j));

      super.renderElement();
    }
  }

  public static class IconButtonElement extends ButtonElement {
    private final static Colour DISABLED_COLOUR = new Colour(0.5f, 0.5f, 0.5f, 1);

    public final ImageElement image;

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
    public IconButtonElement setMaxWidth(@Nullable Dim maxWidth) {
      this.image.setMaxWidth(maxWidth);
      return this;
    }

    /** This determines the width to which the image will be scaled. Defaults to the image's width at 1x scale. */
    public IconButtonElement setMaxContentWidth(@Nullable Dim maxContentWidth) {
      this.image.setMaxContentWidth(maxContentWidth);
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
      int j = 14737632;
      if (!super.getEnabled()) {
        j = 10526880;
      } else if (super.hovered) {
        j = 16777120;
      }
      this.image.setColour(new Colour(j));

      super.renderElement();
    }
  }
}
