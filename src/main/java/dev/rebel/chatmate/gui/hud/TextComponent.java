package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.GuiChatMateHudScreen;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Shadow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class TextComponent extends Box implements IHudComponent {
  private final Minecraft minecraft;
  private final boolean drawOnTop;
  private float scale;
  private final FontEngine fontEngine;
  private final Font font;
  private Anchor anchor;

  private String text;

  public TextComponent(DimFactory dimFactory, Minecraft minecraft, FontEngine fontEngine, Dim x, Dim y, float scale, boolean canTranslate, boolean canResize, Anchor anchor, boolean drawOnTop, Observable<String> text) {
    super(dimFactory, x, y, dimFactory.zeroGui(), dimFactory.zeroGui(), canTranslate, canResize);
    this.minecraft = minecraft;
    this.fontEngine = fontEngine;
    this.font = new Font().withShadow(new Shadow(dimFactory));;
    this.anchor = anchor;
    this.drawOnTop = drawOnTop;
    this.text = text.getValue();
    this.scale = scale;

    this.updateSize(this.text);
    text.listen(this::updateSize);
  }

  private void updateSize(String text) {
    this.text = text;
    Dim width = this.dimFactory.fromGui(this.fontEngine.getStringWidth(text) * this.scale);
    Dim height = this.dimFactory.fromGui(this.fontEngine.FONT_HEIGHT * this.scale);
    super.onResize(width, height, this.anchor);
  }

  @Override
  public float getContentScale() {
    return this.scale;
  }

  @Override
  public boolean canRescaleContent() {
    return super.canResizeBox();
  }

  @Override
  public void onRescaleContent(float newScale) {
    if (this.scale != newScale) {
      this.scale = newScale;
      this.updateSize(this.text);
    }
  }

  @Override
  public void onTranslate(Dim newX, Dim newY) {


    super.onTranslate(newX, newY);
  }

  @Override
  public void render(RenderContext context) {
    if (!this.drawOnTop) {
      // don't draw if other screens are shown
      GuiScreen screen = this.minecraft.currentScreen;
      if (screen != null && !(screen instanceof GuiChatMateHudScreen)) {
        return;
      }
    }

    RendererHelpers.withMapping(new DimPoint(this.x, this.y), this.scale, () -> {
      context.fontEngine.drawString(this.text, 0, 0, this.font);
    });
  }
}
