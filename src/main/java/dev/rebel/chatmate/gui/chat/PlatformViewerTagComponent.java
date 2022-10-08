package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.events.models.ConfigEventData;
import net.minecraft.util.ChatComponentText;

import java.util.function.Function;

import static dev.rebel.chatmate.Asset.LOGO_TWITCH;
import static dev.rebel.chatmate.Asset.LOGO_YOUTUBE;

public class PlatformViewerTagComponent extends ContainerChatComponent {
  // since we are drawing these as part of the list of components that make up a chat message, we add artificial leading padding equivalent to an empty character
  private final static Function<DimFactory, ImageChatComponent> YOUTUBE_COMPONENT = df -> new ImageChatComponent(() -> LOGO_YOUTUBE, df.fromGui(3.5f), df.zeroGui());
  private final static Function<DimFactory, ImageChatComponent> TWITCH_COMPONENT = df -> new ImageChatComponent(() -> LOGO_TWITCH, df.fromGui(3.5f), df.zeroGui());

  private final DimFactory dimFactory;
  private final ChatPlatform platform;
  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChangeShowChatPlatformIcon = this::onChangeShowChatPlatformIcon;

  public PlatformViewerTagComponent(DimFactory dimFactory, ChatPlatform platform) {
    this.dimFactory = dimFactory;
    this.platform = platform;
    this.setComponent(true);
  }

  public PlatformViewerTagComponent(DimFactory dimFactory, Config config, ChatPlatform platform) {
    this.dimFactory = dimFactory;
    this.platform = platform;
    config.getShowChatPlatformIconEmitter().onChange(this._onChangeShowChatPlatformIcon, this, true);
  }

  private ConfigEventData.Out<Boolean> onChangeShowChatPlatformIcon(ConfigEventData.In<Boolean> in) {
    this.setComponent(in.data);
    return new ConfigEventData.Out<>();
  }

  private void setComponent(boolean showChatPlatformIcon) {
    if (showChatPlatformIcon) {
      super.setComponent(this.platform == ChatPlatform.Youtube ? YOUTUBE_COMPONENT.apply(this.dimFactory) : TWITCH_COMPONENT.apply(this.dimFactory));
    } else {
      super.setComponent(new ChatComponentText(""));
    }
  }
}
