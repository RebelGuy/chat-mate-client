package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.hud.ImageComponent;
import dev.rebel.chatmate.gui.models.DimFactory;
import net.minecraft.client.Minecraft;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

public class ImageService {

  private final Minecraft minecraft;

  public ImageService(Minecraft minecraft) {
    this.minecraft = minecraft;
  }

  /** Creates a texture from the base64 image data. */
  public @Nullable Texture createTexture(String imageData) {
    byte[] encodedBytes = imageData.getBytes(StandardCharsets.UTF_8);
    byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);

    try {
      BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
      ResourceLocation location = this.minecraft.getTextureManager().getDynamicTextureLocation("test", new DynamicTexture(bufferedImage));
      return new Texture(bufferedImage.getWidth(), bufferedImage.getHeight(), location);
    } catch (IOException e) {
      // todo: log
      return null;
    }
  }
}
