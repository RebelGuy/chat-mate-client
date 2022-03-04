package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.Asset.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ImageChatComponent extends ChatComponentBase {
  public final int paddingGui = 1; // padding in gui units

  private @Nullable Texture texture;
  private final Supplier<Texture> textureSupplier;

  public ImageChatComponent(Supplier<Texture> textureSupplier) {
    super();
    this.textureSupplier = textureSupplier;
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
