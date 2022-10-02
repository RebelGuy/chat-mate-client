package dev.rebel.chatmate.api.publicObjects.event;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicChatMateEvent extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 5; }

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
