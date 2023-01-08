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
        return new ImagePart(super.context.imageService.createTexture(part.customEmojiData.customEmoji.imageData));
      } else {
        throw new RuntimeException("MessagePartsElement only support text and custom emoji parts at the moment");
      }
    });
    super.setParts(parts);

    return this;
  }
}
