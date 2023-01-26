package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart.MessagePartType;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessageText;
import dev.rebel.chatmate.api.publicObjects.emoji.PublicCustomEmoji;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.TextHelpers;
import org.w3c.dom.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.Objects.casted;

/** Inline label/image elements with correct word wrapping and spacing. */
public class LabelWithImagesElement extends InlineElement {
  private float scale = 1;
  private final List<LabelElement> labelElements = new ArrayList<>();
  private final List<ImageElement> imageElements = new ArrayList<>();
  private boolean colouriseImage = false;
  private Colour colour = Colour.WHITE;
  private @Nullable BiConsumer<ImageElement, Integer> imageProcessor = null;
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;

  public LabelWithImagesElement(InteractiveContext context, IElement parent) {
    super(context, parent);
  }

  /** Applies the given colour to all text and, if `colouriseImage` is true, images as well. */
  public LabelWithImagesElement setColour(Colour colour) {
    this.colour = colour;
    return this;
  }

  /** If true, images will be colourised according to the colour set in `setColour()`. */
  public LabelWithImagesElement setColouriseImage(boolean colouriseImage) {
    this.colouriseImage = colouriseImage;
    return this;
  }

  public LabelWithImagesElement setChildrenHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
    if (this.horizontalAlignment != horizontalAlignment) {
      this.horizontalAlignment = horizontalAlignment;
      super.onInvalidateSize();
    }
    return this;
  }

  public LabelWithImagesElement setParts(List<IPart> parts) {
    super.context.renderer.runSideEffect(() -> {
      super.clear();

      for (int i = 0; i < parts.size(); i++) {
        IPart part = parts.get(i);
        Type type = part.getType();
        @Nullable Type prevType = i == 0 ? null : parts.get(i - 1).getType();
        @Nullable Type nextType = i == parts.size() - 1 ? null : parts.get(i + 1).getType();

        List<IPart> splitParts = this.splitPart(part);

        // when transitioning between text and images that are explicitly separated by a space, remove that
        // space because the padding between the label and image elements already leads to an implicit space.
        if (type == Type.TEXT && nextType == Type.IMAGE && Objects.equals(casted(TextPart.class, Collections.last(splitParts), p -> p.text), "")) {
          // text -> image: discard the last text part (we know it's of type text because it was split from the current part, which is of type text)
          splitParts = splitParts.subList(0, splitParts.size() - 1);
        } else if (prevType == Type.IMAGE && part.getType() == Type.TEXT && Objects.equals(casted(TextPart.class, Collections.first(splitParts), p -> p.text), "")) {
          // custom emoji -> text: discard the first text part
          splitParts = splitParts.subList(1, splitParts.size());
        }

        for (IPart subPart : splitParts) {
          if (subPart.getType() == Type.TEXT) {
            LabelElement labelElement = this.createLabelElement((TextPart)subPart);
            this.labelElements.add(labelElement);
            super.addElement(labelElement);
          } else if (subPart.getType() == Type.IMAGE) {
            ImageElement imageElement = this.createImageElement((ImagePart)subPart);
            this.imageElements.add(imageElement);
            super.addElement(imageElement);
          } else {
            throw new RuntimeException("MessagePartsElement only support text and custom emoji parts at the moment");
          }
        }
      }

      if (this.imageProcessor != null) {
        int i = 0;
        for (ImageElement elemnent : this.imageElements) {
          this.imageProcessor.accept(elemnent, i);
          i++;
        }
      }

      super.onInvalidateSize();
    });
    return this;
  }

  public LabelWithImagesElement setScale(float scale) {
    if (this.scale != scale) {
      this.scale = scale;
      this.labelElements.forEach(el -> {
        el.setFontScale(scale);
        el.setPadding(this.getPaddingForParts());
      });
      this.imageElements.forEach(el -> {
        el.setTargetContentHeight(super.context.fontEngine.FONT_HEIGHT_DIM.times(this.scale));
        el.setPadding(this.getPaddingForParts());
      });
      super.onInvalidateSize();
    }
    return this;
  }

  /** A processor to call for every image element, once generated. The second argument is the image's id in the list. Should be set before any parts are added. */
  public LabelWithImagesElement setImageProcessor(BiConsumer<ImageElement, Integer> processor) {
    this.imageProcessor = processor;
    return this;
  }

  private LabelElement createLabelElement(TextPart part) {
    return new LabelElement(super.context, this)
        .setText(part.text)
        .setFontScale(this.scale)
        .setFont(part.font.withColour(c -> this.colour == null ? c : this.colour))
        .setPadding(this.getPaddingForParts())
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setHorizontalAlignment(this.horizontalAlignment)
        .cast();
  }

  private ImageElement createImageElement(ImagePart part) {
    ImageElement element = new ImageElement(super.context, this)
        .setImage(part.texture)
        .setTargetContentHeight(super.context.fontEngine.FONT_HEIGHT_DIM.times(this.scale))
        .setPadding(this.getPaddingForParts())
        .setHorizontalAlignment(this.horizontalAlignment)
        .cast();

    if (this.colouriseImage) {
      return element.setColour(colour);
    } else {
      return element;
    }
  }

  private RectExtension getPaddingForParts() {
    return new RectExtension(gui(2 * this.scale));
  }

  private List<IPart> splitPart(IPart part) {
    if (part.getType() == Type.TEXT) {
      TextPart textPart = (TextPart)part;
      return Collections.map(
          TextHelpers.split(textPart.text, " "),
          str -> new TextPart(str, textPart.font)
      );
    } else {
      return Collections.list(part);
    }
  }

  public interface IPart {
    Type getType();
  }

  public enum Type { TEXT, IMAGE }

  public static class TextPart implements IPart {
    public final String text;
    public final Font font;

    public TextPart(String text, Font font) {
      this.text = text;
      this.font = font;
    }

    @Override
    public Type getType() {
      return Type.TEXT;
    }
  }

  public static class ImagePart implements IPart {
    public final Texture texture;

    public ImagePart(Texture texture) {
      this.texture = texture;
    }

    @Override
    public Type getType() {
      return Type.IMAGE;
    }
  }
}
