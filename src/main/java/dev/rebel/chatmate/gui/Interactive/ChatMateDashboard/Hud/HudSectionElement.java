package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Hud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.HudRoute;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.StatefulEmitter;
import dev.rebel.chatmate.services.events.models.ConfigEventData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;

public class HudSectionElement extends ContainerElement implements ISectionElement {
  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChangeHudEnabled = this::onChangeHudEnabled;
  private final List<InputElement> hudDependentElements = new ArrayList<>();

  public HudSectionElement(InteractiveContext context, IElement parent, @Nullable HudRoute route, Config config) {
    super(context, parent, LayoutMode.BLOCK);

    float scale = 0.75f;

    // common properties
    BiFunction<StatefulEmitter<Boolean>, CheckboxInputElement, CheckboxInputElement> setupCheckbox = (state, checkbox) -> checkbox
        .setChecked(state.get())
        .onCheckedChanged(state::set)
        .setScale(scale)
        .cast();

    // special: if this is not checked, none of the other checkboxes will be enabled
    super.addElement(setupCheckbox.apply(config.getHudEnabledEmitter(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable ChatMate HUD")
    ));

    this.hudDependentElements.add(setupCheckbox.apply(config.getShowStatusIndicatorEmitter(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show Status Indicator")
    ));
    this.hudDependentElements.add(setupCheckbox.apply(config.getShowLiveViewersEmitter(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show Viewer Count")
    ));
    this.hudDependentElements.add(setupCheckbox.apply(config.getShowServerLogsHeartbeat(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show Server Logs Heartbeat")
    ));
    this.hudDependentElements.add(setupCheckbox.apply(config.getShowServerLogsTimeSeries(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show Server Logs Time Series")
    ));
    this.hudDependentElements.add(setupCheckbox.apply(config.getSeparatePlatforms(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Separate YouTube and Twitch")
    ));

    this.hudDependentElements.forEach(super::addElement);

    config.getHudEnabledEmitter().onChange(this::onChangeHudEnabled, this, true);
  }

  public void onShow() {

  }

  public void onHide() {

  }

  private ConfigEventData.Out<Boolean> onChangeHudEnabled(ConfigEventData.In<Boolean> in) {
    boolean enabled = in.data;
    this.hudDependentElements.forEach(el -> el.setEnabled(this, enabled));
    return new ConfigEventData.Out<>();
  }
}
