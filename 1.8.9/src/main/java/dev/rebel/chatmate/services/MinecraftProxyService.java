package dev.rebel.chatmate.services;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.gui.CustomGuiNewChat;
import dev.rebel.chatmate.events.ForgeEventService;
import dev.rebel.chatmate.events.models.RenderChatGameOverlayEventData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Tuple;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

// if we try to access/modify the Minecraft world on a separate thread, we may get a concurrency-related crash.
// the solution is to schedule work on the minecraft thread, so it can be executed when safe.
// there is no harm in scheduling things while already on the minecraft thread, so for conciseness don't do any checking.
/** Use this proxy for thread-unsafe Minecraft operations. */
public class MinecraftProxyService {
  private final Minecraft minecraft;
  private final LogService logService;
  private final ForgeEventService forgeEventService;
  private final CustomGuiNewChat customGuiNewChat;

  private final List<Tuple<String, IChatComponent>> pendingChatAdditions;
  private final List<IChatComponent> pendingChatDeletions;
  private final List<Tuple2<Predicate<IChatComponent>, UnaryOperator<IChatComponent>>> pendingChatReplacements;
  private boolean refreshChat;

  public MinecraftProxyService(Minecraft minecraft, LogService logService, ForgeEventService forgeEventService, CustomGuiNewChat customGuiNewChat) {
    this.minecraft = minecraft;
    this.logService = logService;
    this.forgeEventService = forgeEventService;
    this.customGuiNewChat = customGuiNewChat;

    this.pendingChatAdditions = Collections.synchronizedList(new ArrayList<>());
    this.pendingChatDeletions = Collections.synchronizedList(new ArrayList<>());
    this.pendingChatReplacements = Collections.synchronizedList(new ArrayList<>());
    this.refreshChat = false;

    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
  }

  public void playSound(ISound sound) {
    this.schedule(mc -> mc.getSoundHandler().playSound(sound));
  }

  /** Prints the chat message immediately, or holds on to the message until the chat GUI is visible again. */
  public void printChatMessage(String type, IChatComponent component) {
    synchronized (this.pendingChatAdditions) {
      this.pendingChatAdditions.add(new Tuple<>(type, component));
    }
  }

  /** Will regenerate the chat lines as soon as possible. */
  public void refreshChat() {
    this.refreshChat = true;
  }

  public void deleteComponentFromChat(IChatComponent component) {
    synchronized (this.pendingChatDeletions) {
      this.pendingChatDeletions.add(component);
    }
  }

  public void replaceComponentInChat(Predicate<IChatComponent> predicate, UnaryOperator<IChatComponent> componentGenerator) {
    synchronized (this.pendingChatReplacements) {
      this.pendingChatReplacements.add(new Tuple2<>(predicate, componentGenerator));
    }
  }

  public boolean checkCurrentScreen(@Nullable GuiScreen screen) {
    return this.minecraft.currentScreen == screen;
  }

  private void schedule(Consumer<Minecraft> work) {
    this.minecraft.addScheduledTask(() -> work.accept(this.minecraft));
  }

  private void onRenderChatGameOverlay(Event<RenderChatGameOverlayEventData> event) {
    this.flushPendingChatChanges();
  }

  private void flushPendingChatChanges() {
    // save up messages until it becomes available
    if (this.minecraft.ingameGUI == null) {
      return;
    }

    synchronized (this.pendingChatAdditions) {
      for (Tuple<String, IChatComponent> chatItem : this.pendingChatAdditions) {
        String type = chatItem.getFirst();
        IChatComponent component = chatItem.getSecond();
        String error = null;
        try {
          this.customGuiNewChat.printChatMessage(component);
        } catch (Exception e) {
          error = e.getMessage();
        }

        if (error == null) {
          this.logService.logInfo(this, String.format("[Chat %s] %s", type, component.getUnformattedText()));
        } else {
          this.logService.logError(this, String.format("Could not print chat %s message '%s'. Error: %s", type, component.getUnformattedText(), error));
        }
      }

      this.pendingChatAdditions.clear();
    }

    synchronized (this.pendingChatDeletions) {
      for (IChatComponent chatItem : this.pendingChatDeletions) {
        this.customGuiNewChat.deleteComponent(chatItem);
        this.refreshChat = true;
      }

      this.pendingChatDeletions.clear();
    }

    synchronized (this.pendingChatReplacements) {
      for (Tuple2<Predicate<IChatComponent>, UnaryOperator<IChatComponent>> args : this.pendingChatReplacements) {
        Predicate<IChatComponent> predicate = args._1;
        UnaryOperator<IChatComponent> componentGenerator = args._2;
        this.customGuiNewChat.replaceLine(abstractChatLine -> predicate.test(abstractChatLine.getChatComponent()), componentGenerator);
        this.refreshChat = true;
      }

      this.pendingChatReplacements.clear();
    }

    if (this.refreshChat) {
      this.refreshChat = false;
      this.customGuiNewChat.refreshChat(true);
    }
  }
}
