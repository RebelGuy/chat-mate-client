package dev.rebel.chatmate.commands.handlers;

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

  private final Minecraft minecraft;

  private @Nullable Timer timer = null;
  private @Nullable Integer secondsRemaining = null;
  private @Nullable String title = null;

  public CountdownHandler(Minecraft minecraft) {
    this.minecraft = minecraft;
  }

  public void start(int duration, String title) {
    if (duration <= 0) {
      return;
    }

    if (this.timer != null) {
      this.stop();
    }

    this.secondsRemaining = duration;
    this.title = title;
    this.timer = new Timer();
    this.timer.scheduleAtFixedRate(new TaskWrapper(this::updateCountdown), 0, 1000);
  }

  public void stop() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }

    this.secondsRemaining = null;
    this.title = null;
  }

  private void updateCountdown() {
    String countdown = getCountdownString(this.secondsRemaining);
    // there seems to be a really amusing bug (?) in displayTitle where you can't set all properties in one call...
    // note the total delay of 1 here - this is important, otherwise we can't modify the times later on!
    this.minecraft.ingameGUI.displayTitle(COUNTDOWN_STYLE + countdown, null, 0, 1, 0);
    if (this.title != null) {
      this.minecraft.ingameGUI.displayTitle(null, SUB_TITLE_STYLE + this.title, 0, 0, 0);
    }

    if (this.secondsRemaining <= 0) {
      this.minecraft.ingameGUI.displayTitle(null, null, 0, 20, 10);
      this.stop();
    } else {
      // set the duration a little bit longer to account for lag spikes
      this.minecraft.ingameGUI.displayTitle(null, null, 0, 25, 0);
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
