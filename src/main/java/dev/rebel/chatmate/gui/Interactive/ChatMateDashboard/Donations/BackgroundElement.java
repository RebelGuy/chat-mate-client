package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.Interactive.WrapperElement;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;

import javax.annotation.Nullable;

import static dev.rebel.chatmate.services.util.Objects.firstOrNull;

public class BackgroundElement extends WrapperElement {
  private @Nullable Colour colour = null;
  private @Nullable Colour hoverColour = null;
  private @Nullable Colour borderColour = null;
  private @Nullable Dim cornerRadius = null;

  public BackgroundElement(InteractiveScreen.InteractiveContext context, IElement parent, IElement contents) {
    super(context, parent, contents);
  }

  public BackgroundElement setColour(@Nullable Colour colour) {
    this.colour = colour;
    return this;
  }

  public BackgroundElement setHoverColour(@Nullable Colour hoverColour) {
    this.hoverColour = hoverColour;
    return this;
  }

  public BackgroundElement setBorderColour(@Nullable Colour borderColour) {
    this.borderColour = borderColour;
    return this;
  }

  public BackgroundElement setCornerRadius(@Nullable Dim cornerRadius) {
    this.cornerRadius = cornerRadius;
    return this;
  }

  @Override
  protected void renderElement() {
    @Nullable Colour colour = firstOrNull(this.isHovering() ? this.hoverColour : this.colour, this.colour);
    if (colour != null) {
      Dim borderWidth = super.getBorder().left;
      RendererHelpers.drawRect(this.getEffectiveZIndex(), super.getPaddingBox(), colour, borderWidth, this.borderColour, this.cornerRadius);
    }

    super.renderElement();
  }
}
