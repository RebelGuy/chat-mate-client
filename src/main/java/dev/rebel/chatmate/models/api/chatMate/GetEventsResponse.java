package dev.rebel.chatmate.models.api.chatMate;

import dev.rebel.chatmate.models.api.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.models.publicObjects.event.PublicChatMateEvent;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetEventsResponse extends ApiResponseBase<GetEventsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 2;
  }

  public static class GetEventsResponseData {
    public Long reusableTimestamp;
    public PublicChatMateEvent[] events;
  }
}
