package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.models.Dim;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ImageChatComponent extends ChatComponentBase {
  public final Dim paddingGuiLeft;
  public final Dim paddingGuiRight;
  private @Nullable Texture texture;
  private final Supplier<Texture> textureSupplier;

  public ImageChatComponent(Supplier<Texture> textureSupplier, Dim paddingGuiLeft, Dim paddingGuiRight) {
    super();
    this.textureSupplier = textureSupplier;
    this.paddingGuiLeft = paddingGuiLeft;
    this.paddingGuiRight = paddingGuiRight;
  }

  /** This must be called from the Minecraft thread, as it may initialise the texture and requires the OpenGL context. */
  public Texture getTexture() {
    if (this.texture == null) {
      this.texture = this.textureSupplier.get();
    }

    return this.texture;
  }

  public void destroy(TextureManager textureManager) {
    if (this.texture != null) {
      textureManager.deleteTexture(this.texture.resourceLocation);
    }
  }

  /** Returns the image width, in GUI units. */
  public Dim getImageWidth(Dim guiHeight) {
    Texture texture = this.getTexture();
    float aspectRatio = (float)texture.width / texture.height;
    return guiHeight.times(aspectRatio);
  }

  /** Returns the horizontal space required to display this image, in GUI units. */
  public Dim getRequiredWidth(Dim guiHeight) {
    return this.getImageWidth(guiHeight).plus(this.paddingGuiLeft).plus(this.paddingGuiRight);
  }

  @Override
  public String getUnformattedTextForChat() {
    return "";
  }

  @Override
  public IChatComponent createCopy() {
    // this gets called when iterating over a component of which this one is a child of, but we don't like that
    return this;
  }
}
