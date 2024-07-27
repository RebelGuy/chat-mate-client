package dev.rebel.chatmate.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.rebel.chatmate.Environment;
import dev.rebel.chatmate.api.models.websocket.Topic;
import dev.rebel.chatmate.api.models.websocket.client.ClientMessage;
import dev.rebel.chatmate.api.models.websocket.client.SubscribeMessageData;
import dev.rebel.chatmate.api.models.websocket.client.UnsubscribeMessageData;
import dev.rebel.chatmate.api.models.websocket.server.AcknowledgeMessageData;
import dev.rebel.chatmate.api.models.websocket.server.EventMessageData;
import dev.rebel.chatmate.api.models.websocket.server.ServerMessage;
import dev.rebel.chatmate.api.models.websocket.server.ServerMessage.ServerMessageType;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.TaskWrapper;

import javax.websocket.*;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.function.Consumer;

@ClientEndpoint
public class ChatMateWebsocketClient {
  private final LogService logService;
  private final Config config;
  private final Gson gson;
  private final URI uri;
  private final List<Runnable> connectCallbacks;
  private final List<Runnable> disconnectCallbacks;
  private final List<Consumer<EventMessageData>> messageCallbacks;
  private final List<AcknowledgementListener> acknowledgementListeners;
  private final Timer pingTimer;
  private double lastRetryBackoff;
  private @Nullable Timer retryTimer;
  private boolean enabled;
  private int id;

  private Session userSession = null;

  public ChatMateWebsocketClient(LogService logService, Environment environment, Config config) {
    this.logService = logService;
    this.config = config;
    this.gson = new GsonBuilder()
        .serializeNulls()
        .create();
    this.uri = createUri(environment);
    this.connectCallbacks = new ArrayList<>();
    this.disconnectCallbacks = new ArrayList<>();
    this.messageCallbacks = new ArrayList<>();
    this.acknowledgementListeners = new CopyOnWriteArrayList<>(); // concurrent version of list so we can allow listeners removing themselves as part of their own callback
    this.pingTimer = new Timer();
    this.pingTimer.scheduleAtFixedRate(new TaskWrapper(this::sendPing), 60_000, 60_000);

    this.lastRetryBackoff = 500;
    this.enabled = true;
    this.id = 0;
    this.tryConnectAfterDelay(100);

    this.config.getChatMateEnabledEmitter().onChange(this::onChatMateEnabledChanged);
    this.config.getLoginInfoEmitter().onChange(this::onLoginInfoChanged);
  }

  // the point of the connect/disconnect listeners is to toggle the ApiPollers - when the websocket is not connected,
  // we instead get the equivalent data via API calls as a fallback.

  public void addConnectListener(Runnable callback) {
    this.connectCallbacks.add(callback);
  }

  public void removeConnectListener(Runnable callback) {
    this.connectCallbacks.remove(callback);
  }

  public void addDisconnectListener(Runnable callback) {
    this.disconnectCallbacks.add(callback);
  }

  public void removeDisconnectListener(Runnable callback) {
    this.disconnectCallbacks.remove(callback);
  }

  /** The event data can be null, e.g. for ping messages. */
  public void addMessageListener(Consumer<EventMessageData> callback) {
    this.messageCallbacks.add(callback);
  }

  public void removeMessageListener(Consumer<EventMessageData> callback) {
    this.messageCallbacks.remove(callback);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    if (!this.enabled) {
      this.close();
    } else {
      this.onTryReconnect();
    }
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  @OnMessage
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

      if (parsed.id != null) {
        for (AcknowledgementListener callback : this.acknowledgementListeners) {
          try {
            if (callback.onAcknowledgementReceived(parsed.id, acknowledgementData.success)) {
              this.acknowledgementListeners.remove(callback);
            }
          } catch (Exception e) {
            this.logService.logError(this, "Encountered exception while executing acknowledgement callback for message", parsed, e);
          }
        }
      }

    } else if (parsed.type == ServerMessageType.EVENT) {
      this.logService.logInfo(this, "Received event from server.", parsed);
      EventMessageData eventData = parsed.getEventData();
      if (eventData.topic == Topic.STREAMER_CHAT || eventData.topic == Topic.STREAMER_EVENTS) {
        for (Consumer<EventMessageData> callback : this.messageCallbacks) {
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

  @OnMessage
  public void onWebsocketPong(PongMessage message) {
    for (Consumer<EventMessageData> callback : this.messageCallbacks) {
      try {
        callback.accept(null);
      } catch (Exception e) {
        this.logService.logError(this, "Encountered exception while executing message callback for the pong message", e);
      }
    }
  }

  @OnOpen
  public void onOpen(Session userSession) {
    this.logService.logInfo(this, "Websocket connection established");

    this.userSession = userSession;
    this.cancelRetryTimer();
    this.lastRetryBackoff = 500;

    @Nullable String selectedStreamer = this.config.getLoginInfoEmitter().get().username;
    if (selectedStreamer == null) {
      this.logService.logError(this, "Websocket opened but no streamer is selected - closing");
      this.close();
      return;
    }

    CompletableFuture.allOf(
        this.subscribeToStreamerTopic(Topic.STREAMER_CHAT, selectedStreamer),
        this.subscribeToStreamerTopic(Topic.STREAMER_EVENTS, selectedStreamer)
    ).thenRun(() -> {
      this.logService.logInfo(this, "Subscribed to all events");

      for (Runnable callback : this.connectCallbacks) {
        try {
          callback.run();
        } catch (Exception e) {
          this.logService.logError(this, "Encountered exception while notifying listeners of the onOpen event", e);
        }
      }
    }).exceptionally(e -> {
      this.logService.logError(this, "Failed to subscribe to all events - closing Websocket for another attempt.", e);
      this.close();
      return null;
    });
  }

  @OnClose
  public void onClose(CloseReason closeReason) {
    this.logService.logInfo(this, "Websocket closed with code", closeReason.getCloseCode().getCode(), "and reason", closeReason.getReasonPhrase());

    if (this.shouldConnect()) {
      this.cancelRetryTimer();
      this.lastRetryBackoff *= 2;
      this.tryConnectAfterDelay(this.lastRetryBackoff);
    } else {
      this.logService.logDebug(this, "Won't attempt to reconnect because the conditions to connect are not currently met");
    }

    for (Runnable callback : this.disconnectCallbacks) {
      try {
        callback.run();
      } catch (Exception e) {
        this.logService.logError(this, "Encountered exception while notifying listeners of the onClose event", e);
      }
    }
  }

  @OnError
  public void onError(Throwable e) {
    this.logService.logError(this, "Websocket encountered an error:", e);
  }

  private void onChatMateEnabledChanged(Event<Boolean> event) {
    if (event.getData()) {
      this.tryConnectAfterDelay(1);
    } else {
      this.close();
    }
  }

  private void onLoginInfoChanged(Event<Config.LoginInfo> in) {
    // technically we shouldn't open/close the connection based on the streamer's username - instead we should just cycle our streamer topic subscriptions
    // but i'm too lazy
    if (in.getData().username != null) {
      this.tryConnectAfterDelay(1);
    } else {
      this.close();
    }
  }

  private boolean shouldConnect() {
    boolean chatMateEnabled = this.config.getChatMateEnabledEmitter().get();
    boolean streamerSelected = this.config.getLoginInfoEmitter().get().username != null;

    return chatMateEnabled && streamerSelected && this.enabled;
  }

  private void onTryReconnect() {
    if (!this.shouldConnect()) {
      return;
    }

    this.logService.logInfo(this, "Attempting to connect...");

    try {
      this.cancelRetryTimer();
      if (this.isOpen()) {
        this.logService.logError(this, "Attempting to connect but connection is already open. Attempting to close and re-open the connection.");
        this.close();
      }

      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.connectToServer(this, this.uri);

      if (this.isOpen()) {
        return;
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

  private void close() {
    if (this.userSession == null) {
      this.logService.logDebug(this, "Closing websocket connection, but no user session exists anyway");
      return;
    }

    try {
      this.userSession.close();
      this.userSession = null;
    } catch (Exception e) {
      this.logService.logError(this, "Unable to close the connection:", e);
      throw new RuntimeException(e);
    }
  }

  private boolean isOpen() {
    return this.userSession != null && this.userSession.isOpen();
  }

  private CompletableFuture<Void> subscribeToStreamerTopic(Topic topic, String streamer) {
    /*
     * Note to self: in the future if you have to deal with async stuff, the CompletableFuture stuff isn't that bad.
     * - You can explicitly complete it with a result or exception. If already completed and attepting to do so again is a no-op
     * - You can chain together CompletableFutures and pass around results, similar to Javascript's `then`
     * - `thenRun`/`thenApply` functions are called if the future has resolved
     * - `exceptionally` functions are called if the future has thrown
     * - You probably don't want to use `get()` because it's a blocking operation
      */

    int id = this.id++;

    CompletableFuture<Void> future = new CompletableFuture<>();
    Timer timer = new Timer();

    AcknowledgementListener callback = (messageId, success) -> {
      if (messageId != id) {
        return false;
      }

      timer.cancel();
      if (success) {
        future.complete(null);
      } else {
        future.completeExceptionally(new RuntimeException(String.format("Subscription for topic %s for streamer %s was not successful", topic, streamer)));
      }

      // note: this callback is a one-time use and will be removed from the list automatically
      return true;
    };

    timer.schedule(new TaskWrapper(() -> {
      future.completeExceptionally(new RuntimeException(String.format("Subscription for topic %s for streamer %s timed out", topic, streamer)));
      this.acknowledgementListeners.remove(callback);
    }), 5000);

    this.acknowledgementListeners.add(callback);
    this.send(ClientMessage.createSubscribeMessage(id, new SubscribeMessageData(topic, streamer)));

    return future;
  }

  private void unsubscribeFromStreamerTopic(Topic topic, String streamer) {
    this.send(ClientMessage.createUnsubscribeMessage(this.id++, new UnsubscribeMessageData(topic, streamer)));
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

  private void send(String message) {
    this.logService.logDebug(this, "Sending message", message);
    this.userSession.getAsyncRemote().sendText(message);
  }

  private void sendPing() {
    if (this.userSession != null) {
      try {
        this.userSession.getAsyncRemote().sendPing(null);
      } catch (Exception e) {
        this.logService.logError(this, "Unable to send ping:", e);
      }
    }
  }

  private static URI createUri(Environment environment) {
    try {
      return new URI(environment.serverUrl.replace("http", "ws") + "/ws");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @FunctionalInterface
  private interface AcknowledgementListener {
    boolean onAcknowledgementReceived(Integer messageId, boolean successfullyHandledByServer);
  }
}
