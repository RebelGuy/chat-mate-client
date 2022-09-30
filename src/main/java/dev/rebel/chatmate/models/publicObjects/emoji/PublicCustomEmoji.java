package dev.rebel.chatmate.models.publicObjects.emoji;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicCustomEmoji extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public String name;
  public String symbol;
  public String imageData;
  public Integer levelRequirement;
  public Integer[] whitelistedRanks;
}
