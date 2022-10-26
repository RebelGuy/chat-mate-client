package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Debug;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.StatefulEmitter;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.DebugRoute;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.style.Colour;

import javax.annotation.Nullable;

import java.util.function.BiFunction;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.*;

public class DebugSectionElement extends ContainerElement implements ISectionElement {
  private final LabelElement didClearStoresLabel;

  public DebugSectionElement(InteractiveContext context, IElement parent, @Nullable DebugRoute route) {
    super(context, parent, LayoutMode.BLOCK);

    super.addElement(TEXT_BUTTON_LIGHT.create(context, this)
        .setText("Clear stores")
        .setTextScale(0.75f)
        .setOnClick(this::onClearStores)
    );

    this.didClearStoresLabel = new LabelElement(context, this)
        .setText("Cleared stores!")
        .setFontScale(0.5f)
        .setColour(Colour.DARK_GREEN)
        .setMargin(new RectExtension(gui(3), gui(2)))
        .setVisible(false)
        .cast();
    super.addElement(this.didClearStoresLabel);

    super.addElement(CHECKBOX_WITH_CONFIG.apply(context.config.getShowServerLogsHeartbeat(), CHECKBOX_LIGHT.create(context, this).setLabel("Show Server Logs Heartbeat")));
    super.addElement(CHECKBOX_WITH_CONFIG.apply(context.config.getShowServerLogsTimeSeries(), CHECKBOX_LIGHT.create(context, this).setLabel("Show Server Logs Time Series")));
  }

  private void onClearStores() {
    super.context.rankApiStore.clear();
    super.context.donationApiStore.clear();
    super.context.livestreamApiStore.clear();
    super.context.donationHudStore.clear();
    this.didClearStoresLabel.setVisible(true);
  }

  @Override
  public void onShow() {

  }

  @Override
  public void onHide() {
    this.didClearStoresLabel.setVisible(false);
  }
}
