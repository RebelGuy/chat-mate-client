package dev.rebel.chatmate.gui.shared.components.SimpleButton;

import dev.rebel.chatmate.gui.components.ComponentManager;
import dev.rebel.chatmate.gui.components.View;
import dev.rebel.chatmate.gui.shared.components.SimpleButton.SimpleButton.*;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MousePositionData;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class SimpleButtonView extends View<VProps, State> {
  private final static State initialState = new State(false);
  protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");

  private int posX = 200;
  private int posY = 100;
  private int w = 50;
  private int h = 20;

  public SimpleButtonView(ComponentManager manager) {
    super(manager, initialState);
  }

  @Override
  protected void onInitialise(@Nonnull VProps initialProps) {
    initialProps.mouseHandler.accept(this::onMouseDown);
  }

  private MouseEventData.Out onMouseDown(MouseEventData.In eventIn) {
    MousePositionData position = eventIn.mousePositionData;
    boolean overButton = position.x.getScreen() >= this.posX && position.x.getScreen() <= this.posX + this.w && position.y.getScreen() >= this.posY && position.y.getScreen() <= this.posY + this.h;

    if (overButton) {
      this.getProps().onClick.run();
      return new MouseEventData.Out(MouseHandlerAction.SWALLOWED);
    }

    return new MouseEventData.Out();
  }

  @Override
  protected void onUpdate(@Nonnull VProps prevProps, @Nonnull VProps props) {

  }

  @Override
  protected void onDispose() {
    // todo: unsubscribe mouse handler
  }

  @Override
  protected void onRenderScreen() {
    GlStateManager.translate(0, (float)(20 * Math.sin(System.currentTimeMillis() / 1000.0)), 0);
    FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
    Minecraft.getMinecraft().getTextureManager().bindTexture(buttonTextures);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//    this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
//    int i = this.getHoverState(this.hovered);

    // hover state: Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over this button
    int i = this.getState().isHovering ? 2 : 1;

    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.blendFunc(770, 771);
    this.drawTexturedModalRect(this.posX, this.posY, 0, 46 + i * 20, this.w / 2, this.h);
    this.drawTexturedModalRect(this.posX + this.w / 2, this.posY, 200 - this.w / 2, 46 + i * 20, this.w / 2, this.h);
    int j = 14737632;

    boolean enabled = true;
    if (!enabled)
    {
      j = 10526880;
    }
    else if (this.getState().isHovering)
    {
      j = 16777120;
    }

    this.drawCenteredString(fontrenderer, "TEST", this.posX + this.w / 2, this.posY + (this.h - 8) / 2, j);
  }

  @Override
  protected void onRenderComponents() {
    this.add(this.getProps().children);
  }

  /**
   * Renders the specified text to the screen, center-aligned. Args : renderer, string, x, y, color
   */
  public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
  {
    fontRendererIn.drawStringWithShadow(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color);
  }

  /**
   * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
   */
  public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
  {
    float f = 0.00390625F;
    float f1 = 0.00390625F;
    float zLevel = 0;
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
    worldrenderer.pos((double)(x + 0), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + height) * f1)).endVertex();
    worldrenderer.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f1)).endVertex();
    worldrenderer.pos((double)(x + width), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + 0) * f1)).endVertex();
    worldrenderer.pos((double)(x + 0), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + 0) * f1)).endVertex();
    tessellator.draw();
  }
}
