package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.event.PublicPlatformRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

import java.util.List;

public class RankUpdatedEventData {
  public final PublicRank.RankName rankName;
  public final boolean isAdded;
  public final PublicUser user;
  public final List<PublicPlatformRank> platformRanks;

  public RankUpdatedEventData(PublicRank.RankName rankName, boolean isAdded, PublicUser user, List<PublicPlatformRank> platformRanks) {
    this.rankName = rankName;
    this.isAdded = isAdded;
    this.user = user;
    this.platformRanks = platformRanks;
  }
}
