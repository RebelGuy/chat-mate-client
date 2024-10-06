package dev.rebel.chatmate.api.models.websocket.server;

import dev.rebel.chatmate.api.models.websocket.Topic;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.api.publicObjects.event.PublicChatMateEvent;

import static dev.rebel.chatmate.util.JsonHelpers.parseSerialisedObject;

public class EventMessageData {
  public Topic topic;
  public String streamer;
  public Object data;

  public PublicChatItem getChatData() {
    if (this.topic != Topic.STREAMER_CHAT) {
      throw new RuntimeException("Inconsistent data type");
    }

    return parseSerialisedObject(this.data, PublicChatItem.class);
  }

  public PublicChatMateEvent getEventData() {
    if (this.topic != Topic.STREAMER_EVENTS) {
      throw new RuntimeException("Inconsistent data type");
    }

    return parseSerialisedObject(this.data, PublicChatMateEvent.class);
  }
}
