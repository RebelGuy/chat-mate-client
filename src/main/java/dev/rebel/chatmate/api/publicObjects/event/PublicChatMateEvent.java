package dev.rebel.chatmate.api.publicObjects.event;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class PublicChatMateEvent {
  public ChatMateEventType type;
  public Long timestamp;
  public @Nullable PublicLevelUpData levelUpData;
  public @Nullable PublicNewTwitchFollowerData newTwitchFollowerData;
  public @Nullable PublicDonationData donationData;
  public @Nullable PublicNewViewerData newViewerData;
  public @Nullable PublicChatMessageDeletedData chatMessageDeletedData;
  public @Nullable PublicRankUpdateData rankUpdateData;
  public @Nullable PublicLiveReactionsData liveReactionsData;

  public enum ChatMateEventType {
    @SerializedName("levelUp") LEVEL_UP,
    @SerializedName("newTwitchFollower") NEW_TWITCH_FOLLOWER,
    @SerializedName("donation") DONATION,
    @SerializedName("newViewer") NEW_VIEWER,
    @SerializedName("chatMessageDeleted") CHAT_MESSAGE_DELETED,
    @SerializedName("rankUpdate") RANK_UPDATE,
    @SerializedName("liveReactions") LIVE_REACTIONS
  }
}
