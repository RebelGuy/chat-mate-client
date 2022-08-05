package dev.rebel.chatmate.models.publicObjects.rank;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicRank extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public RankName name;
  public RankGroup group;
  public String displayNameNoun;
  public String displayNameAdjective;
  public @Nullable String description;

  public enum RankName {
    @SerializedName("owner") OWNER,
    @SerializedName("famous") FAMOUS,
    @SerializedName("mod") MOD,
    @SerializedName("ban") BAN,
    @SerializedName("timeout") TIMEOUT,
    @SerializedName("mute") MUTE,
  }

  public enum RankGroup {
    @SerializedName("administration") ADMINISTRATION,
    @SerializedName("cosmetic") COSMETIC,
    @SerializedName("punishment") PUNISHMENT,
    @SerializedName("donation") DONATION,
  }
}
