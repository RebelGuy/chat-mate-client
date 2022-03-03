package dev.rebel.chatmate.gui.chat;

import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;

public class ImageChatComponent extends ChatComponentBase {
  public final int paddingGui = 1; // padding in gui units
  public @Nullable ChatImage image;

  public ImageChatComponent(@Nullable ChatImage image) {
    super();
    this.image = image;
  }

  @Override
  public String getUnformattedTextForChat() {
    return "";
  }

  @Override
  public IChatComponent createCopy() {
    return new ImageChatComponent(this.image);
  }
}
