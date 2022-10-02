package dev.rebel.chatmate.api.publicObjects.donation;

import dev.rebel.chatmate.api.publicObjects.PublicObject;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

import javax.annotation.Nullable;

public class PublicDonation extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public Long time;
  public Float amount;
  public String formattedAmount;
  public String currency;
  public String name;
  public @Nullable String message;
  public @Nullable PublicUser linkedUser;
  public @Nullable Long linkedAt;
}
