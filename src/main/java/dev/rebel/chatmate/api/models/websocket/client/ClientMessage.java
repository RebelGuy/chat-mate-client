package dev.rebel.chatmate.api.models.websocket.client;

import com.google.gson.annotations.SerializedName;

public class ClientMessage {
  public final ClientMessageType type;
  public final Integer id;
  public final Object data;

  private ClientMessage(ClientMessageType type, int id, Object data) {
    this.type = type;
    this.id = id;
    this.data = data;
  }

  public static ClientMessage createSubscribeMessage(int id, SubscribeMessageData data) {
    return new ClientMessage(ClientMessageType.SUBSCRIBE, id, data);
  }

  public static ClientMessage createUnsubscribeMessage(int id, UnsubscribeMessageData data) {
    return new ClientMessage(ClientMessageType.UNSUBSCRIBE, id, data);
  }

  public enum ClientMessageType {
    @SerializedName("subscribe") SUBSCRIBE,
    @SerializedName("unsubscribe") UNSUBSCRIBE
  }
}
