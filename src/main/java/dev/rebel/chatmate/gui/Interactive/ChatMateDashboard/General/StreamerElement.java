package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.api.models.streamer.GetPrimaryChannelsResponse.GetPrimaryChannelsResponseData;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;

public class StreamerElement extends BlockElement {
  private final StreamerEndpointProxy streamerEndpointProxy;

  private final IElement youtubeLivestreamElement;
  private final LabelElement errorLabel;
  private final ContainerElement noPrimaryLivestreamsElement;

  public StreamerElement(InteractiveContext context, IElement parent, StreamerEndpointProxy streamerEndpointProxy, Config config) {
    super(context, parent);
    this.streamerEndpointProxy = streamerEndpointProxy;

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

    this.youtubeLivestreamElement = new YoutubeLivestreamElement(context, this, streamerEndpointProxy).setVisible(false);
    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this);

    this.noPrimaryLivestreamsElement = new InlineElement(context, this)
        .setPadding(RectExtension.fromTop(gui(4)))
        .cast();
    SharedElements.INFO_LABEL.create(context, this).setText("You do not have any primary channels set. Please go to the ")
        .splitWords(this.noPrimaryLivestreamsElement)
        .forEach(this.noPrimaryLivestreamsElement::addElement);
    this.noPrimaryLivestreamsElement.addElement(new UrlElement(context, this, "ChatMate website", context.environment.getStudioStreamerManagerUrl()));
    SharedElements.INFO_LABEL.create(context, this).setText(" to indicate which Youtube and/or Twitch channel you will be streaming on.")
        .splitWords(this.noPrimaryLivestreamsElement)
        .forEach(this.noPrimaryLivestreamsElement::addElement);
    this.noPrimaryLivestreamsElement
        .setVisible(false);

    super.addElement(enableChatMateCheckbox);
    super.addElement(enableSoundCheckbox);
    super.addElement(showInOptionsCheckbox);
    super.addElement(this.youtubeLivestreamElement);
    super.addElement(this.errorLabel);
    super.addElement(this.noPrimaryLivestreamsElement);
  }

  @Override
  public ContainerElement setVisible(boolean visible) {
    super.setVisible(visible);

    if (visible) {
      this.noPrimaryLivestreamsElement.setVisible(false);
      this.youtubeLivestreamElement.setVisible(false);
      this.errorLabel.setVisible(false);
      this.streamerEndpointProxy.getPrimaryChannelsAsync(this::onGetPrimaryChannelsResponse, this::onGetPrimaryChannelsError);
    }

    return this;
  }

  private void onGetPrimaryChannelsError(Throwable error) {
    this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error)).setVisible(true);
  }

  private void onGetPrimaryChannelsResponse(GetPrimaryChannelsResponseData response) {
    if (!response.hasPrimaryYoutubeChannel() && !response.hasPrimaryTwitchChannel()) {
      this.noPrimaryLivestreamsElement.setVisible(true);
    } else if (response.hasPrimaryYoutubeChannel()) {
      this.youtubeLivestreamElement.setVisible(true);
    }
  }
}
