package dev.rebel.chatmate.models.publicObjects.punishment;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicPunishment extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public PunishmentType type;
  public Long issuedAt;
  public Boolean isActive;
  public @Nullable Long expirationTime;
  public @Nullable String message;
  public @Nullable Long revokedAt;
  public @Nullable String revokeMessage;

  public enum PunishmentType {
    @SerializedName("ban") BAN,
    @SerializedName("timeout") TIMEOUT,
    @SerializedName("mute") MUTE,
  }
}
