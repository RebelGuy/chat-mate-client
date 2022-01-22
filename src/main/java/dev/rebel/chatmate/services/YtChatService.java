package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.GetChatResponse;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.services.util.TaskWrapper;
import jline.internal.Nullable;

import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;

public class YtChatService extends EventEmitterService<ChatItem[]> {
  private final ChatEndpointProxy chatEndpointProxy;

  private final long TIMEOUT_WAIT = 20 * 1000;
  private @Nullable Timer timer = null;
  private @Nullable Long lastTimestamp = null;
  private @Nullable Long pauseUntil = null;
  private Boolean requestInProgress = false;

  public YtChatService(ChatEndpointProxy chatEndpointProxy) {
    super();

    this.chatEndpointProxy = chatEndpointProxy;
  }

  public void start() {
    if (this.timer != null) {
      return;
    }

    this.timer = new Timer();
    this.timer.scheduleAtFixedRate(new TaskWrapper(this::makeRequest), 0, 500);
  }

  public void stop() {
    super.clear();

    if (this.timer == null) {
      return;
    }

    this.timer.cancel();
    this.timer = null;
  }

  private void makeRequest() {
    if (!this.canMakeRequest()) {
      return;
    }
    this.requestInProgress = true;

    GetChatResponse response = null;
    try {
      response = this.chatEndpointProxy.GetChat(this.lastTimestamp, null);
    } catch (ConnectException e) {
      this.pauseUntil = new Date().getTime() + this.TIMEOUT_WAIT;
    } catch (Exception ignored) { }

    if (response != null) {
      this.lastTimestamp = response.lastTimestamp;
      super.dispatch(response.chat);
    }

    this.requestInProgress = false;
  }

  private boolean canMakeRequest() {
    boolean skipRequest = this.requestInProgress || this.pauseUntil != null && this.pauseUntil > new Date().getTime();
    return !skipRequest;
  }
}
