package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.style.Colour;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ImageChatComponent extends ChatComponentBase {
  public final Dim paddingGuiLeft;
  public final Dim paddingGuiRight;
  private final @Nullable Dim maxHeight;
  private @Nullable Texture texture;
  private Colour colour;
  private final Supplier<Texture> textureSupplier;

  public ImageChatComponent(Supplier<Texture> textureSupplier, Dim paddingGuiLeft, Dim paddingGuiRight) {
    this(textureSupplier, paddingGuiLeft, paddingGuiRight, null);
  }

  public ImageChatComponent(Supplier<Texture> textureSupplier, Dim paddingGuiLeft, Dim paddingGuiRight, @Nullable Dim maxHeight) {
    super();
    this.textureSupplier = textureSupplier;
    this.paddingGuiLeft = paddingGuiLeft;
    this.paddingGuiRight = paddingGuiRight;
    this.maxHeight = maxHeight;
    this.colour = new Colour(1.0f, 1.0f, 1.0f);
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
    Dim effectiveHeight = this.getEffectiveHeight(guiHeight);
    return effectiveHeight.times(aspectRatio);
  }

  /** Returns the horizontal space required to display this image, in GUI units. */
  public Dim getRequiredWidth(Dim guiHeight) {
    Dim effectiveHeight = this.getEffectiveHeight(guiHeight);
    return this.getImageWidth(effectiveHeight).plus(this.paddingGuiLeft).plus(this.paddingGuiRight);
  }

  public Dim getEffectiveHeight(Dim guiHeight) {
    return this.maxHeight == null ? guiHeight : Dim.min(this.maxHeight, guiHeight);
  }

  public void setColour(Colour colour) {
    this.colour = colour;
  }

  public Colour getColour() {
    return this.colour;
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
