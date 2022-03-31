package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem.ChatPlatform;
import net.minecraft.util.ChatComponentText;

import java.util.function.Consumer;

import static dev.rebel.chatmate.models.Styles.VIEWER_RANK_STYLE;
import static dev.rebel.chatmate.models.Styles.styledText;

public class ViewerTagComponent extends ContainerChatComponent {
  private final static ChatComponentText VIEWER_COMPONENT = styledText("VIEWER", VIEWER_RANK_STYLE);
  private final static ChatComponentText YOUTUBE_COMPONENT = styledText("YOUTUBE", VIEWER_RANK_STYLE);
  private final static ChatComponentText TWITCH_COMPONENT = styledText("TWITCH", VIEWER_RANK_STYLE);

  private final ChatPlatform platform;
  private final Consumer<Boolean> _onChangeIdentifyPlatforms = this::setComponent;

  public ViewerTagComponent(Config config, ChatPlatform platform) {
    this.platform = platform;
    this.setComponent(config.getIdentifyPlatforms().get());
    config.getIdentifyPlatforms().onChange(this._onChangeIdentifyPlatforms, this);
  }

  private void setComponent(boolean identifyPlatforms) {
    if (identifyPlatforms) {
      super.component = this.platform == ChatPlatform.Youtube ? YOUTUBE_COMPONENT : TWITCH_COMPONENT;
    } else {
      super.component = VIEWER_COMPONENT;
    }
  }
}
