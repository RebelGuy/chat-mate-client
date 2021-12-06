package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.ChatItem;

import java.util.ArrayList;
import java.util.function.Consumer;

public class YtChatEventService {
  private final ArrayList<Consumer<ChatItem[]>> _chatListeners;

  public YtChatEventService() {
    this._chatListeners = new ArrayList<>();
  }

  public void onChat(Consumer<ChatItem[]> newChatCallback) {
    this._chatListeners.add(newChatCallback);
  }

  public void clear() {
    this._chatListeners.clear();
  }

  public void dispatchChat(ChatItem[] newChat) {
    this._chatListeners.forEach(l -> l.accept(newChat));
  }
}
