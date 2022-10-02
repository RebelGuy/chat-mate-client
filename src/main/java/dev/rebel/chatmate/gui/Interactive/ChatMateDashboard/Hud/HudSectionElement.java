package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Hud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.HudRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations.BackgroundElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.SeparableHudElement;
import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.config.Config.StatefulEmitter;
import dev.rebel.chatmate.events.models.ConfigEventData;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;
import static dev.rebel.chatmate.util.TextHelpers.toSentenceCase;

public class HudSectionElement extends ContainerElement implements ISectionElement {
  private final static float SCALE = 0.75f;

  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChangeHudEnabled = this::onChangeHudEnabled;
  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChangeDebugModeEnabled = this::onChangeDebugModeEnabled;
  private final InputElement showStatusIndicatorCheckbox;
  private final ExpandableElement statusIndicatorSubElement;
  private final InputElement showViewerCountCheckbox;
  private final ExpandableElement viewerCountSubElement;
  private final InputElement showServerLogsHeartbeatCheckbox;
  private final InputElement showServerLogsTimeSeriesCheckbox;

  private final StatefulEmitter<SeparableHudElement> statusIndicatorEmitter;
  private final StatefulEmitter<SeparableHudElement> viewerCountEmitter;

  public HudSectionElement(InteractiveContext context, IElement parent, @Nullable HudRoute route, Config config) {
    super(context, parent, LayoutMode.BLOCK);

    this.statusIndicatorEmitter = config.getStatusIndicatorEmitter();
    this.viewerCountEmitter = config.getViewerCountEmitter();

    // common properties
    BiFunction<StatefulEmitter<Boolean>, CheckboxInputElement, CheckboxInputElement> setupCheckbox = (state, checkbox) -> checkbox
        .setChecked(state.get())
        .onCheckedChanged(state::set)
        .setScale(SCALE)
        .cast();

    // special: if this is not checked, none of the other checkboxes will be enabled
    super.addElement(setupCheckbox.apply(config.getHudEnabledEmitter(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable ChatMate HUD")
    ));

    this.showStatusIndicatorCheckbox = CHECKBOX_LIGHT.create(context, this)
        .setChecked(this.statusIndicatorEmitter.get().enabled)
        .onCheckedChanged(enabled -> this.statusIndicatorEmitter.set(x -> x.withEnabled(enabled)))
        .setScale(SCALE)
        .setLabel("Show status indicator");
    this.statusIndicatorSubElement = new ExpandableElement(context, this, this.statusIndicatorEmitter, "Show separate YouTube/Twitch indicators")
        .setMargin(new RectExtension(gui(6), ZERO, ZERO, ZERO))
        .cast();

    this.showViewerCountCheckbox = CHECKBOX_LIGHT.create(context, this)
        .setChecked(this.viewerCountEmitter.get().enabled)
        .onCheckedChanged(enabled -> this.viewerCountEmitter.set(x -> x.withEnabled(enabled)))
        .setScale(SCALE)
        .setLabel("Show viewer count");
    this.viewerCountSubElement = new ExpandableElement(context, this, this.viewerCountEmitter, "Show separate YouTube/Twitch viewer counts")
        .setMargin(new RectExtension(gui(6), ZERO, ZERO, ZERO))
        .cast();

    this.showServerLogsHeartbeatCheckbox = setupCheckbox.apply(config.getShowServerLogsHeartbeat(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show Server Logs Heartbeat")
    );
    this.showServerLogsTimeSeriesCheckbox = setupCheckbox.apply(config.getShowServerLogsTimeSeries(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show Server Logs Time Series")
    );

    super.addElement(this.showStatusIndicatorCheckbox);
    super.addElement(this.statusIndicatorSubElement);
    super.addElement(this.showViewerCountCheckbox);
    super.addElement(this.viewerCountSubElement);
    super.addElement(this.showServerLogsHeartbeatCheckbox);
    super.addElement(this.showServerLogsTimeSeriesCheckbox);

    config.getHudEnabledEmitter().onChange(this._onChangeHudEnabled, this, true);
    config.getDebugModeEnabledEmitter().onChange(this._onChangeDebugModeEnabled, this, true);
  }

  public void onShow() {

  }

  public void onHide() {

  }

  private ConfigEventData.Out<Boolean> onChangeHudEnabled(ConfigEventData.In<Boolean> in) {
    boolean enabled = in.data;
    this.showStatusIndicatorCheckbox.setEnabled(this, enabled);
    this.statusIndicatorSubElement.separatePlatformsElement.setEnabled(this, enabled); // nice use of the key functionality of setEnabled!
    this.statusIndicatorSubElement.showPlatformIconElement.setEnabled(this, enabled);
    this.statusIndicatorSubElement.iconLocationDropdown.setEnabled(this, enabled);
    this.showViewerCountCheckbox.setEnabled(this, enabled);
    this.viewerCountSubElement.separatePlatformsElement.setEnabled(this, enabled);
    this.viewerCountSubElement.showPlatformIconElement.setEnabled(this, enabled);
    this.viewerCountSubElement.iconLocationDropdown.setEnabled(this, enabled);
    this.showServerLogsHeartbeatCheckbox.setEnabled(this, enabled);
    this.showServerLogsTimeSeriesCheckbox.setEnabled(this, enabled);
    return new ConfigEventData.Out<>();
  }

  private ConfigEventData.Out<Boolean> onChangeDebugModeEnabled(ConfigEventData.In<Boolean> in) {
    this.showServerLogsHeartbeatCheckbox.setVisible(in.data);
    this.showServerLogsTimeSeriesCheckbox.setVisible(in.data);
    return new ConfigEventData.Out<>();
  }

  private static class ExpandableElement extends WrapperElement {
    private final AnimatedBool expanded;
    private final StatefulEmitter<SeparableHudElement> emitter;
    private final Function<ConfigEventData.In<SeparableHudElement>, ConfigEventData.Out<SeparableHudElement>> _onChangeConfig = this::onChangeConfig;

    public final CheckboxInputElement separatePlatformsElement;
    public final CheckboxInputElement showPlatformIconElement;
    public final DropdownSelectionElement iconLocationDropdown;

    private final List<Tuple2<PlatformIconPosition, LabelElement>> selectionLabels;

    public ExpandableElement(InteractiveContext context, IElement parent, StatefulEmitter<SeparableHudElement> emitter, String separatePlatformsLabel) {
      super(context, parent);

      this.expanded = new AnimatedBool(200L, emitter.get().enabled, this::onAnimationComplete);
      this.emitter = emitter;

      this.separatePlatformsElement = CHECKBOX_LIGHT.create(context, this)
          .onCheckedChanged(checked -> emitter.set(x -> x.withSeparatePlatforms(checked)))
          .setChecked(emitter.get().separatePlatforms)
          .setLabel(separatePlatformsLabel)
          .setScale(SCALE)
          .cast();
      this.showPlatformIconElement = CHECKBOX_LIGHT.create(context, this)
          .onCheckedChanged(checked -> emitter.set(x -> x.withShowPlatformIcon(checked)))
          .setChecked(emitter.get().showPlatformIcon)
          .setLabel("Display platform icons")
          .setScale(SCALE)
          .cast();
      this.iconLocationDropdown = new DropdownSelectionElement(context, parent)
          .setMargin(new RectExtension(gui(6), gui(2)))
          .setMaxWidth(gui(50))
          .cast();
      this.iconLocationDropdown.label
          .setFontScale(SCALE);
      this.iconLocationDropdown.dropdownMenu
          .setBackground(Colour.BLACK.withAlpha(0.8f));

      this.selectionLabels = new ArrayList<>();
      for (PlatformIconPosition position : PlatformIconPosition.values()) {
        LabelElement label = new LabelElement(context, this)
            .setText(toSentenceCase(position.toString()))
            .setFontScale(SCALE)
            .setOnClick(() -> { this.onClickSelection(position); this.iconLocationDropdown.dropdownMenu.setVisible(false); })
            .setPadding(new RectExtension(gui(3), gui(1)))
            .setSizingMode(SizingMode.FILL)
            .cast();
        this.selectionLabels.add(new Tuple2<>(position, label));
        this.iconLocationDropdown.dropdownMenu.addOption(new BackgroundElement(context, this, label)
            .setCornerRadius(gui(2))
            .setHoverColour(Colour.GREY75.withAlpha(0.2f))
            .setMargin(new RectExtension(gui(1), gui(1)))
        );
      }

      super.setContent(new BlockElement(context, this)
          .addElement(this.separatePlatformsElement)
          .addElement(new InlineElement(context, this)
              .addElement(this.showPlatformIconElement)
              .addElement(this.iconLocationDropdown)
          )
      );

      emitter.onChange(this._onChangeConfig, this, true);
    }

    private ConfigEventData.Out<SeparableHudElement> onChangeConfig(ConfigEventData.In<SeparableHudElement> eventIn) {
      this.setExpanded(eventIn.data.enabled);
      this.showPlatformIconElement.setEnabled(this, eventIn.data.separatePlatforms);
      this.iconLocationDropdown.setEnabled(this, eventIn.data.separatePlatforms && eventIn.data.showPlatformIcon);
      this.iconLocationDropdown.label.setText(toSentenceCase(eventIn.data.platformIconPosition.toString()));
      this.selectionLabels.forEach(pair -> pair._2.setColour(pair._1 == eventIn.data.platformIconPosition ? Colour.LIGHT_YELLOW : Colour.WHITE));

      return new ConfigEventData.Out<>();
    }

    private void onClickSelection(PlatformIconPosition position) {
      this.emitter.set(x -> x.withPlatformIconPosition(position));
    }

    public void setExpanded(boolean expanded) {
      this.expanded.set(expanded);
    }

    private void onAnimationComplete(boolean expanded) {
      // do one final recalculation
      super.onInvalidateSize();
    }

    @Override
    protected DimPoint calculateThisSize(Dim maxContentSize) {
      DimPoint size = super.calculateThisSize(maxContentSize);
      return new DimPoint(size.getX(), size.getY().times(this.expanded.getFrac()));
    }

    @Override
    public void setBox(DimRect box) {
      if (this.expanded.getFrac() < 1) {
        super.setVisibleBox(box);
      } else {
        super.setVisibleBox(null);
      }

      super.setBox(box);
    }

    @Override
    protected void renderElement() {
      if (this.expanded.getFrac() != 0 && this.expanded.getFrac() != 1) {
        super.onInvalidateSize();
      }

      super.renderElement();
    }
  }
}
