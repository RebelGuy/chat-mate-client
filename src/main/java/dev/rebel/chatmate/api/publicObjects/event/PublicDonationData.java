package dev.rebel.chatmate.api.publicObjects.event;

import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

import javax.annotation.Nullable;

public class PublicDonationData {
  public Integer id;
  public Long time;
  public Float amount;
  public String formattedAmount;
  public String currency;
  public String name;
  public PublicMessagePart[] messageParts;
  public @Nullable PublicUser linkedUser;
}
