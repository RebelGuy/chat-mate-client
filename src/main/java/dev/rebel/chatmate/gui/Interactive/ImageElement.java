package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import net.minecraft.client.renderer.texture.TextureManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ImageElement extends SingleElement {
  private @Nullable Texture image;
  protected float scale;
  private @Nullable Colour colour;

  public ImageElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.image = null;
    this.scale = 1;
    this.colour = null;
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

  /** Sets a colour modifier. */
  public ImageElement setColour(@Nullable Colour colour) {
    this.colour = colour;
    return this;
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
  protected DimPoint calculateThisSize(Dim maxWidth) {
    if (this.image == null) {
      return new DimPoint(ZERO, ZERO);
    }

    // dimensions before culling
    Dim width = gui(this.image.width).times(this.scale);
    Dim height = gui(this.image.height).times(this.scale);

    if (width.gt(maxWidth)) {
      float scalingFactor = width.over(maxWidth);
      width = width.over(scalingFactor);
      height = height.over(scalingFactor);
    }

    @Nullable Dim effectiveHeight = super.getEffectiveTargetHeight();
    @Nullable Dim maxHeight = effectiveHeight == null ? null : super.getContentBoxHeight(effectiveHeight);
    if (maxHeight != null && height.gt(maxHeight)) {
      float scalingFactor = height.over(maxHeight);
      height = height.over(scalingFactor);
      width = width.over(scalingFactor);
    }

    if (this.getSizingMode() == SizingMode.FILL) {
      float scalingFactor = width.over(maxWidth);

      if (maxHeight != null) {
        float heightScalingFactor = height.over(maxHeight);

        // use the larger scaling factor (i.e. we are dividing by a larger number), otherwise we would get bigger than allowed
        if (heightScalingFactor > scalingFactor) {
          scalingFactor = heightScalingFactor;
        }
      }

      width = width.over(scalingFactor);
      height = height.over(scalingFactor);
    }

    return new DimPoint(width, height);
  }

  @Override
  protected void renderElement() {
    if (this.image == null) {
      return;
    }

    DimPoint size = getContentBoxSize(super.lastCalculatedSize);
    DimRect imageBox = ElementHelpers.alignElementInBox(size, super.getContentBox(), super.getHorizontalAlignment(), super.getVerticalAlignment());
    float effectiveScale = imageBox.getWidth().over(gui(this.image.width));
    TextureManager textureManager = super.context.minecraft.getTextureManager();
    RendererHelpers.drawTexture(textureManager, super.context.dimFactory, this.image, imageBox.getTopLeft(), effectiveScale, this.colour);
  }
}
