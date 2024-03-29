package dev.rebel.chatmate;

import net.minecraft.util.ResourceLocation;

public class Asset {
  public static Texture STATUS_INDICATOR_RED = new Texture(16, 16, "textures/status_indicator_red.png");
  public static Texture STATUS_INDICATOR_ORANGE = new Texture(16, 16, "textures/status_indicator_orange.png");
  public static Texture STATUS_INDICATOR_CYAN = new Texture(16, 16, "textures/status_indicator_cyan.png");
  public static Texture STATUS_INDICATOR_BLUE = new Texture(16, 16, "textures/status_indicator_blue.png");
  public static Texture STATUS_INDICATOR_GREEN = new Texture(16, 16, "textures/status_indicator_green.png");
  
  public static Texture GUI_CLEAR_ICON = new Texture(64, 64, "textures/gui/clear_icon.png");
  public static Texture GUI_COPY_ICON = new Texture(64, 64, "textures/gui/copy_icon.png");
  public static Texture GUI_REFRESH_ICON = new Texture(64, 64, "textures/gui/refresh_icon.png");
  public static Texture GUI_TICK_ICON = new Texture(64, 64, "textures/gui/tick_icon.png");
  public static Texture GUI_WEB_ICON = new Texture(64, 64, "textures/gui/web_icon.png");
  public static Texture GUI_EXTERNAL_ICON = new Texture(64, 64, "textures/gui/external_icon.png");
  public static Texture GUI_BIN_ICON = new Texture(64, 64, "textures/gui/bin_icon.png");
  public static Texture GUI_LINK_ICON = new Texture(64, 64, "textures/gui/link_icon.png");
  public static Texture GUI_UNLINK_ICON = new Texture(64, 64, "textures/gui/unlink_icon.png");
  public static Texture GUI_DOLLAR_ICON = new Texture(64, 64, "textures/gui/dollar_icon.png");
  public static Texture GUI_FX_ICON = new Texture(128, 128, "textures/gui/fx_icon.png");
  public static Texture GUI_TEXT_ICON = new Texture(128, 128, "textures/gui/text_icon.png");
  public static Texture GUI_VERIFICATION_ICON = new Texture(128, 128, "textures/gui/verification_icon.png");
  public static Texture GUI_VERIFICATION_ICON_WHITE_SMALL = new Texture(32, 32, "textures/gui/verification_icon_white_small.png"); // use this when recolouring the icon

  public static Texture LOGO_YOUTUBE = new Texture(320, 320, "textures/logo_youtube.png");
  public static Texture LOGO_TWITCH = new Texture(320, 320, "textures/logo_twitch.png");
  public static Texture STAR = new Texture(64, 64, "textures/star.png");

  public static CursorImage CURSOR_DEFAULT = new CursorImage(0, 0, "textures/cursor_default.png");
  public static CursorImage CURSOR_TIP = new CursorImage(0, 0, "textures/cursor_tip.png");
  public static CursorImage CURSOR_WAIT = new CursorImage(7, 12, "textures/cursor_wait.png");
  public static CursorImage CURSOR_TEXT = new CursorImage(16, 16, "textures/cursor_text.png");
  public static CursorImage CURSOR_CLICK = new CursorImage(10, 4, "textures/cursor_click.png");

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

    public Texture(int width, int height, ResourceLocation resourceLocation) {
      this.width = width;
      this.height = height;
      this.resourceLocation = resourceLocation;
    }
  }

  /** Image is considered "upgright" when viewed from the top left corner to the bottom right corner. */
  public static class CursorImage {
    /** From left. */
    public final int xHotspot;

    /** From top. */
    public final int yHotspot;
    public final ResourceLocation resourceLocation;

    public CursorImage(int xHotspot, int yHotspot, String resourceLocation) {
      this.xHotspot = xHotspot;
      this.yHotspot = yHotspot;
      this.resourceLocation = new ResourceLocation("chatmate", resourceLocation);
    }
  }
}
