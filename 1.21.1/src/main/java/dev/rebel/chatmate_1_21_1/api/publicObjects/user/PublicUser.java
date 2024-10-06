package dev.rebel.chatmate_1_21_1.api.publicObjects.user;

import dev.rebel.chatmate_1_21_1.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate_1_21_1.api.publicObjects.rank.PublicUserRank;

import javax.annotation.Nullable;
import java.util.Arrays;

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
    if (this.registeredUser != null) {
      return this.registeredUser.displayName != null ? this.registeredUser.displayName : this.registeredUser.username;
    } else {
      return  this.channel.displayName;
    }
  }
}
