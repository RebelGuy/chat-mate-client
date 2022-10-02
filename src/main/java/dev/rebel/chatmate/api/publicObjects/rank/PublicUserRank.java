package dev.rebel.chatmate.api.publicObjects.rank;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicUserRank extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public PublicRank rank;
  public Long issuedAt;
  public Boolean isActive;
  public @Nullable Long expirationTime;
  public @Nullable String message;
  public @Nullable Long revokedAt;
  public @Nullable String revokeMessage;
}
