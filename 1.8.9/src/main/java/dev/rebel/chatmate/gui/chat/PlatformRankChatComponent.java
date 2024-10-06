package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.api.publicObjects.event.PublicPlatformRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

import static dev.rebel.chatmate.util.EnumHelpers.mapEnum;

public class PlatformRankChatComponent extends ChatComponentBase {
  private final PublicRank.RankName rankName;
  private final boolean isAdded;
  private final List<PublicPlatformRank> platformRanks;

  public PlatformRankChatComponent(PublicRank.RankName rankName, boolean isAdded, List<PublicPlatformRank> platformRanks) {
    this.rankName = rankName;
    this.isAdded = isAdded;
    this.platformRanks = platformRanks;
  }

  public List<String> getHoverLines() {
    List<String> lines = new ArrayList<>();
    if (this.platformRanks.size() == 0) {
      return lines;
    }

    lines.add("Platform updates:");

    List<PublicPlatformRank> youtubeRanks = Collections.filter(this.platformRanks, r -> r.platform == PublicChatItem.ChatPlatform.Youtube);
    List<PublicPlatformRank> twitchRanks = Collections.filter(this.platformRanks, r -> r.platform == PublicChatItem.ChatPlatform.Twitch);

    for (PublicPlatformRank platformRank : youtubeRanks) {
      lines.add(String.format("%s (Youtube) %s", platformRank.channelName, platformRank.success ? "✓" : "x"));
    }
    for (PublicPlatformRank platformRank : twitchRanks) {
      lines.add(String.format("%s (Twitch) %s", platformRank.channelName, platformRank.success ? "✓" : "x"));
    }

    return lines;
  }

  public ChatComponentText getChatComponentText() {
    ChatComponentText component = new ChatComponentText(this.getUnformattedTextForChat());
    component.setChatStyle(this.getChatStyle());
    return component;
  }

  @Override
  public String getUnformattedTextForChat() {
    if (this.isAdded) {
      return mapEnum(this.rankName, null,
          new Tuple2<>(PublicRank.RankName.BAN, "banned"),
          new Tuple2<>(PublicRank.RankName.TIMEOUT, "timed out"),
          new Tuple2<>(PublicRank.RankName.MUTE, "muted"),
          new Tuple2<>(PublicRank.RankName.MOD, "modded")
      );
    } else {
      return mapEnum(this.rankName, null,
          new Tuple2<>(PublicRank.RankName.BAN, "unbanned"),
          new Tuple2<>(PublicRank.RankName.TIMEOUT, "un-timed out"),
          new Tuple2<>(PublicRank.RankName.MUTE, "unmuted"),
          new Tuple2<>(PublicRank.RankName.MOD, "unmodded")
      );
    }
  }

  @Override
  public IChatComponent createCopy() {
    return this;
  }
}
