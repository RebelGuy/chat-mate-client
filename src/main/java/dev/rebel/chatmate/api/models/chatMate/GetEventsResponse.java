package dev.rebel.chatmate.api.models.chatMate;

import dev.rebel.chatmate.api.models.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.api.publicObjects.event.PublicChatMateEvent;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetEventsResponse extends ApiResponseBase<GetEventsResponseData> {
  public static class GetEventsResponseData {
    public Long reusableTimestamp;
    public PublicChatMateEvent[] events;
  }
}
