package dev.rebel.chatmate.models.publicObjects.punishment;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicPunishment extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public Integer id;
  public PunishmentType type;
  public Long issuedAt;
  public Boolean isActive;
  public @Nullable Long expirationTime;
  public @Nullable String message;
  public @Nullable Long revokedAt;
  public @Nullable String revokeMessage;

  public enum PunishmentType {
    @SerializedName("banned") BAN,
    @SerializedName("timed_out") TIMEOUT,
    @SerializedName("muted") MUTE,
  }
}
