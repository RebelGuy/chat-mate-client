package dev.rebel.chatmate.models.api.chat;

import dev.rebel.chatmate.models.api.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetChatResponse extends ApiResponseBase<GetChatResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 5;
  }

  public static class GetChatResponseData {
    public Long reusableTimestamp;
    public PublicChatItem[] chat;
  }
}
