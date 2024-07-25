package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatImage;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.models.LiveReactionsEventData;
import dev.rebel.chatmate.services.ImageService;
import dev.rebel.chatmate.util.ResolvableTexture;
import dev.rebel.chatmate.util.TaskWrapper;
import net.minecraft.client.Minecraft;

import java.util.Timer;

public class LiveReactionService {
  private final ChatMateHudStore chatMateHudStore;
  private final Minecraft minecraft;
  private final ImageService imageService;

  public LiveReactionService(ChatMateEventService chatMateEventService, ChatMateHudStore chatMateHudStore, Minecraft minecraft, ImageService imageService) {
    this.chatMateHudStore = chatMateHudStore;
    this.minecraft = minecraft;
    this.imageService = imageService;

    chatMateEventService.onLiveReactions(this::onLiveReactions);
  }

  private void onLiveReactions(Event<LiveReactionsEventData> event) {
    int reactionCount = event.getData().reactionCount;
    PublicChatImage image = event.getData().emojiImage;
    String cacheKey = event.getData().getCacheKey();

    int duration = (int)(Math.random() * 10000);
    for (int i = 0; i < reactionCount; i++) {
      int delay = duration * i / reactionCount;
      new Timer().schedule(
          new TaskWrapper(() -> this.minecraft.addScheduledTask(
              () -> this.spawnReactionElement(image, cacheKey))
          ),
          delay
      );
    }
  }

  private void spawnReactionElement(PublicChatImage image, String cacheKey) {
    ResolvableTexture texture = this.imageService.createCacheableTextureFromUrl(image.width, image.height, image.url, cacheKey);
    this.chatMateHudStore.addElement(
        (context, parent) -> new LiveReactionElement(context, parent, texture, this.chatMateHudStore::removeElement)
    );
  }
}
