package dev.rebel.chatmate.services;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;

public class RenderService {
  // todo: can add proper laying out of boxed components with text alignments, padding, automatic wrapping (Using fontRenderer), etc.
  // e.g. boxes will be anchored to preset points of the screen (corners, centres, etc), and reposition automatically
  // if more boxes are added or the screen size changes.
  // use fontRenderer.getStringWidth() for aligning text
  //
  // we can use GL11 (OpenGL) directly if we want
  // e.g. https://relativity.net.au/gaming/java/Transparency.html
  // e.g. https://forums.minecraftforge.net/topic/33464-18solved-drawing-a-simple-line/
  //
  // OpenGL reference: https://learnopengl.com/

  private static int VERTICAL_PADDING = 2;

  private final Minecraft minecraft;
  private final ForgeEventService forgeEventService;
  private final FontEngine fontEngine;
  private final DimFactory dimFactory;
  private final Font font;

  // until adding custom layouts, we only allow drawing one text object at a time
  private WeakReference<DrawnText> drawnText; // (fancy weak reference!)

  public RenderService(Minecraft minecraft, ForgeEventService forgeEventService, FontEngine fontEngine, DimFactory dimFactory) {
    this.minecraft = minecraft;
    this.forgeEventService = forgeEventService;
    this.fontEngine = fontEngine;
    this.dimFactory = dimFactory;
    this.font = new Font().withShadow(new Shadow(dimFactory));

    this.registerHandlers();
  }

  public DrawnText drawText(int x, int y, float scale, String... lines) {
    DrawnText drawnText = new DrawnText(x, y, scale, Arrays.stream(lines).filter(Objects::nonNull).toArray(String[]::new));
    this.drawnText = new WeakReference<>(drawnText);
    return drawnText;
  }

  private void registerHandlers() {
    this.forgeEventService.onRenderGameOverlay(this::onRenderGameOverlay, new Options(ElementType.ALL));
  }

  private RenderGameOverlay.Out onRenderGameOverlay(RenderGameOverlay.In eventIn) {
    if (this.drawnText == null) {
      return null;
    } else if (this.drawnText.get() == null) {
      this.drawnText = null;
      return null;
    }

    DrawnText drawnText = this.drawnText.get();
    if (drawnText.isVisible) {
      int fontHeight = this.fontEngine.FONT_HEIGHT;

      // we scale in the "push-pop" block only.
      // see https://forums.minecraftforge.net/topic/44188-111-is-it-possible-to-change-the-font-size-of-a-string/
      // note that this scales the screen, so we can just draw the text as we normally would
      GlStateManager.pushMatrix();
      GlStateManager.scale(drawnText.scale, drawnText.scale, drawnText.scale);

      for (int i = 0; i < drawnText.lines.length; i++) {
        String text = drawnText.lines[i];
        int lineY = drawnText.y + (fontHeight + VERTICAL_PADDING) * i;
        this.fontEngine.drawString(text, drawnText.x, lineY, this.font);
      }

      GlStateManager.popMatrix();
    }

    return null;
  }

  // Modifying these properties will be reflected in the rendering immediately.
  public static class DrawnText {
    public int x;
    public int y;
    public float scale;
    public String[] lines;
    public boolean isVisible;

    public DrawnText(int x, int y, float scale, String... lines) {
      this.x = x;
      this.y = y;
      this.scale = scale;
      this.lines = lines;
      this.isVisible = true;
    }
  }
}
