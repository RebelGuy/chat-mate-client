package dev.rebel.chatmate.models.publicObjects.event;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicDonationData extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public Long time;
  public Float amount;
  public String formattedAmount;
  public String currency;
  public String name;
  public @Nullable String message;
}
