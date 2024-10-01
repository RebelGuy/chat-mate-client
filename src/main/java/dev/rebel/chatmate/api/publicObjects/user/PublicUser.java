package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import javax.annotation.Nullable;
import java.util.Arrays;

import static dev.rebel.chatmate.util.Objects.firstNonNull;

public class PublicUser {
  public Integer primaryUserId;
  public @Nullable PublicRegisteredUser registeredUser;
  public PublicChannel channel;
  public PublicLevelInfo levelInfo;
  public PublicUserRank[] activeRanks;
  public Long firstSeen;

  public PublicUserRank[] getActivePunishments() {
    return Arrays.stream(activeRanks).filter(r -> r.rank.group == PublicRank.RankGroup.PUNISHMENT).toArray(PublicUserRank[]::new);
  }

  public String getDisplayName() {
    return this.registeredUser != null ? firstNonNull(this.registeredUser.displayName, this.registeredUser.username) : this.channel.displayName;
  }
}
