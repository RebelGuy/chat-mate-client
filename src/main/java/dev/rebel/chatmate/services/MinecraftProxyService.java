package dev.rebel.chatmate.services;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

// if we try to access/modify the Minecraft world on a separate thread, we may get a concurrency-related crash.
// the solution is to schedule work on the minecraft thread, so it can be executed when safe.
// there is no harm in scheduling things while already on the minecraft thread, so for conciseness don't do any checking.
/** Use this proxy for thread-unsafe Minecraft operations. */
public class MinecraftProxyService {
  private final Minecraft minecraft;
  private final LogService logService;

  public MinecraftProxyService(Minecraft minecraft, LogService logService) {
    this.minecraft = minecraft;
    this.logService = logService;
  }

  public void playSound(ISound sound) {
    this.schedule(mc -> mc.getSoundHandler().playSound(sound));
  }

  /** Returns true if the chat message was printed successfully. */
  public boolean tryPrintChatMessage(String type, IChatComponent component) {
    String error = null;
    if (!canPrintChatMessage()) {
      error = "minecraft.ingameGUI is null";
    } else {
      try {
        this.minecraft.ingameGUI.getChatGUI().printChatMessage(component);
      } catch (Exception e) {
        error = e.getMessage();
      }
    }

    if (error == null) {
      this.logService.logInfo(this, String.format("[Chat %s] %s", type, component.getUnformattedText()));
      return false;
    } else {
      this.logService.logError(this, String.format("Could not print chat %s message '%s'. Error: %s", type, component.getUnformattedText(), error));
      return true;
    }
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

  private void schedule(Consumer<Minecraft> work) {
    this.minecraft.addScheduledTask(() -> work.accept(this.minecraft));
  }
}
