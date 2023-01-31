package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import javax.annotation.Nullable;
import java.util.Arrays;

public class PublicUser {
  public Integer primaryUserId;
  public @Nullable PublicRegisteredUser registeredUser;
  public PublicChannelInfo channelInfo;
  public PublicLevelInfo levelInfo;
  public PublicUserRank[] activeRanks;

  public PublicUserRank[] getActivePunishments() {
    return Arrays.stream(activeRanks).filter(r -> r.rank.group == PublicRank.RankGroup.PUNISHMENT).toArray(PublicUserRank[]::new);
  }
}
