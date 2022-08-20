package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.GuiChatMateHud;
import dev.rebel.chatmate.gui.hud.TitleComponent;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.services.util.TaskWrapper;
import jline.internal.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Timer;

public class CountdownHandler {
  private final static String COUNTDOWN_STYLE = new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(true).getFormattingCode();
  private final static String SUB_TITLE_STYLE = new ChatStyle().setColor(EnumChatFormatting.YELLOW).getFormattingCode();

  private final TitleComponent titleComponent;
  private final GuiChatMateHud guiChatMateHud;

  private @Nullable Timer timer = null;
  private @Nullable Integer secondsRemaining = null;
  private @Nullable String title = null;

  public CountdownHandler(DimFactory dimFactory, Minecraft minecraft, FontEngine fontEngine, GuiChatMateHud guiChatMateHud) {
    this.titleComponent = new TitleComponent(dimFactory, minecraft, fontEngine, true, true);
    this.guiChatMateHud = guiChatMateHud;
  }

  public void start(int durationSeconds, String title) {
    if (durationSeconds <= 0) {
      return;
    }

    if (this.timer != null) {
      this.stop();
    }

    this.secondsRemaining = durationSeconds;
    this.title = title;
    this.timer = new Timer();
    this.timer.scheduleAtFixedRate(new TaskWrapper(this::updateCountdown), 0, 1000);
    this.guiChatMateHud.hudComponents.add(this.titleComponent);
    this.updateCountdown();
  }

  public void stop() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }

    this.secondsRemaining = null;
    this.title = null;
    this.titleComponent.clearTimerState();
    this.guiChatMateHud.hudComponents.remove(this.titleComponent);
  }

  public boolean hasExistingCountdown() {
    return this.titleComponent.hasExistingCountdown();
  }

  private void updateCountdown() {
    String mainTitle = COUNTDOWN_STYLE + getCountdownString(this.secondsRemaining);
    String subTitle = this.title == null ? null : SUB_TITLE_STYLE + this.title;

    if (this.secondsRemaining <= 0) {
      this.titleComponent.displayTitle(mainTitle, subTitle, 0, 1000, 500);
      this.stop();
    } else {
      // set the duration a little bit longer to account for lag spikes
      this.titleComponent.displayTitle(mainTitle, subTitle, 0, 2000, 0);
      this.secondsRemaining--;
    }
  }

  private static String getCountdownString(int totalSeconds) {
    int hours = totalSeconds / 3600;
    int minutes = (totalSeconds - hours * 3600) / 60;
    int seconds = totalSeconds - hours * 3600 - minutes * 60;

    ArrayList<String> parts = new ArrayList<>();
    if (hours > 0) {
      // leading
      parts.add(Integer.toString(hours));
    }

    if (parts.size() > 0) {
      // trailing/internal
      parts.add(String.format("%02d", minutes));
    } else if (minutes > 0) {
      // leading
      parts.add(Integer.toString(minutes));
    }

    if (parts.size() > 0) {
      // trailing
      parts.add(String.format("%02d", seconds));
    } else {
      parts.add(Integer.toString(seconds));
    }

    return String.join(":", parts);
  }
}
