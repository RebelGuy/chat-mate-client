package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.util.ResolvableTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ImageChatComponent extends ChatComponentBase {
  public final Dim paddingGuiLeft;
  public final Dim paddingGuiRight;
  private final @Nullable Dim maxHeight;
  private @Nullable Texture texture;
  private Colour colour;
  private final @Nullable Supplier<Texture> textureSupplier;
  private final @Nullable ResolvableTexture resolvableTexture;
  private final boolean greyScale;

  public ImageChatComponent(Supplier<Texture> textureSupplier, Dim paddingGuiLeft, Dim paddingGuiRight, boolean greyScale) {
    this(textureSupplier, paddingGuiLeft, paddingGuiRight, greyScale, null);
  }

  public ImageChatComponent(@Nonnull Supplier<Texture> textureSupplier, Dim paddingGuiLeft, Dim paddingGuiRight, boolean greyScale, @Nullable Dim maxHeight) {
    super();
    this.textureSupplier = textureSupplier;
    this.resolvableTexture = null;
    this.paddingGuiLeft = paddingGuiLeft;
    this.paddingGuiRight = paddingGuiRight;
    this.maxHeight = maxHeight;
    this.greyScale = greyScale;
    this.colour = new Colour(1.0f, 1.0f, 1.0f);
  }

  public ImageChatComponent(ResolvableTexture resolvableTexture, Dim paddingGuiLeft, Dim paddingGuiRight, boolean greyScale) {
    this(resolvableTexture, paddingGuiLeft, paddingGuiRight, greyScale, null);
  }

  public ImageChatComponent(@Nonnull ResolvableTexture resolvableTexture, Dim paddingGuiLeft, Dim paddingGuiRight, boolean greyScale, @Nullable Dim maxHeight) {
    super();
    this.textureSupplier = null;
    this.resolvableTexture = resolvableTexture;
    this.paddingGuiLeft = paddingGuiLeft;
    this.paddingGuiRight = paddingGuiRight;
    this.maxHeight = maxHeight;
    this.greyScale = greyScale;
    this.colour = new Colour(1.0f, 1.0f, 1.0f);
  }

  /** This must be called from the Minecraft thread, as it may initialise the texture and requires the OpenGL context. */
  public @Nullable Texture getTexture() {
    if (this.textureSupplier != null) {
      if (this.texture == null) {
        this.texture = this.textureSupplier.get();
      }

      return this.texture;
    } else if (this.resolvableTexture != null) {
      this.resolvableTexture.begin();
      return this.resolvableTexture.getResolvedTexture();
    } else {
      throw new RuntimeException("Unable to get texture (did not expect to get here)");
    }
  }

  public void destroy(TextureManager textureManager) {
    if (this.texture != null) {
      textureManager.deleteTexture(this.texture.resourceLocation);
    }

    if (this.resolvableTexture != null && this.resolvableTexture.getResolvedTexture() == null) {
      this.resolvableTexture.abort();
    }
  }

  /** Returns the image width, in GUI units. */
  public Dim getImageWidth(Dim guiHeight) {
    @Nullable Texture texture = this.getTexture();
    int width;
    int height;

    if (texture != null) {
      width = texture.width;
      height = texture.height;
    } else if (this.resolvableTexture != null) {
      width = this.resolvableTexture.width;
      height = this.resolvableTexture.height;
    } else {
      // this should be impossible
      throw new RuntimeException("Unable to get image width (did not expect to get here)");
    }

    float aspectRatio = (float)width / height;
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

  public boolean getGreyScale() {
    return this.greyScale;
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
