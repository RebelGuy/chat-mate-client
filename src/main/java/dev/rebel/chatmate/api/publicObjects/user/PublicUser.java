package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import java.util.Arrays;

public class PublicUser extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public Integer id;
  public PublicChannelInfo userInfo;
  public PublicLevelInfo levelInfo;
  public PublicUserRank[] activeRanks;

  public PublicUserRank[] getActivePunishments() {
    return Arrays.stream(activeRanks).filter(r -> r.rank.group == PublicRank.RankGroup.PUNISHMENT).toArray(PublicUserRank[]::new);
  }
}
