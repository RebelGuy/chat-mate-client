package dev.rebel.chatmate.gui.style;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Immutable object describing the shadow behaviour for rendered text. */
public class Shadow {
  private final DimFactory _dimFactory;

  private @Nonnull DimPoint _offset;
  private @Nonnull Dim _blurRadius;
  private @Nullable Colour _colour;

  /** Sensible defaults that reproduce the original Minecraft shadows. */
  public Shadow(DimFactory dimFactory) {
    this._dimFactory = dimFactory;

    this._offset = new DimPoint(dimFactory.fromGui(1), dimFactory.fromGui(1));
    this._blurRadius = dimFactory.zeroGui();
    this._colour = null;
  }

  public Shadow(Shadow parent) {
    this._dimFactory = parent._dimFactory;

    this._offset = parent.getOffset();
    this._blurRadius = parent.getBlurRadius();
    this._colour = parent._colour;
  }

  public Shadow withOffset(DimPoint offset) {
    if (offset == null) {
      throw new RuntimeException("Shadow offset cannot be null");
    }
    return this.update(shadow -> shadow._offset = offset);
  }

  public DimPoint getOffset() {
    return this._offset;
  }

  public Shadow withBlurRadius(Dim blurRadius) {
    if (blurRadius == null) {
      throw new RuntimeException("Shadow blur radius cannot be null");
    }
    return this.update(shadow -> shadow._blurRadius = blurRadius);
  }

  public Dim getBlurRadius() {
    return this._blurRadius;
  }

  /** If set, the shadow will have a static colour (the RGB component is independent of the font colour).
   * If null, the shadow's colour will be dynamically generated based on the font colour that it is shadowing.
   * Note that the transparency component is always interpreted relative to the font's transparency.
   * For example, a shadow alpha of 50% and a font alpha with 50% transparency will result in a 25% shadow alpha. */
  public Shadow withColour(@Nullable Colour colour) {
    return this.update(shadow -> shadow._colour = colour);
  }

  /** Generates the shadow colour, which partially or fully depends on the font colour. */
  public @Nullable Colour getColour(Font font) {
    Colour colour = this._colour == null ? font.getColour().withBrightness(0.25f) : this._colour;

    // simulate layered transparency
    return colour.withAlpha(colour.alphaf * font.getColour().alphaf);
  }

  private Shadow update(Consumer<Shadow> updater) {
    Shadow newShadow = new Shadow(this);
    updater.accept(newShadow);
    return newShadow;
  }
}
