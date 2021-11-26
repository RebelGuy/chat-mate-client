package dev.rebel.chatoverlay.services;

import dev.rebel.chatoverlay.models.chat.ChatItem;

import java.util.ArrayList;
import java.util.function.Consumer;

public class YtChatListenerService {
  private final ArrayList<Consumer<ChatItem[]>> _listeners;

  public YtChatListenerService() {
    this._listeners = new ArrayList<>();
  }

  public void listen(Consumer<ChatItem[]> newChatCallback) {
    this._listeners.add(newChatCallback);
  }

  public void clear() {
    this._listeners.clear();
  }

  public void onNewChat(ChatItem[] newChat) {
    this._listeners.forEach(l -> l.accept(newChat));
  }
}
