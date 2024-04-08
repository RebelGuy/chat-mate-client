package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart.MessagePartType;
import dev.rebel.chatmate.api.publicObjects.emoji.PublicCustomEmoji;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;

import java.util.List;

public class MessagePartsElement extends LabelWithImagesElement {
  public MessagePartsElement(InteractiveContext context, IElement parent) {
    super(context, parent);
  }

  public MessagePartsElement setMessageParts(List<PublicMessagePart> messageParts) {
    List<IPart> parts = Collections.map(messageParts, part -> {
      if (part.type == MessagePartType.text) {
        Font font = new Font()
            .withBold(part.textData.isBold)
            .withItalic(part.textData.isItalics);
        return new TextPart(part.textData.text, font);
      } else if (part.type == MessagePartType.customEmoji) {
        assert part.customEmojiData != null;
        PublicCustomEmoji customEmoji = part.customEmojiData.customEmoji;
        return new ResolvableImagePart(super.context.imageService.createTextureFromUrl(customEmoji.imageWidth, customEmoji.imageHeight, customEmoji.imageUrl));
      } else {
        throw new RuntimeException("MessagePartsElement only support text and custom emoji parts at the moment");
      }
    });
    super.setParts(parts);

    return this;
  }
}
