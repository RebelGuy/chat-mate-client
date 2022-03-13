package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonElement extends SingleElement {
  private static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

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
        .setPadding(new Layout.RectExtension(context.dimFactory.fromGui(4))); // make sure the text doesn't touch the border

    this.hovered = false;
  }

  public ButtonElement setText(String text) {
    this.label.setText(text);
    this.onInvalidateSize();
    return this;
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    maxWidth = getContentBoxWidth(maxWidth);

    DimPoint labelSize = this.label.calculateSize(maxWidth);

    Dim width;
    if (this.layoutMode == LayoutMode.FIT) {
      width = Dim.min(labelSize.getX(), maxWidth);
    } else if (this.layoutMode == LayoutMode.FULL_WIDTH) {
      width = maxWidth;
    } else {
      throw new RuntimeException("Invalid LayoutMode " + this.layoutMode);
    }

    return getFullBoxSize(new DimPoint(width, labelSize.getY()));
  }

  @Override
  public void render() {
    int hoverState = !this.enabled ? 0 : this.hovered ? 2 : 1; // 0 if disabled, 1 if normal, 2 if hovering

    FontRenderer font = this.context.fontRenderer;
    this.context.minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.blendFunc(770, 771);

    // todo
//    this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
//    this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hoverState * 20, this.width / 2, this.height);
//    this.mouseDragged(mc, mouseX, mouseY);

    int j = 14737632;
    if (!this.enabled) {
      j = 10526880;
    } else if (this.hovered) {
      j = 16777120;
    }
    this.label.setColour(new Colour(j));
    this.label.render();
//    this.drawCenteredString(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);

  }

  public enum LayoutMode {
    FIT, // the element's width will be calculated to fit the text
    FULL_WIDTH // the element's width will always take up 100% of the provided width
  }
}
