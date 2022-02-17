package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.services.events.models.NewChatEventData.In;
import dev.rebel.chatmate.services.events.models.NewChatEventData.Out;
import dev.rebel.chatmate.services.events.models.NewChatEventData.Options;

public class NewChatEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final PublicChatItem[] chatItems;

    public In(PublicChatItem[] chatItems) {
      this.chatItems = chatItems;
    }
  }

  public static class Out extends EventOut {

  }

  public static class Options extends EventOptions {

  }
}
