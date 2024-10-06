package dev.rebel.chatmate_1_21_1.api.publicObjects.event;

import dev.rebel.chatmate_1_21_1.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate_1_21_1.api.publicObjects.user.PublicUser;

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
