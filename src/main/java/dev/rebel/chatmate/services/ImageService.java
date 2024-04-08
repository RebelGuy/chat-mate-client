package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Asset.Texture;
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

  public ImageService(Minecraft minecraft, LogService logService) {
    this.minecraft = minecraft;
    this.logService = logService;
  }

  /** Creates a texture from the base64 image data. IMPORTANT: MUST BE RUN ON THE MAIN RENDER THREAD. */
  public @Nullable Texture createTexture(String imageData) {
    try {
      byte[] encodedBytes = imageData.getBytes(StandardCharsets.UTF_8);
      byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);

      BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
      ResourceLocation location = this.minecraft.getTextureManager().getDynamicTextureLocation("test", new DynamicTexture(bufferedImage));
      return new Texture(bufferedImage.getWidth(), bufferedImage.getHeight(), location);
    } catch (Exception e) {
      this.logService.logError(this, "Unable to create texture from the given imageData:", e);
      return null;
    }
  }

  public @Nullable Texture createTextureFromUrl(String imageUrl) {
    try {
      byte[] imageBytes = this.downloadImage(imageUrl);
      @Nullable BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (bufferedImage == null) {
        throw new Exception("Buffered image is null - probably corrupt data.");
      }

      ResourceLocation location = this.minecraft.getTextureManager().getDynamicTextureLocation("test", new DynamicTexture(bufferedImage));
      return new Texture(bufferedImage.getWidth(), bufferedImage.getHeight(), location);
    } catch (Exception e) {
      this.logService.logError(this, "Unable to create texture from the given url:", e);
      return null;
    }
  }

  private byte[] downloadImage(String url) throws Exception {
    // copied from a random SO answer, I hope this works lol
    // https://stackoverflow.com/a/45560205
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
  }
}
