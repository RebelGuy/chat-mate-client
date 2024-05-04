package dev.rebel.chatmate.api.models.websocket.client;

import com.google.gson.annotations.SerializedName;

public class ClientMessage {
  public final ClientMessageType type;
  public final Object data;

  private ClientMessage(ClientMessageType type, Object data) {
    this.type = type;
    this.data = data;
  }

  public static ClientMessage createSubscribeMessage(SubscribeMessageData data) {
    return new ClientMessage(ClientMessageType.SUBSCRIBE, data);
  }

  public static ClientMessage createUnsubscribeMessage(UnsubscribeMessageData data) {
    return new ClientMessage(ClientMessageType.UNSUBSCRIBE, data);
  }

  public enum ClientMessageType {
    @SerializedName("subscribe") SUBSCRIBE,
    @SerializedName("unsubscribe") UNSUBSCRIBE
  }
}
