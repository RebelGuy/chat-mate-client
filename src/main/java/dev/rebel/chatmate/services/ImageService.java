package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.util.ResolvableTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ImageService {

  private final Minecraft minecraft;
  private final LogService logService;
  private final PersistentCacheService persistentCacheService;

  public ImageService(Minecraft minecraft, LogService logService, PersistentCacheService persistentCacheService) {
    this.minecraft = minecraft;
    this.logService = logService;
    this.persistentCacheService = persistentCacheService;
  }

  /** Creates a texture from the base64 image data. IMPORTANT: MUST BE RUN ON THE MAIN RENDER THREAD. */
  public @Nullable Texture createTexture(String imageData) {
    try {
      byte[] encodedBytes = imageData.getBytes(StandardCharsets.UTF_8);
      byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);
      BufferedImage bufferedImage = this.bufferedImageFromBytes(decodedBytes);
      if (bufferedImage == null) {
        this.logService.logError(this, "Unable to convert the image bytes to a buffered image - probably corrupt data.");
        return null;
      }

      ResourceLocation location = this.minecraft.getTextureManager().getDynamicTextureLocation("test", new DynamicTexture(bufferedImage));
      return new Texture(bufferedImage.getWidth(), bufferedImage.getHeight(), location);
    } catch (Exception e) {
      this.logService.logError(this, "Unable to create texture from the given imageData:", e);
      return null;
    }
  }

  /** Returns null if something went wrong. */
  public @Nullable BufferedImage bufferedImageFromBytes(byte[] bytes) {
    try {
      @Nullable BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
      if (image == null) {
        this.logService.logError(this, "Unable to create a texture from the given image bytes. Image may be corrupted.");
      }
      return image;

    } catch (Exception e) {
      this.logService.logError(this, "Unable to create texture from the given image bytes:", e);
      return null;
    }
  }

  /** Returns null if something went wrong. */
  public @Nullable byte[] bytesFromBufferedImage(BufferedImage image) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", stream);
      return stream.toByteArray();
    } catch (Exception e) {
      this.logService.logError(this, "Unable to convert the buffered image to a string:", e);
      return null;
    }
  }

  /** Creates a texture from a HTTP image URL. Can be run on any thread. */
  public ResolvableTexture createCacheableTextureFromUrl(int width, int height, String imageUrl, @Nullable String cacheKey) {
    return new ResolvableTexture(this.minecraft, this.persistentCacheService, this, width, height, () -> this.downloadImageBytes(imageUrl), cacheKey);
  }

  private byte[] downloadImageBytes(String url) {
    // copied from a random SO answer, I hope this works lol
    // https://stackoverflow.com/a/45560205
    try {
      URL imageUrl = new URL(url);
      URLConnection connection = imageUrl.openConnection();
      InputStream inputStream = connection.getInputStream();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int read;
      while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
        outputStream.write(buffer, 0, read);
      }
      outputStream.flush();

      return outputStream.toByteArray();
    } catch (Exception e) {
      this.logService.logError(this, "Unable to download image bytes from", url, e);
      return null;
    }
  }
}
