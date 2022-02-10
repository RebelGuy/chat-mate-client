package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.services.events.models.NewChatEventData.In;
import dev.rebel.chatmate.services.events.models.NewChatEventData.Out;
import dev.rebel.chatmate.services.events.models.NewChatEventData.Options;

public class NewChatEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final ChatItem[] chatItems;

    public In(ChatItem[] chatItems) {
      this.chatItems = chatItems;
    }
  }

  public static class Out extends EventOut {

  }

  public static class Options extends EventOptions {

  }
}
