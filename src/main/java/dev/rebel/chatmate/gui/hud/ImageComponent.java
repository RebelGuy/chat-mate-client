package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.RenderContext;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class ImageComponent extends Gui implements IHudComponent {
  private int x;
  private int y;
  private float scale;
  private final boolean canRescale;
  private final boolean canTranslate;

  public final Texture texture;

  public ImageComponent(Texture texture, int x, int y, float scale, boolean canRescale, boolean canTranslate) {
    this.texture = texture;
    this.x = x;
    this.y = y;
    this.scale = scale;
    this.canRescale = canRescale;
    this.canTranslate = canTranslate;
  }

  @Override
  public int getX() {
    return this.x;
  }

  @Override
  public int getY() {
    return this.y;
  }

  @Override
  public int getWidth() {
    return (int)(this.texture.width * this.scale);
  }

  @Override
  public int getHeight() {
    return (int)(this.texture.height * this.scale);
  }

  @Override
  public boolean canResizeBox() {
    // false because it would distort the texture
    return false;
  }

  @Override
  public void onResize(int newWidth, int newHeight) { }

  @Override
  public boolean canRescaleContent() {
    return this.canRescale;
  }

  @Override
  public void onRescaleContent(float newScale) {
    if (this.canRescale && this.scale != newScale) {
      float deltaScale = newScale - this.scale;
      float deltaWidth = deltaScale * this.texture.width;
      float deltaHeight = deltaScale * this.texture.height;
      this.scale = newScale;
      this.x = (int)(this.x + deltaWidth / 2);
      this.y = (int)(this.y + deltaHeight / 2);
    }
  }

  @Override
  public float getContentScale() {
    return this.scale;
  }

  @Override
  public boolean canTranslate() {
    return this.canTranslate;
  }

  @Override
  public void onTranslate(int newX, int newY) {
    if (this.canTranslate) {
      this.x = newX;
      this.y = newY;
    }
  }

  @Override
  public void render(RenderContext context) {
    if (context.textureManager == null) {
      return;
    }

    // Minecraft expects a 256x256 texture to render.
    // if we provide it with a smaller size, it will stretch out the texture.
    // so we let it do that, and simply scale the screen. This means we will need to re-calculate the position.
    // this will always draw the top-left corner at x-y, and display the (possibly rescaled) texture.
    // (admittedly I don't fully understand why this works)
    float scaleX = (float)this.texture.width / 256 * this.scale;
    float scaleY = (float)this.texture.height / 256 * this.scale;
    int u = 0, v = 0; // offset, with repeating boundaries in the 256x256 box

    // position and size in the transformed (scaled) space
    int renderX = (int)(this.x / scaleX);
    int renderY = (int)(this.y / scaleY);
    int width = 256;
    int height = 256;

    // note: it is important to use GlStateManager for two reasons:
    // 1. better efficiency
    // 2. calling the manager is used to keep track of state changes independently to GL11.* calls, and minecraft
    //    uses the state manager - thus we would get unexpected behaviour if we use GL11 methods.
    // see https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/2231761-opengl-calls-glstatemanager
    GlStateManager.pushMatrix();
    GlStateManager.scale(scaleX, scaleY, 1);
    GlStateManager.enableBlend();

    context.textureManager.bindTexture(this.texture.resourceLocation);
    this.drawTexturedModalRect(renderX, renderY, u, v, width, height);

    GlStateManager.popMatrix();
  }
}
