package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import net.minecraft.client.renderer.GlStateManager;

public class RendererHelpers {
  public static void withTranslation(DimPoint translation, Runnable onRender) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(translation.getX().getGui(), translation.getY().getGui(), 0);
    onRender.run();
    GlStateManager.popMatrix();
  }
}
