package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Chat;

import dev.rebel.chatmate.config.Config.CommandMessageChatVisibility;
import dev.rebel.chatmate.events.models.ConfigEventData;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.ChatRoute;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.gui.style.Colour;

import javax.annotation.Nullable;

import java.util.function.Function;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;
import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.SCALE;
import static dev.rebel.chatmate.util.TextHelpers.toSentenceCase;

public class ChatSectionElement extends ContainerElement implements ISectionElement {
  private final DropdownSelectionElement<CommandMessageChatVisibility> commandVisibilityDropdown;

  private final Function<ConfigEventData.In<CommandMessageChatVisibility>, ConfigEventData.Out<CommandMessageChatVisibility>> _onChangeConfig = this::onChangeConfig;

  public ChatSectionElement(InteractiveContext context, IElement parent, @Nullable ChatRoute route, Config config) {
    super(context, parent, LayoutMode.BLOCK);

    LabelElement chatDisplacementLabel = new LabelElement(context, this)
        .setFontScale(SCALE)
        .setText("Chat Height Offset")
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setMargin(new RectExtension(ZERO, gui(3), ZERO, ZERO))
        .cast();
    ValueSliderElement chatDisplacementSlider = new ValueSliderElement(context, this)
        .setDecimals(0)
        .setMinValue(0)
        .setMaxValue(100)
        .setSuffix("px")
        .setValue(config.getChatVerticalDisplacementEmitter().get())
        .onChange(x -> config.getChatVerticalDisplacementEmitter().set((int)x.floatValue())) // lol
        .setSizingMode(SizingMode.FILL)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast()
        .cast();
    super.addElement(new InlineElement(context, this)
        .addElement(chatDisplacementLabel)
        .addElement(chatDisplacementSlider)
        .setMargin(new RectExtension(ZERO, gui(2)))
    );

    super.addElement(CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show platform icon")
        .setChecked(config.getShowChatPlatformIconEmitter().get())
        .onCheckedChanged(config.getShowChatPlatformIconEmitter()::set)
        .setScale(0.75f)
        .setMargin(new RectExtension(ZERO, gui(2)))
    );

    LabelElement commandVisibilityLabel = new LabelElement(context, this)
        .setFontScale(SCALE)
        .setText("Command Message Visibility")
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setMargin(new RectExtension(ZERO, gui(3), ZERO, ZERO))
        .cast();
    this.commandVisibilityDropdown = new DropdownSelectionElement<>(context, this)
        .setMargin(new RectExtension(gui(6), gui(2)))
        .setMaxWidth(gui(70))
        .cast();
    this.commandVisibilityDropdown.label
        .setFontScale(SCALE);
    this.commandVisibilityDropdown.dropdownMenu
        .setBackground(Colour.BLACK.withAlpha(0.8f));

    Function<CommandMessageChatVisibility, String> stringifyVisibility = value -> value == CommandMessageChatVisibility.SHOWN ? "Shown" : value == CommandMessageChatVisibility.HIDDEN ? "Hidden" : value == CommandMessageChatVisibility.GREYED_OUT ? "Greyed out" : "Unknown";
    for (CommandMessageChatVisibility value : CommandMessageChatVisibility.values()) {
      LabelElement label = new LabelElement(context, this)
          .setText(stringifyVisibility.apply(value))
          .setFontScale(SCALE)
          .setPadding(new RectExtension(gui(3), gui(1)))
          .setSizingMode(SizingMode.FILL)
          .cast();
      this.commandVisibilityDropdown.addOption(
          label,
          value,
          this::onClickSelection,
          (el, selected) -> el.setColour(selected ? Colour.LIGHT_YELLOW : Colour.WHITE),
          stringifyVisibility);
    }
    super.addElement(new InlineElement(context, this)
        .addElement(commandVisibilityLabel)
        .addElement(this.commandVisibilityDropdown)
    );

    context.config.getCommandMessageChatVisibilityEmitter().onChange(this._onChangeConfig, this, true);
  }

  @Override
  public void onShow() {

  }

  @Override
  public void onHide() {

  }

  private void onClickSelection(CommandMessageChatVisibility value) {
    super.context.config.getCommandMessageChatVisibilityEmitter().set(value);
  }

  private ConfigEventData.Out<CommandMessageChatVisibility> onChangeConfig(ConfigEventData.In<CommandMessageChatVisibility> value) {
    this.commandVisibilityDropdown.setSelection(value.data);
    return new ConfigEventData.Out<>();
  }
}
