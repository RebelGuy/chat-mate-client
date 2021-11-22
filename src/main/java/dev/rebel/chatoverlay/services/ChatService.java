package dev.rebel.chatoverlay.services;

import com.google.gson.JsonSyntaxException;
import dev.rebel.chatoverlay.models.chat.GetChatResponse;
import dev.rebel.chatoverlay.proxy.ChatProxy;
import jline.internal.Nullable;

import java.net.ConnectException;
import java.util.Timer;
import java.util.TimerTask;

public class ChatService {
  private final ChatProxy chatProxy;
  private final ChatListenerService chatListenerService;
  private final Timer timer;

  private @Nullable
  Long lastTimestamp = null;
  Boolean requestInProgress = false;

  public ChatService(ChatProxy chatProxy, ChatListenerService chatListenerService) {
    this.chatProxy = chatProxy;
    this.chatListenerService = chatListenerService;
    this.timer = new Timer();
  }

  public void start() {
    this.timer.scheduleAtFixedRate(new ChatServiceWorker(this::makeRequest), 0, 500);
  }

  private void makeRequest() {
    if (this.requestInProgress) {
      return;
    }
    this.requestInProgress = true;

    GetChatResponse response = null;
    try {
      response = this.chatProxy.GetChat(this.lastTimestamp, null);
    } catch (ConnectException e) {
      System.out.println("[ChatService] Failed to connect to server - is it running?");
    } catch (JsonSyntaxException e) {
      System.out.println("[ChatService] Failed to process JSON response - has the schema changed? " + e.getMessage());
    } catch (Exception e) {
      System.out.println("[ChatService] Failed to get chat: " + e.getMessage());
    }

    if (response != null) {
      this.lastTimestamp = response.lastTimestamp;

      try {
        this.chatListenerService.onNewChat(response.chat);
      } catch (Exception e) {
        System.out.println("[ChatService] Failed to notify listeners of new chat items: " + e.getMessage());
      }
    }

    this.requestInProgress = false;
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
