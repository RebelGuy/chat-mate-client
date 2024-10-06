package dev.rebel.chatmate.api.publicObjects.donation;

import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

import javax.annotation.Nullable;

public class PublicDonation {
  public Integer id;
  public Long time;
  public Float amount;
  public String formattedAmount;
  public String currency;
  public String name;
  public PublicMessagePart[] messageParts;
  public String linkIdentifier;
  public @Nullable PublicUser linkedUser;
  public @Nullable Long linkedAt;
  public @Nullable Long refundedAt;
  public @Nullable Long deletedAt;
}
