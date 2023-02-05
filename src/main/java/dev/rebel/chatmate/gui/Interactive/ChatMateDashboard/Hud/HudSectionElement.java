package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Hud;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.HudRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudService;
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
import dev.rebel.chatmate.events.models.ConfigEventOptions;

import javax.annotation.Nullable;
import java.util.function.Function;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.*;
import static dev.rebel.chatmate.util.TextHelpers.toSentenceCase;

public class HudSectionElement extends ContainerElement implements ISectionElement {
  private final static float SCALE = 0.75f;

  private final EventCallback<Boolean> _onChangeHudEnabled = this::onChangeHudEnabled;
  private final EventCallback<SeparableHudElement> _onChangeStatusIndicator = this::onChangeStatusIndicator;
  private final EventCallback<SeparableHudElement> _onChangeViewerCount = this::onChangeViewerCount;

  private final CheckboxInputElement enableHudCheckbox;
  private final CheckboxInputElement showStatusIndicatorCheckbox;
  private final ExpandableElement statusIndicatorSubElement;
  private final CheckboxInputElement showViewerCountCheckbox;
  private final ExpandableElement viewerCountSubElement;
  private final IElement didResetHudLabel;

  private final StatefulEmitter<SeparableHudElement> statusIndicatorEmitter;
  private final StatefulEmitter<SeparableHudElement> viewerCountEmitter;
  private final ChatMateHudService chatMateHudService;

  public HudSectionElement(InteractiveContext context, IElement parent, @Nullable HudRoute route, Config config, ChatMateHudService chatMateHudService) {
    super(context, parent, LayoutMode.BLOCK);

    this.statusIndicatorEmitter = config.getStatusIndicatorEmitter();
    this.viewerCountEmitter = config.getViewerCountEmitter();
    this.chatMateHudService = chatMateHudService;

    // special: if this is not checked, none of the other checkboxes will be enabled
    this.enableHudCheckbox = CHECKBOX_WITH_CONFIG.apply(config.getHudEnabledEmitter(), CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable ChatMate HUD")
    );
    super.addElement(this.enableHudCheckbox);

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

    super.addElement(this.showStatusIndicatorCheckbox);
    super.addElement(this.statusIndicatorSubElement);
    super.addElement(this.showViewerCountCheckbox);
    super.addElement(this.viewerCountSubElement);

    super.addElement(TEXT_BUTTON_LIGHT.create(context, this)
        .setText("Reset HUD")
        .setTextScale(0.75f)
        .setOnClick(this::onResetHud)
    );

    this.didResetHudLabel = new LabelElement(context, this)
        .setText("HUD has been reset!")
        .setFontScale(0.5f)
        .setColour(Colour.DARK_GREEN)
        .setMargin(new RectExtension(gui(3), gui(2)))
        .setVisible(false)
        .cast();
    super.addElement(this.didResetHudLabel);

    config.getHudEnabledEmitter().onChange(this._onChangeHudEnabled, this, true);
    config.getStatusIndicatorEmitter().onChange(this._onChangeStatusIndicator, this, false);
    config.getViewerCountEmitter().onChange(this._onChangeViewerCount, this, false);
  }

  @Override
  public void onShow() {

  }

  @Override
  public void onHide() {
    this.didResetHudLabel.setVisible(false);
  }

  private void onResetHud() {
    this.chatMateHudService.resetHud();
    this.didResetHudLabel.setVisible(true);
  }

  private void onChangeHudEnabled(Event<Boolean> event) {
    boolean enabled = event.getData();
    this.enableHudCheckbox.setChecked(enabled, true);
    this.showStatusIndicatorCheckbox.setEnabled(this, enabled);
    this.statusIndicatorSubElement.separatePlatformsElement.setEnabled(this, enabled); // nice use of the key functionality of setEnabled!
    this.statusIndicatorSubElement.showPlatformIconElement.setEnabled(this, enabled);
    this.statusIndicatorSubElement.iconLocationDropdown.setEnabled(this, enabled);
    this.showViewerCountCheckbox.setEnabled(this, enabled);
    this.viewerCountSubElement.separatePlatformsElement.setEnabled(this, enabled);
    this.viewerCountSubElement.showPlatformIconElement.setEnabled(this, enabled);
    this.viewerCountSubElement.iconLocationDropdown.setEnabled(this, enabled);
  }

  private void onChangeStatusIndicator(Event<SeparableHudElement> event) {
    this.showStatusIndicatorCheckbox.setChecked(event.getData().enabled, true);
  }

  private void onChangeViewerCount(Event<SeparableHudElement> event) {
    this.showViewerCountCheckbox.setChecked(event.getData().enabled, true);
  }

  private static class ExpandableElement extends WrapperElement {
    private final AnimatedBool expanded;
    private final StatefulEmitter<SeparableHudElement> emitter;
    private final EventCallback<SeparableHudElement> _onChangeConfig = this::onChangeConfig;

    public final CheckboxInputElement separatePlatformsElement;
    public final CheckboxInputElement showPlatformIconElement;
    public final DropdownSelectionElement<PlatformIconPosition> iconLocationDropdown;

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
      this.iconLocationDropdown = new DropdownSelectionElement<PlatformIconPosition>(context, parent)
          .setMargin(new RectExtension(gui(6), gui(2)))
          .setMaxWidth(gui(50))
          .cast();
      this.iconLocationDropdown.label
          .setFontScale(SCALE);
      this.iconLocationDropdown.dropdownMenu
          .setBackground(Colour.BLACK.withAlpha(0.8f));

      for (PlatformIconPosition position : PlatformIconPosition.values()) {
        LabelElement label = new LabelElement(context, this)
            .setText(toSentenceCase(position.toString()))
            .setFontScale(SCALE)
            .setPadding(new RectExtension(gui(3), gui(1)))
            .setSizingMode(SizingMode.FILL)
            .cast();
        this.iconLocationDropdown.addOption(
            label,
            position,
            this::onClickSelection,
            (el, selected) -> el.setColour(selected ? Colour.LIGHT_YELLOW : Colour.WHITE),
            pos -> toSentenceCase(pos.toString()));
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

    private void onChangeConfig(Event<SeparableHudElement> event) {
      SeparableHudElement data = event.getData();
      this.setExpanded(data.enabled);
      this.separatePlatformsElement.setChecked(data.separatePlatforms, true);
      this.showPlatformIconElement.setChecked(data.showPlatformIcon, true);
      this.showPlatformIconElement.setEnabled(this, data.separatePlatforms);
      this.iconLocationDropdown.setEnabled(this, data.separatePlatforms && data.showPlatformIcon);
      this.iconLocationDropdown.setSelection(data.platformIconPosition);
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
