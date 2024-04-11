package dev.rebel.chatmate.util;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.services.ImageService;
import dev.rebel.chatmate.services.PersistentCacheService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ResolvableTexture {
  public final int width;
  public final int height;
  private boolean hasStarted;
  private final @Nullable Minecraft minecraft;
  private final @Nullable PersistentCacheService cacheService;
  private final @Nullable ImageService imageService;
  private @Nullable Texture resolvedTexture;
  private final @Nullable Supplier<BufferedImage> resolvableImage;
  private final @Nullable String cacheKey;
  private @Nullable Consumer<Texture> onResolveCallback;
  private @Nullable Consumer<Throwable> onErrorCallback;

  /** The width and height must be the same as the resolved texture's width and height. */
  public ResolvableTexture(@Nonnull Minecraft minecraft,
                           @Nullable PersistentCacheService cacheService,
                           @Nullable ImageService imageService,
                           int width,
                           int height,
                           @Nonnull Supplier<BufferedImage> resolvableImage,
                           @Nullable String cacheKey) {
    this.width = width;
    this.height = height;
    this.minecraft = minecraft;
    this.cacheService = cacheService;
    this.imageService = imageService;
    this.resolvableImage = resolvableImage;
    this.cacheKey = cacheKey;
  }

  public ResolvableTexture(Texture resolvedTexture) {
    this.width = resolvedTexture.width;
    this.height = resolvedTexture.height;
    this.minecraft = null;
    this.cacheService = null;
    this.imageService = null;
    this.resolvableImage = null;
    this.cacheKey = null;
    this.resolvedTexture = resolvedTexture;
  }

  /** The callback is invoked when the texture has finished loading. The error callback will never be invoked. */
  public void onResolve(@Nullable Consumer<Texture> callback) {
    this.onResolveCallback = callback;

    if (this.resolvedTexture != null && callback != null) {
      callback.accept(this.resolvedTexture);
    }
  }

  /** The callback is invoked when the texture has failed to load. The resolver callback will never be invoked. */
  public void onError(@Nullable Consumer<Throwable> callback) {
    this.onErrorCallback = callback;
  }

  public void begin() {
    if (this.resolvedTexture != null || this.hasStarted) {
      return;
    }

    this.hasStarted = true;

    assert this.resolvableImage != null;

    // use caching if available
    if (this.cacheService != null && this.cacheKey != null) {
      this.cacheService.getValueForKey(this.cacheKey, this::onGetTextureBytes, this::onReceivedTextureBytes);
    } else {
      byte[] bytes = this.onGetTextureBytes();
      this.onReceivedTextureBytes(bytes);
    }
  }

  private byte[] onGetTextureBytes() {
    assert this.imageService != null;

    BufferedImage bufferedImage = this.resolvableImage.get();
    try {
      return this.imageService.bytesFromBufferedImage(bufferedImage);
    } catch (Exception e) {
      return null;
    }
  }

  private void onReceivedTextureBytes(byte[] result) {
    assert this.imageService != null;

    try {
      BufferedImage bufferedImage = this.imageService.bufferedImageFromBytes(result);

      // generating the texture must happen on the main thread, where we have access to openGl
      assert this.minecraft != null;
      this.minecraft.addScheduledTask(() -> {
        ResourceLocation resourceLocation = this.minecraft.getTextureManager().getDynamicTextureLocation("test", new DynamicTexture(bufferedImage));
        this.resolvedTexture = new Texture(this.width, this.height, resourceLocation);
      });

      if (this.onResolveCallback != null) {
        this.onResolveCallback.accept(this.resolvedTexture);
      }

    } catch (Exception e) {
      if (this.onErrorCallback != null) {
        this.onErrorCallback.accept(e);
      }
    }
  }

  public @Nullable Texture getResolvedTexture() {
    return this.resolvedTexture;
  }

  public void abort() {
    // i don't know how to actually stop an in-progress operation so let's just force the callbacks to unsubscribe.
    // does the tree fall in the forest if no one is there to witness it?
    this.onResolveCallback = null;
    this.onErrorCallback = null;
  }
}
