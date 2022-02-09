package dev.rebel.chatmate;

import net.minecraft.util.ResourceLocation;

public class Asset {
  public static Texture STATUS_INDICATOR_RED = new Texture(16, 16, "textures/status_indicator_red.png");
  public static Texture STATUS_INDICATOR_ORANGE = new Texture(16, 16, "textures/status_indicator_orange.png");
  public static Texture STATUS_INDICATOR_CYAN = new Texture(16, 16, "textures/status_indicator_cyan.png");
  public static Texture STATUS_INDICATOR_GREEN = new Texture(16, 16, "textures/status_indicator_green.png");

  public static class Texture {
    /** In screen units. */
    public final int width;
    /** In screen units. */
    public final int height;
    public final ResourceLocation resourceLocation;

    public Texture(int width, int height, String resourceLocation) {
      this.width = width;
      this.height = height;
      this.resourceLocation = new ResourceLocation("chatmate", resourceLocation);
    }
  }
}
