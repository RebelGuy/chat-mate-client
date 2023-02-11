package dev.rebel.chatmate.api.models.chat;

import dev.rebel.chatmate.api.models.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetChatResponse extends ApiResponseBase<GetChatResponseData> {
  public static class GetChatResponseData {
    public Long reusableTimestamp;
    public PublicChatItem[] chat;
  }
}
