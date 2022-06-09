package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.EnumHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ImageElement extends SingleElement {
  private @Nullable Texture image;
  private float scale;

  public ImageElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.image = null;
    this.scale = 1;
  }

  public ImageElement setImage(@Nullable Texture image) {
    if (!Objects.equals(this.image, image)) {
      this.image = image;
      super.onInvalidateSize();
    }
    return this;
  }

  public ImageElement setScale(float scale) {
    if (this.scale != scale) {
      this.scale = scale;
      super.onInvalidateSize();
    }
    return this;
  }

  private float getEffectiveScale(Dim maxWidth) {
    assert this.image != null;

    Dim actualWidth = gui(this.image.width);
    if (this.getSizingMode() == SizingMode.MINIMISE || this.getSizingMode() == SizingMode.ANY) {
      Dim scaledWidth = actualWidth.times(this.scale);
      if (scaledWidth.lte(maxWidth)) {
        return this.scale;
      } else {
        return this.scale * maxWidth.over(scaledWidth);
      }
    } else if (this.getSizingMode() == SizingMode.FILL) {
      return maxWidth.over(actualWidth);
    } else {
      throw EnumHelpers.<SizingMode>assertUnreachable(this.getSizingMode());
    }
  }

  @Nullable @Override
  public List<IElement> getChildren() {
    return null;
  }

  /** FILL: Proportionally scales the image to fill the available width, ignoring the custom scale that may have been set.<br/>
   * MINIMISE: Uses the set scale (defaulting to 1x), or reduces the scale if the width surpasses the available width. */
  @Override
  public IElement setSizingMode(SizingMode sizingMode) {
    return super.setSizingMode(sizingMode);
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    if (this.image == null) {
      return new DimPoint(ZERO, ZERO);
    }

    float effectiveScale = this.getEffectiveScale(maxContentSize);
    Dim width = gui(this.image.width).times(effectiveScale);
    Dim height = gui(this.image.height).times(effectiveScale);
    return new DimPoint(width, height);
  }

  @Override
  public void renderElement() {
    if (this.image == null) {
      return;
    }

    DimPoint size = getContentBoxSize(super.lastCalculatedSize);
    DimRect imageBox = ElementHelpers.alignElementInBox(size, super.getContentBox(), super.getHorizontalAlignment(), super.getVerticalAlignment());
    float effectiveScale = imageBox.getWidth().over(gui(this.image.width));
    RendererHelpers.drawTexture(super.context, this.image, imageBox.getTopLeft(), effectiveScale);
  }
}
