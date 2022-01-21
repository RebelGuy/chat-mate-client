package dev.rebel.chatmate.gui;

import net.minecraft.client.renderer.texture.TextureManager;

import javax.annotation.Nullable;

public class RenderContext {
  public final @Nullable TextureManager textureManager;

  public RenderContext(@Nullable TextureManager textureManager) {
    this.textureManager = textureManager;
  }
}
