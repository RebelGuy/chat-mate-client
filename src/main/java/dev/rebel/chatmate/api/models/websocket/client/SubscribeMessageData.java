package dev.rebel.chatmate.api.models.websocket.client;

import dev.rebel.chatmate.api.models.websocket.Topic;

public class SubscribeMessageData {
  public final Topic topic;
  public final String streamer;

  public SubscribeMessageData(Topic topic, String streamer) {
    this.topic = topic;
    this.streamer = streamer;
  }
}
