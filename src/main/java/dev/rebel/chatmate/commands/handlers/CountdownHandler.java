package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.TitleHudElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.TitleHudElement.TitleOptions;
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

  private @Nullable TitleHudElement titleHudElement;
  private final ChatMateHudStore chatMateHudStore;

  private @Nullable Timer timer = null;
  private @Nullable Integer secondsRemaining = null;
  private @Nullable String title = null;

  public CountdownHandler(DimFactory dimFactory, Minecraft minecraft, FontEngine fontEngine, ChatMateHudStore chatMateHudStore) {
    this.chatMateHudStore = chatMateHudStore;
  }

  public void start(int durationSeconds, String title) {
    if (this.hasExistingCountdown()) {
      throw new RuntimeException("A countdown is already running.");
    }

    if (durationSeconds <= 0) {
      throw new RuntimeException("The duration must be positive.");
    }

    if (this.timer != null) {
      this.stop();
    }

    this.secondsRemaining = durationSeconds;
    this.title = title;
    this.timer = new Timer();
    this.titleHudElement = this.chatMateHudStore.addElement(TitleHudElement::new);
    this.timer.scheduleAtFixedRate(new TaskWrapper(this::updateCountdown), 0, 1000);
  }

  public void stop() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }

    this.secondsRemaining = null;
    this.title = null;
    this.chatMateHudStore.removeElement(this.titleHudElement);
    this.titleHudElement = null;
  }

  public boolean hasExistingCountdown() {
    return this.titleHudElement != null;
  }

  private void updateCountdown() {
    String mainTitle = COUNTDOWN_STYLE + getCountdownString(this.secondsRemaining);
    String subTitle = this.title == null ? null : SUB_TITLE_STYLE + this.title;

    if (this.secondsRemaining <= 0) {
      this.titleHudElement.setTitle(new TitleOptions(mainTitle, subTitle, 0, 1000, 500));
      this.timer.cancel();
      this.timer = new Timer();
      this.timer.schedule(new TaskWrapper(this::stop), 2000);
    } else {
      // set the duration a little bit longer to account for lag spikes
      this.titleHudElement.setTitle(new TitleOptions(mainTitle, subTitle, 0, 2000, 0));
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
