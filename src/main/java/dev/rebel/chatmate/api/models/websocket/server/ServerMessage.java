package dev.rebel.chatmate.api.models.websocket.server;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

import static dev.rebel.chatmate.util.JsonHelpers.parseSerialisedObject;

public class ServerMessage {
  public ServerMessageType type;
  public @Nullable Integer id;
  public Object data;

  public AcknowledgeMessageData getAcknowledgeData() {
    if (this.type != ServerMessageType.ACKNOWLEDGE) {
      throw new RuntimeException("Inconsistent data type");
    }

    return parseSerialisedObject(this.data, AcknowledgeMessageData.class);
  }

  public EventMessageData getEventData() {
    if (this.type != ServerMessageType.EVENT) {
      throw new RuntimeException("Inconsistent data type");
    }

    return parseSerialisedObject(this.data, EventMessageData.class);
  }

  public enum ServerMessageType {
    @SerializedName("acknowledge") ACKNOWLEDGE,
    @SerializedName("event") EVENT
  }
}
