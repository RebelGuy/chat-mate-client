package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem.ChatPlatform;
import net.minecraft.util.ChatComponentText;

import java.util.function.Consumer;

import static dev.rebel.chatmate.Asset.LOGO_TWITCH;
import static dev.rebel.chatmate.Asset.LOGO_YOUTUBE;

public class PlatformViewerTagComponent extends ContainerChatComponent {
  // since we are drawing these as part of the list of components that make up a chat message, we add artificial leading padding equivalent to an empty character
  private final static ImageChatComponent YOUTUBE_COMPONENT = new ImageChatComponent(() -> LOGO_YOUTUBE, 3.5f, 0);
  private final static ImageChatComponent TWITCH_COMPONENT = new ImageChatComponent(() -> LOGO_TWITCH, 3.5f, 0);

  private final ChatPlatform platform;
  private final Consumer<Boolean> _onChangeIdentifyPlatforms = this::setComponent;

  public PlatformViewerTagComponent(ChatPlatform platform) {
    this.platform = platform;
    this.setComponent(true);
  }

  public PlatformViewerTagComponent(Config config, ChatPlatform platform) {
    this.platform = platform;
    this.setComponent(config.getIdentifyPlatforms().get());
    config.getIdentifyPlatforms().onChange(this._onChangeIdentifyPlatforms, this);
  }

  private void setComponent(boolean identifyPlatforms) {
    if (identifyPlatforms) {
      super.setComponent(this.platform == ChatPlatform.Youtube ? YOUTUBE_COMPONENT : TWITCH_COMPONENT);
    } else {
      super.setComponent(new ChatComponentText(""));
    }
  }
}
