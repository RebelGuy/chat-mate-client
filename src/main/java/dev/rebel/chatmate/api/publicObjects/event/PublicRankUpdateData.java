package dev.rebel.chatmate.api.publicObjects.event;

import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

public class PublicRankUpdateData {
  public PublicRank.RankName rankName;
  public Boolean isAdded;
  public PublicUser user;
  public PublicPlatformRank[] platformRanks;
}
