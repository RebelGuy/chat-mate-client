package dev.rebel.chatmate.api.models.websocket.client;

import dev.rebel.chatmate.api.models.websocket.Topic;

public class UnsubscribeMessageData {
  public final Topic topic;
  public final String streamer;

  public UnsubscribeMessageData(Topic topic, String streamer) {
    this.topic = topic;
    this.streamer = streamer;
  }
}
