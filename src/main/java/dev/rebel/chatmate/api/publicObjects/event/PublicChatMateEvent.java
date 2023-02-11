package dev.rebel.chatmate.api.publicObjects.event;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class PublicChatMateEvent {
  public ChatMateEventType type;
  public Long timestamp;
  public @Nullable PublicLevelUpData levelUpData;
  public @Nullable PublicNewTwitchFollowerData newTwitchFollowerData;
  public @Nullable PublicDonationData donationData;

  public enum ChatMateEventType {
    @SerializedName("levelUp") LEVEL_UP,
    @SerializedName("newTwitchFollower") NEW_TWITCH_FOLLOWER,
    @SerializedName("donation") DONATION
  }
}
