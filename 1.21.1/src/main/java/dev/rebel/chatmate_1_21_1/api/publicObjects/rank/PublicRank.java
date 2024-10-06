package dev.rebel.chatmate_1_21_1.api.publicObjects.rank;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class PublicRank {
  public Number id;
  public RankName name;
  public RankGroup group;
  public String displayNameNoun;
  public String displayNameAdjective;
  public @Nullable String description;

  public enum RankName {
    @SerializedName("admin") ADMIN,
    @SerializedName("owner") OWNER,
    @SerializedName("famous") FAMOUS,
    @SerializedName("mod") MOD,

    @SerializedName("ban") BAN,
    @SerializedName("timeout") TIMEOUT,
    @SerializedName("mute") MUTE,

    @SerializedName("donator") DONATOR,
    @SerializedName("supporter") SUPPORTER,
    @SerializedName("member") MEMBER,
  }

  public enum RankGroup {
    @SerializedName("administration") ADMINISTRATION,
    @SerializedName("cosmetic") COSMETIC,
    @SerializedName("punishment") PUNISHMENT,
    @SerializedName("donation") DONATION,
  }
}
