package dev.rebel.chatmate_1_21_1.api.publicObjects.event;

import dev.rebel.chatmate_1_21_1.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate_1_21_1.api.publicObjects.user.PublicUser;

import javax.annotation.Nullable;

public class PublicRankUpdateData {
  public PublicRank.RankName rankName;
  public Boolean isAdded;
  public @Nullable PublicUser appliedBy;
  public PublicUser user;
  public PublicPlatformRank[] platformRanks;
}
