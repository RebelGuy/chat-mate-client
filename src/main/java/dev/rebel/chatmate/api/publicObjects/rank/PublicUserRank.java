package dev.rebel.chatmate.api.publicObjects.rank;

import javax.annotation.Nullable;

public class PublicUserRank {
  public Integer id;
  public @Nullable String streamer;
  public PublicRank rank;
  public Long issuedAt;
  public Boolean isActive;
  public @Nullable Long expirationTime;
  public @Nullable String message;
  public @Nullable Long revokedAt;
  public @Nullable String revokeMessage;
  public @Nullable String customRankName;
}
