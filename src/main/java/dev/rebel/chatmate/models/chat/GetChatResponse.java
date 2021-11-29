package dev.rebel.chatmate.models.chat;

import dev.rebel.chatmate.interfaces.IApiResponse;

public class GetChatResponse implements IApiResponse {
  public Number schema;
  public String liveId;
  public Long lastTimestamp;
  public Boolean isPartial;
  public ChatItem[] chat;

  @Override
  public Number GetExpectedSchema() {
    return 1;
  }
}
