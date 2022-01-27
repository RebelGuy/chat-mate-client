package dev.rebel.chatmate.gui.dashboard.components.DashboardMainView;

import dev.rebel.chatmate.gui.components.ComponentManager;
import dev.rebel.chatmate.gui.components.View;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainView.*;
import dev.rebel.chatmate.gui.shared.components.SimpleButton.SimpleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import javax.annotation.Nonnull;

import static dev.rebel.chatmate.gui.components.Components.Interactive.Buttons.SimpleButton;

public class DashboardMainViewView extends View<VProps, State> {
  private final static State initialState = new State();
  private Minecraft mc;

  protected DashboardMainViewView(@Nonnull ComponentManager componentManager) {
    super(componentManager, initialState);
  }

  @Override
  protected void onInitialise(@Nonnull VProps initialProps) {
    this.mc = initialProps.minecraft;
  }

  @Override
  protected void onUpdate(@Nonnull VProps prevProps, @Nonnull VProps props) {

  }

  @Override
  protected void onDispose() {

  }

  @Override
  protected void onRenderScreen() {
    GlStateManager.disableLighting();
    GlStateManager.disableFog();
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    int height = this.getProps().height;
    int width = this.getProps().width;
    double z = -1;
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
    worldrenderer.pos(0.0D, (double)height, 0.0D).tex(0.0D, (double)((float)height / 32.0F + (float)0)).color(64, 64, 64, 255).endVertex();
    worldrenderer.pos((double)width, (double)height, 0.0D).tex((double)((float)width / 32.0F), (double)((float)height / 32.0F + (float)0)).color(64, 64, 64, 255).endVertex();
    worldrenderer.pos((double)width, 0.0D, 0.0D).tex((double)((float)width / 32.0F), (double)z).color(64, 64, 64, 255).endVertex();
    worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, (double)z).color(64, 64, 64, 255).endVertex();
    tessellator.draw();
  }

  @Override
  protected void onRenderComponents() {
    this.add(SimpleButton("Close", new SimpleButton.Props(this.getProps().onClick)));
  }
}
