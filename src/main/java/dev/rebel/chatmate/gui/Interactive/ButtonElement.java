package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class ButtonElement extends SingleElement {
  private static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

  /** A button cannot physically be wider than this value, as it is limited by the texture width itself.
   * If there is ever a requirement for wider buttons, the rendering mechanism will need to be modified. */
  private static final int MAX_WIDTH_GUI = 200;

  /** Based on the texture. I don't know what happens if this is larger, though. */
  private static final int MIN_WIDTH_GUI = 20;

  private LabelElement label;
  private LayoutMode layoutMode;
  private boolean hovered;
  private boolean enabled;

  public ButtonElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    this.layoutMode = LayoutMode.FIT;
    this.label = (LabelElement)new LabelElement(context, this)
        .setText("")
        .setAlignment(LabelElement.TextAlignment.CENTRE)
        .setOverflow(LabelElement.TextOverflow.TRUNCATE)
        .setLayoutMode(LabelElement.LayoutMode.FIT)
        .setHorizontalAlignment(HorizontalAlignment.CENTRE)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setPadding(new Layout.RectExtension(context.dimFactory.fromGui(4))); // make sure the text doesn't touch the border

    this.enabled = true;
    this.hovered = false;
  }

  public ButtonElement setText(String text) {
    this.label.setText(text);
    this.onInvalidateSize();
    return this;
  }

  public ButtonElement setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public boolean onMouseMove(In in) {
    return super.onMouseMove(in);
  }

  @Override
  public List<IElement> getChildren() {
    return Collections.list(this.label);
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    maxWidth = Dim.min(getContentBoxWidth(maxWidth), this.context.dimFactory.fromGui(MAX_WIDTH_GUI));

    DimPoint labelSize = this.label.calculateSize(maxWidth);

    Dim width;
    if (this.layoutMode == LayoutMode.FIT) {
      width = Dim.min(labelSize.getX(), maxWidth);
    } else if (this.layoutMode == LayoutMode.FULL_WIDTH) {
      width = maxWidth;
    } else {
      throw new RuntimeException("Invalid LayoutMode " + this.layoutMode);
    }

    Dim height = Dim.max(this.context.dimFactory.fromGui(MIN_WIDTH_GUI), labelSize.getY());
    return this.setLastCalculatedSize(getFullBoxSize(new DimPoint(width, height)));
  }

  @Override
  protected DimPoint setLastCalculatedSize(DimPoint size) {
    this.lastCalculatedSize = size;
    return size;
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    DimRect contentBox = this.getContentBox();
    DimRect labelBox = this.alignChild(this.label);
    this.label.setBox(labelBox);
  }

  @Override
  public void renderElement() {
    int hoverState = !this.enabled ? 0 : this.hovered ? 2 : 1; // 0 if disabled, 1 if normal, 2 if hovering

    this.context.minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.blendFunc(770, 771);

    DimPoint pos = this.getContentBox().getPosition();
    DimPoint size = this.getContentBox().getSize();

    // draw left half of button
    DimRect rect1 = new DimRect(pos, new DimPoint(size.getX().over(2), size.getY()));
    int u1 = 0;
    int v1 = 46 + hoverState * 20;
    RendererHelpers.drawTexturedModalRect(rect1, 0, u1, v1);

    // draw right half of button (assumes the button is not larger than 200)
    DimRect rect2 = new DimRect(pos.getX().plus(size.getX().over(2)), pos.getY(), size.getX().over(2), size.getY());
    int u2 = MAX_WIDTH_GUI - (int)size.getX().over(2).getGui();
    int v2 = 46 + hoverState * 20;
    RendererHelpers.drawTexturedModalRect(rect2, 0, u2, v2);

    int j = 14737632;
    if (!this.enabled) {
      j = 10526880;
    } else if (this.hovered) {
      j = 16777120;
    }
    this.label.setColour(new Colour(j));
    this.label.render();
  }

  public enum LayoutMode {
    FIT, // the element's width will be calculated to fit the text
    FULL_WIDTH // the element's width will always take up 100% of the provided width
  }
}
