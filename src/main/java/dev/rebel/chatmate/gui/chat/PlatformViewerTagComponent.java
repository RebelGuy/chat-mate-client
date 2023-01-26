package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.events.models.ConfigEventData;
import dev.rebel.chatmate.gui.style.Colour;
import net.minecraft.util.ChatComponentText;

import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.rebel.chatmate.Asset.LOGO_TWITCH;
import static dev.rebel.chatmate.Asset.LOGO_YOUTUBE;

public class PlatformViewerTagComponent extends ContainerChatComponent {
  // since we are drawing these as part of the list of components that make up a chat message, we add artificial leading padding equivalent to an empty character
  private final static BiFunction<DimFactory, Boolean, ImageChatComponent> YOUTUBE_COMPONENT = (df, greyScale) -> new ImageChatComponent(() -> LOGO_YOUTUBE, df.fromGui(3.5f), df.zeroGui(), greyScale);
  private final static BiFunction<DimFactory, Boolean, ImageChatComponent> TWITCH_COMPONENT = (df, greyScale) -> new ImageChatComponent(() -> LOGO_TWITCH, df.fromGui(3.5f), df.zeroGui(), greyScale);

  private final DimFactory dimFactory;
  private final ChatPlatform platform;
  private final boolean greyScale;
  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChangeShowChatPlatformIcon = this::onChangeShowChatPlatformIcon;

  public PlatformViewerTagComponent(DimFactory dimFactory, ChatPlatform platform) {
    this.dimFactory = dimFactory;
    this.platform = platform;
    this.greyScale = false;
    this.setComponent(true);
  }

  public PlatformViewerTagComponent(DimFactory dimFactory, Config config, ChatPlatform platform, boolean greyScale) {
    this.dimFactory = dimFactory;
    this.platform = platform;
    this.greyScale = greyScale;
    config.getShowChatPlatformIconEmitter().onChange(this._onChangeShowChatPlatformIcon, this, true);
  }

  private ConfigEventData.Out<Boolean> onChangeShowChatPlatformIcon(ConfigEventData.In<Boolean> in) {
    this.setComponent(in.data);
    return new ConfigEventData.Out<>();
  }

  private void setComponent(boolean showChatPlatformIcon) {
    if (showChatPlatformIcon) {
      ImageChatComponent image = this.platform == ChatPlatform.Youtube ? YOUTUBE_COMPONENT.apply(this.dimFactory, this.greyScale) : TWITCH_COMPONENT.apply(this.dimFactory, this.greyScale);
      super.setComponent(image);
    } else {
      super.setComponent(new ChatComponentText(""));
    }
  }
}
