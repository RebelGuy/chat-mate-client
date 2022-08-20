package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.GuiChatMateHudScreen;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class TextComponent extends Box implements IHudComponent {
  private final Minecraft minecraft;
  private final boolean drawOnTop;
  private float scale;
  private Anchor anchor;

  private String text;

  public TextComponent(DimFactory dimFactory, Minecraft minecraft, Dim x, Dim y, float scale, boolean canTranslate, boolean canResize, Anchor anchor, boolean drawOnTop, Observable<String> text) {
    super(dimFactory, x, y, dimFactory.zeroGui(), dimFactory.zeroGui(), canTranslate, canResize);
    this.minecraft = minecraft;
    this.anchor = anchor;
    this.drawOnTop = drawOnTop;
    this.text = text.getValue();
    this.scale = scale;

    this.updateSize(this.text);
    text.listen(this::updateSize);
  }

  private void updateSize(String text) {
    this.text = text;
    FontRenderer font = this.minecraft.fontRendererObj;
    Dim width = this.dimFactory.fromGui(font.getStringWidth(text) * this.scale);
    Dim height = this.dimFactory.fromGui(font.FONT_HEIGHT * this.scale);
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
    // depending on where the text currently is, modify the anchor so the box expands inwards when resized
    DimRect mcRect = this.dimFactory.getMinecraftRect();
    DimRect left = mcRect.partial(0, 0.25f, 0, 1);
    DimRect centre = mcRect.partial(0.25f, 0.75f, 0, 1);
    DimRect right = mcRect.partial(0.75f, 1, 0, 1);
    DimRect top = mcRect.partial(0, 1, 0, 0.25f);
    DimRect middle = mcRect.partial(0, 1, 0.25f, 0.75f);
    DimRect bottom = mcRect.partial(0, 1, 0.75f, 1);

    DimRect box = new DimRect(this.x, this.y, this.w, this.h);
    Dim x = this.x;
    Dim y = this.y;
    Dim r = box.getRight();
    Dim b = box.getBottom();

    int xAlignment;
    if (left.checkCollisionX(x) && right.checkCollisionX(r) || centre.checkCollisionX(x) && centre.checkCollisionX(r)) {
      xAlignment = 0;
    } else if (left.checkCollisionX(x)) {
      xAlignment = -1;
    } else {
      xAlignment = 1;
    }

    int yAlignment;
    if (top.checkCollisionY(y) && bottom.checkCollisionY(b) || middle.checkCollisionY(y) && middle.checkCollisionY(b)) {
      yAlignment = 0;
    } else if (top.checkCollisionY(y)) {
      yAlignment = -1;
    } else {
      yAlignment = 1;
    }

    Anchor anchor;
    if (xAlignment == -1 && yAlignment == -1) {
      anchor = Anchor.TOP_LEFT;
    } else if (xAlignment == 0 && yAlignment == -1) {
      anchor = Anchor.TOP_CENTRE;
    } else if (xAlignment == 1 && yAlignment == -1) {
      anchor = Anchor.TOP_RIGHT;
    } else if (xAlignment == -1 && yAlignment == 0) {
      anchor = Anchor.LEFT_CENTRE;
    } else if (xAlignment == 0 && yAlignment == 0) {
      anchor = Anchor.MIDDLE;
    } else if (xAlignment == 1 && yAlignment == 0) {
      anchor = Anchor.RIGHT_CENTRE;
    } else if (xAlignment == -1 && yAlignment == 1) {
      anchor = Anchor.BOTTOM_LEFT;
    } else if (xAlignment == 0 && yAlignment == 1) {
      anchor = Anchor.BOTTOM_CENTRE;
    } else {
      anchor = Anchor.BOTTOM_RIGHT;
    }
    this.anchor = anchor;

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
      context.fontEngine.drawStringWithShadow(this.text, 0, 0, Colour.WHITE.toInt());
    });
  }
}
