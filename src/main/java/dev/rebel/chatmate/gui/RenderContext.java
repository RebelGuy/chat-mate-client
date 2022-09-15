package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.services.MinecraftProxyService;
import net.minecraft.client.renderer.texture.TextureManager;

import javax.annotation.Nullable;

public class RenderContext {
  public final @Nullable TextureManager textureManager;
  public final FontEngine fontEngine;

  public RenderContext(@Nullable TextureManager textureManager, FontEngine fontEngine) {
    this.textureManager = textureManager;
    this.fontEngine = fontEngine;
  }
}
