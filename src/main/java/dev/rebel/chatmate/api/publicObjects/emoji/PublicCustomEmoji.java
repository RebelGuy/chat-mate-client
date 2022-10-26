package dev.rebel.chatmate.api.publicObjects.emoji;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicCustomEmoji extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public String name;
  public String symbol;
  public String imageData;
  public Integer levelRequirement;
  public Boolean canUseInDonationMessage;
  public Integer version;
  public Boolean isActive;
  public Integer[] whitelistedRanks;
}
