package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.gui.Interactive.BlockElement;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;

public class StreamerElement extends BlockElement {
  private final LivestreamElement livestreamElement;

  public StreamerElement(InteractiveContext context, IElement parent, StreamerEndpointProxy streamerEndpointProxy, Config config) {
    super(context, parent);

    IElement enableChatMateCheckbox = CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable ChatMate")
        .setChecked(config.getChatMateEnabledEmitter().get())
        .onCheckedChanged(config.getChatMateEnabledEmitter()::set)
        .setScale(0.75f)
        .setSizingMode(Layout.SizingMode.FILL);
    IElement enableSoundCheckbox = CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable sound")
        .setChecked(config.getSoundEnabledEmitter().get())
        .onCheckedChanged(config.getSoundEnabledEmitter()::set)
        .setScale(0.75f);
    IElement showInOptionsCheckbox = CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show ChatMate options in the pause menu")
        .setChecked(config.getShowChatMateOptionsInPauseMenuEmitter().get())
        .onCheckedChanged(config.getShowChatMateOptionsInPauseMenuEmitter()::set)
        .setScale(0.75f);
    this.livestreamElement = new LivestreamElement(context, this, streamerEndpointProxy);

    super.addElement(enableChatMateCheckbox);
    super.addElement(enableSoundCheckbox);
    super.addElement(showInOptionsCheckbox);
    super.addElement(this.livestreamElement);
  }

  @Override
  public ContainerElement setVisible(boolean visible) {
    return super.setVisible(visible);
  }
}
