package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Debug;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.LogLevel;
import dev.rebel.chatmate.config.Config.StatefulEmitter;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.DebugRoute;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.*;

public class DebugSectionElement extends ContainerElement implements ISectionElement {
  private final Config config;
  private final LabelElement didClearStoresLabel;

  public DebugSectionElement(InteractiveContext context, IElement parent, @Nullable DebugRoute route, Config config) {
    super(context, parent, LayoutMode.BLOCK);
    this.config = config;

    for (LogLevel logLevel : LogLevel.values()) {
      super.addElement(CHECKBOX_LIGHT.create(context, this)
          .setLabel(String.format("Log %s messages", logLevel.toString()))
          .setChecked(this.hasLogLevel(logLevel))
          .onCheckedChanged(checked -> this.setLogLevel(logLevel, checked))
          .setScale(0.75f)
          .setMargin(new RectExtension(ZERO, gui(2)))
      );
    }

    super.addElement(TEXT_BUTTON_LIGHT.create(context, this)
        .setText("Clear stores")
        .setTextScale(0.75f)
        .setOnClick(this::onClearStores)
        .setMargin(RectExtension.fromTop(gui(8)))
    );

    this.didClearStoresLabel = new LabelElement(context, this)
        .setText("Cleared stores!")
        .setFontScale(0.5f)
        .setColour(Colour.DARK_GREEN)
        .setMargin(new RectExtension(gui(3), gui(2)))
        .setVisible(false)
        .cast();
    super.addElement(this.didClearStoresLabel);

    super.addElement(TEXT_BUTTON_LIGHT.create(context, this)
        .setText("Reset cursor")
        .setTextScale(0.75f)
        .setOnClick(this::onResetCursor)
    );
  }

  private boolean hasLogLevel(LogLevel logLevel) {
    return Arrays.stream(this.config.getLogLevelsEmitter().get()).anyMatch(level -> level == logLevel);
  }

  private void setLogLevel(LogLevel logLevel, boolean enabled) {
    Set<LogLevel> levels = new HashSet<>(Collections.list(this.config.getLogLevelsEmitter().get()));
    if (enabled) {
      levels.add(logLevel);
    } else {
      levels.remove(logLevel);
    }
    this.config.getLogLevelsEmitter().set(levels.toArray(new LogLevel[0]));
  }

  private void onClearStores() {
    super.context.rankApiStore.clear();
    super.context.donationApiStore.clear();
    super.context.livestreamApiStore.clear();
    super.context.donationHudStore.clear();
    super.context.commandApiStore.clear();
    this.didClearStoresLabel.setVisible(true);
  }

  private void onResetCursor() {
    super.context.cursorService.reset();
  }

  @Override
  public void onShow() {

  }

  @Override
  public void onHide() {
    this.didClearStoresLabel.setVisible(false);
  }
}
