package dev.rebel.chatmate.services;

import dev.rebel.chatmate.gui.CustomGuiNewChat;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

// if we try to access/modify the Minecraft world on a separate thread, we may get a concurrency-related crash.
// the solution is to schedule work on the minecraft thread, so it can be executed when safe.
// there is no harm in scheduling things while already on the minecraft thread, so for conciseness don't do any checking.
/** Use this proxy for thread-unsafe Minecraft operations. */
public class MinecraftProxyService {
  private final Minecraft minecraft;
  private final LogService logService;
  private final ForgeEventService forgeEventService;

  private final List<Tuple<String, IChatComponent>> pendingChat;
  private final List<IChatComponent> pendingDeletionChat;
  private boolean refreshChat;

  public MinecraftProxyService(Minecraft minecraft, LogService logService, ForgeEventService forgeEventService) {
    this.minecraft = minecraft;
    this.logService = logService;
    this.forgeEventService = forgeEventService;

    this.pendingChat = Collections.synchronizedList(new ArrayList<>());
    this.pendingDeletionChat = Collections.synchronizedList(new ArrayList<>());
    this.refreshChat = false;

    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
  }

  public void playSound(ISound sound) {
    this.schedule(mc -> mc.getSoundHandler().playSound(sound));
  }

  /** Prints the chat message immediately, or holds on to the message until the chat GUI is visible again. */
  public void printChatMessage(String type, IChatComponent component) {
    if (!canPrintChatMessage()) {
      this.logService.logError(this, String.format("Could not print chat %s message '%s'. Error: %s", type, component.getUnformattedText(), "minecraft.ingameGUI is null"));
    } else {
      synchronized (this.pendingChat) {
        this.pendingChat.add(new Tuple<>(type, component));
      }
    }
  }

  /** Will regenerate the chat lines as soon as possible. */
  public void refreshChat() {
    this.refreshChat = true;
  }

  public boolean canPrintChatMessage() {
    return this.minecraft.gameSettings.chatVisibility != EnumChatVisibility.HIDDEN && this.minecraft.ingameGUI != null;
  }

  /** Never null if `canPrintChatMessage()` returns true. */
  public @Nullable FontRenderer getChatFontRenderer() {
    if (canPrintChatMessage()) {
      return this.minecraft.ingameGUI.getFontRenderer();
    } else {
      return null;
    }
  }

  public @Nullable Integer getChatWidth() {
    if (this.minecraft.ingameGUI == null) {
      return null;
    }

    return this.getChatGUI().getChatWidth();
  }

  /** Returns the effective chat width that takes into account scaling. If the font renderer measures text to be at most this width, it will fit onto the chat GUI. */
  public @Nullable Integer getChatWidthForText() {
    if (this.minecraft.ingameGUI == null) {
      return null;
    }

    return this.getChatGUI().getChatWidthForText();
  }

  public void deleteComponentFromChat(IChatComponent component) {
    synchronized (this.pendingDeletionChat) {
      this.pendingDeletionChat.add(component);
    }
  }

  private void schedule(Consumer<Minecraft> work) {
    this.minecraft.addScheduledTask(() -> work.accept(this.minecraft));
  }

  private RenderChatGameOverlay.Out onRenderChatGameOverlay(RenderChatGameOverlay.In eventIn) {
    this.flushPendingChatChanges();
    return new RenderChatGameOverlay.Out();
  }

  private void flushPendingChatChanges() {
    synchronized (this.pendingChat) {
      for (Tuple<String, IChatComponent> chatItem : this.pendingChat) {
        String type = chatItem.getFirst();
        IChatComponent component = chatItem.getSecond();
        String error = null;
        try {
          this.getChatGUI().printChatMessage(component);
        } catch (Exception e) {
          error = e.getMessage();
        }

        if (error == null) {
          this.logService.logInfo(this, String.format("[Chat %s] %s", type, component.getUnformattedText()));
        } else {
          this.logService.logError(this, String.format("Could not print chat %s message '%s'. Error: %s", type, component.getUnformattedText(), error));
        }
      }

      this.pendingChat.clear();
    }

    synchronized (this.pendingDeletionChat) {
      CustomGuiNewChat gui = this.getChatGUI();
      for (IChatComponent chatItem : this.pendingDeletionChat) {
        gui.deleteComponent(chatItem);
        this.refreshChat = true;
      }

      this.pendingDeletionChat.clear();
    }

    if (this.refreshChat) {
      this.refreshChat = false;
      this.getChatGUI().refreshChat(true);
    }
  }

  public CustomGuiNewChat getChatGUI() {
    CustomGuiNewChat gui = (CustomGuiNewChat)this.minecraft.ingameGUI.getChatGUI();
    assert gui != null;
    return gui;
  }
}
