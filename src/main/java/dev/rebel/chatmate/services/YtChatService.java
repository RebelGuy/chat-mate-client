package dev.rebel.chatmate.services;

import com.google.gson.JsonSyntaxException;
import dev.rebel.chatmate.models.chat.GetChatResponse;
import dev.rebel.chatmate.proxy.YtChatProxy;
import jline.internal.Nullable;

import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class YtChatService {
  private final YtChatProxy ytChatProxy;
  private final YtChatEventService ytChatEventService;

  private final long TIMEOUT_WAIT = 20 * 1000;
  private @Nullable Timer timer = null;
  private @Nullable Long lastTimestamp = null;
  private @Nullable Long pauseUntil = null;
  private Boolean requestInProgress = false;

  public YtChatService(YtChatProxy ytChatProxy, YtChatEventService ytChatEventService) {
    this.ytChatProxy = ytChatProxy;
    this.ytChatEventService = ytChatEventService;
  }

  public void start() {
    if (this.timer != null) {
      return;
    }

    this.timer = new Timer();
    this.timer.scheduleAtFixedRate(new ChatServiceWorker(this::makeRequest), 0, 500);
  }

  public void stop() {
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
      response = this.ytChatProxy.GetChat(this.lastTimestamp, null);
    } catch (ConnectException e) {
      System.out.println("[ChatService] Failed to connect to server - is it running?");
      this.pauseUntil = new Date().getTime() + this.TIMEOUT_WAIT;
    } catch (JsonSyntaxException e) {
      System.out.println("[ChatService] Failed to process JSON response - has the schema changed? " + e.getMessage());
    } catch (Exception e) {
      System.out.println("[ChatService] Failed to get chat: " + e.getMessage());
    }

    if (response != null) {
      this.lastTimestamp = response.lastTimestamp;

      try {
        this.ytChatEventService.dispatchChat(response.chat);
      } catch (Exception e) {
        System.out.println("[ChatService] Failed to notify listeners of new chat items: " + e.getMessage());
      }
    }

    this.requestInProgress = false;
  }

  private boolean canMakeRequest() {
    boolean skipRequest = this.requestInProgress || this.pauseUntil != null && this.pauseUntil > new Date().getTime();
    return !skipRequest;
  }

  class ChatServiceWorker extends TimerTask {
    private final Runnable runnable;

    ChatServiceWorker(Runnable runnable) {
      this.runnable = runnable;
    }

    public void run() {
      this.runnable.run();
    }
  }
}
