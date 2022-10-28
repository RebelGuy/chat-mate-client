package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart.MessagePartType;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessageText;
import dev.rebel.chatmate.api.publicObjects.emoji.PublicCustomEmoji;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.TextHelpers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessagePartsElement extends InlineElement {
  private float scale = 1;
  private final List<LabelElement> labelElements = new ArrayList<>();
  private final List<ImageElement> imageElements = new ArrayList<>();

  public MessagePartsElement(InteractiveContext context, IElement parent) {
    super(context, parent);
  }

  public MessagePartsElement setMessageParts(List<PublicMessagePart> messageParts) {
    super.context.renderer.runSideEffect(() -> {
      if (Collections.any(messageParts, p -> p.type == MessagePartType.emoji || p.type == MessagePartType.cheer)) {
        throw new RuntimeException("MessagePartsElement only support text and custom emoji parts at the moment");
      }

      super.clear();

      for (int i = 0; i < messageParts.size(); i++) {
        PublicMessagePart part = messageParts.get(i);
        @Nullable MessagePartType prevType = i == 0 ? null : messageParts.get(i - 1).type;
        @Nullable MessagePartType nextType = i == messageParts.size() - 1 ? null : messageParts.get(i + 1).type;

        List<PublicMessagePart> splitParts = this.splitPart(part);

        // when transitioning between text and custom emojis that are explicitly separated by a space, remove that
        // space because the padding between the label and image elements already leads to an implicit space.
        if (part.type == MessagePartType.text && nextType == MessagePartType.customEmoji && Objects.equals(Collections.last(splitParts).textData.text, "")) {
          // text -> custom emoji: discard the last text part
          splitParts = splitParts.subList(0, splitParts.size() - 1);
        } else if (prevType == MessagePartType.customEmoji && part.type == MessagePartType.text && Objects.equals(Collections.first(splitParts).textData.text, "")) {
          // custom emoji -> text: discard the first text part
          splitParts = splitParts.subList(1, splitParts.size());
        }

        for (PublicMessagePart subPart : splitParts) {
          if (subPart.type == MessagePartType.text) {
            LabelElement labelElement = this.createLabelElement(subPart.textData);
            this.labelElements.add(labelElement);
            super.addElement(labelElement);
          } else if (subPart.type == MessagePartType.customEmoji) {
            ImageElement imageElement = this.createImageElement(subPart.customEmojiData.customEmoji);
            this.imageElements.add(imageElement);
            super.addElement(imageElement);
          } else {
            throw new RuntimeException("MessagePartsElement only support text and custom emoji parts at the moment");
          }
        }
      }

      super.onInvalidateSize();
    });
    return this;
  }

  public MessagePartsElement setScale(float scale) {
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

  private LabelElement createLabelElement(PublicMessageText text) {
    Font font = new Font()
        .withBold(text.isBold)
        .withItalic(text.isItalics)
        .withShadow(new Shadow(super.context.dimFactory));

    return new LabelElement(super.context, this)
        .setText(text.text)
        .setFontScale(this.scale)
        .setFont(font)
        .setPadding(this.getPaddingForParts())
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();
  }

  private ImageElement createImageElement(PublicCustomEmoji emoji) {
    return new ImageElement(super.context, this)
        .setImage(super.context.imageService.createTexture(emoji.imageData))
        .setTargetContentHeight(super.context.fontEngine.FONT_HEIGHT_DIM.times(this.scale))
        .setPadding(this.getPaddingForParts())
        .cast();
  }

  private RectExtension getPaddingForParts() {
    return new RectExtension(gui(2 * this.scale));
  }

  private List<PublicMessagePart> splitPart(PublicMessagePart part) {
    if (part.type == MessagePartType.text) {
      return Collections.map(
          TextHelpers.split(part.textData.text, " "),
          str -> new PublicMessagePart() {{
            type = part.type;
            textData = new PublicMessageText() {{
              text = str;
              isItalics = part.textData.isItalics;
              isBold = part.textData.isBold;
            }};
          }}
      );
    } else {
      return Collections.list(part);
    }
  }
}
