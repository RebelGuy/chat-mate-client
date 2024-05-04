package dev.rebel.chatmate.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.rebel.chatmate.Environment;
import dev.rebel.chatmate.api.models.websocket.Topic;
import dev.rebel.chatmate.api.models.websocket.client.ClientMessage;
import dev.rebel.chatmate.api.models.websocket.client.SubscribeMessageData;
import dev.rebel.chatmate.api.models.websocket.server.AcknowledgeMessageData;
import dev.rebel.chatmate.api.models.websocket.server.EventMessageData;
import dev.rebel.chatmate.api.models.websocket.server.ServerMessage;
import dev.rebel.chatmate.api.models.websocket.server.ServerMessage.ServerMessageType;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.TaskWrapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

public class ChatMateWebsocketClient extends WebSocketClient {
  private final LogService logService;
  private final Gson gson;
  private final List<Consumer<EventMessageData>> callbacks;
  private double lastRetryBackoff;
  private @Nullable Timer retryTimer;
  private boolean hasAttemptedInitialConnection;

  public ChatMateWebsocketClient(LogService logService, Environment environment) {
    super(createUri(environment)); // the fact that we have to wrap this logic into a static function is laughable
    this.logService = logService;
    this.gson = new GsonBuilder()
        .serializeNulls()
        .create();
    this.callbacks = new ArrayList<>();

    this.hasAttemptedInitialConnection = false;
    this.lastRetryBackoff = 500;
    this.tryConnectAfterDelay(100);
  }

  public void addListener(Consumer<EventMessageData> callback) {
    this.callbacks.add(callback);
  }

  public void removeListener(Consumer<EventMessageData> callback) {
    this.callbacks.remove(callback);
  }

  @Override
  public void onMessage(String message) {
    ServerMessage parsed;
    try {
      parsed = this.gson.fromJson(message, ServerMessage.class);

      if (parsed == null) {
        this.logService.logError(this, "Websocket message cannot be parsed. Message:", message);
        return;
      }
    } catch (Exception e) {
      this.logService.logError(this, "Error while attempting to parse message. Message:", message, e);
      return;
    }

    if (parsed.type == ServerMessageType.ACKNOWLEDGE) {
      this.logService.logInfo(this, "Received acknowledgment from server.", parsed);
      AcknowledgeMessageData acknowledgementData = parsed.getAcknowledgeData();
      // don't handle for now
    } else if (parsed.type == ServerMessageType.EVENT) {
      this.logService.logInfo(this, "Received event from server.", parsed);
      EventMessageData eventData = parsed.getEventData();
      if (eventData.topic == Topic.STREAMER_CHAT || eventData.topic == Topic.STREAMER_EVENTS) {
        for (Consumer<EventMessageData> callback : this.callbacks) {
          try {
            callback.accept(eventData);
          } catch (Exception e) {
            this.logService.logError(this, "Encountered exception while executing message callback for message", parsed, e);
          }
        }
      } else {
        this.logService.logError(this, "Invalid event data topic", parsed);
      }
    } else {
      this.logService.logError(this, "Received invalid server message type", parsed);
    }
  }

  @Override
  public void onOpen(ServerHandshake handshake) {
    this.logService.logInfo(this, "Websocket connection established");

    this.cancelRetryTimer();
    this.lastRetryBackoff = 500;
    this.subscribeToStreamerTopic(Topic.STREAMER_CHAT, "rebel_guy");
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    this.logService.logInfo(this, "Websocket closed with code", code, "and reason", reason);

    this.cancelRetryTimer();
    this.lastRetryBackoff *= 2;
    this.tryConnectAfterDelay(this.lastRetryBackoff);
    // todo: automatically reconnect
    // todo: upon reconnection (or while disconnected), we might want to use an REST API request to fetch the data we might have missed
  }

  @Override
  public void onError(Exception e) {
    this.logService.logError(this, "Websocket encountered an error:", e);
  }

  private void onTryReconnect() {
    this.logService.logInfo(this, "Attempting to connect...");

    try {
      if (super.getReadyState() == ReadyState.OPEN) {
        this.logService.logError(this, "Attempting to connect but connection is already open. Attempting to close and re-open the connection.");
        super.closeBlocking();
      }

      if (!this.hasAttemptedInitialConnection) {
        // we can only do the initial connection once - any further attempts to call connect() will throw.
        // it doesn't seem like we can check the state on the Websocket object, which is just fantastic!
        this.hasAttemptedInitialConnection = true;
        super.connectBlocking();
      } else {
        super.reconnectBlocking();
      }

      if (super.getReadyState() == ReadyState.OPEN || super.getReadyState() == ReadyState.CLOSED) {
        // if it's closed, the onClose handler will be called
        return;
      } else {
        this.logService.logError(this, "Apparently connected successfully but ready state is", super.getReadyState(), "- will retry the connection");
      }
    } catch (Exception e) {
      this.logService.logError(this, "Unable to connect:", e);
    }

    this.cancelRetryTimer();
    this.lastRetryBackoff *= 2;
    this.tryConnectAfterDelay(this.lastRetryBackoff);
  }

  private void tryConnectAfterDelay(double delay) {
    this.retryTimer = new Timer();
    this.retryTimer.schedule(new TaskWrapper(this::onTryReconnect), (long)delay);
  }

  private void subscribeToStreamerTopic(Topic topic, String streamer) {
    // todo: subscribe when logging in
    this.send(ClientMessage.createSubscribeMessage(new SubscribeMessageData(topic, streamer)));

    // todo: listen to the next few seconds' messages here and verify that the server acknowledged the request.
    // will need to make server changes so that we know which client message the acknowledgement is for.
  }

  private void unsubscribeFromStreamerTopic(Topic topic, String streamer) {
    // todo: unsubscribe when the mod unloads and when logging out
  }

  private void send(Object data) {
    this.send(this.gson.toJson(data));
  }

  private void cancelRetryTimer() {
    if (this.retryTimer != null) {
      this.retryTimer.cancel();
      this.retryTimer = null;
    }
  }

  @Override
  public void send(String message) {
    this.logService.logDebug(this, "Sending message", message);
    super.send(message);
  }

  private static URI createUri(Environment environment) {
    try {
      return new URI(environment.serverUrl.replace("http", "ws") + "/ws");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
