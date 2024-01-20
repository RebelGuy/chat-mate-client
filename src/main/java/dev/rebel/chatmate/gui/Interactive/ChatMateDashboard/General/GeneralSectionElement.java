package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.api.proxy.AccountEndpointProxy;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;
import dev.rebel.chatmate.config.Config.LoginInfo;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.GeneralRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;

public class GeneralSectionElement extends ContainerElement implements ISectionElement {
  private final LoadingSpinnerElement loadingSpinner;
  private final LabelElement notStreamerLabel;
  private final LabelElement errorLabel;
  private final StreamerElement streamerElement;
  private final EventCallback<LoginInfo> _onLoginInfoChanged = this::onLoginInfoChanged;

  public GeneralSectionElement(InteractiveContext context, IElement parent, @Nullable GeneralRoute route, StreamerEndpointProxy streamerEndpointProxy, Config config, AccountEndpointProxy accountEndpointProxy) {
    super(context, parent, LayoutMode.BLOCK);

    super.addElement(new LoginElement(context, this, accountEndpointProxy)
        .setMargin(RectExtension.fromBottom(gui(4)))
    );

    this.loadingSpinner = new LoadingSpinnerElement(this.context, this)
        .setHorizontalAlignment(Layout.HorizontalAlignment.CENTRE)
        .setMaxContentWidth(gui(16))
        .setMargin(new RectExtension(gui(8)))
        .setVisible(false)
        .setSizingMode(SizingMode.FILL)
        .cast();
    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this);
    this.notStreamerLabel = SharedElements.INFO_LABEL.create(context, this)
        .setText("You are not a streamer. Please head over to the ChatMate website to become a streamer.")
        .setVisible(false)
        .cast();
    this.streamerElement = new StreamerElement(context, this, streamerEndpointProxy, config)
        .setVisible(false)
        .cast();
    IElement enableDebugModeCheckbox = CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable Debug Mode")
        .setChecked(config.getDebugModeEnabledEmitter().get())
        .onCheckedChanged(config.getDebugModeEnabledEmitter()::set)
        .setScale(0.75f)
        .setMargin(RectExtension.fromTop(gui(12)));

    super.addElement(this.loadingSpinner);
    super.addElement(this.errorLabel);
    super.addElement(this.notStreamerLabel);
    super.addElement(this.streamerElement);
    super.addElement(enableDebugModeCheckbox);

    config.getLoginInfoEmitter().onChange(this._onLoginInfoChanged, this, true);
    super.addDisposer(() -> config.getLoginInfoEmitter().off(this));
  }

  private void onLoginInfoChanged(Event<LoginInfo> loginInfoEvent) {
    this.tryShowStreamerElements();
  }

  private void tryShowStreamerElements() {
    String username = super.context.config.getLoginInfoEmitter().get().username;

    // don't do anything if the user is not logged in
    if (username == null) {
      this.loadingSpinner.setVisible(false);
      this.errorLabel.setVisible(false);
      this.notStreamerLabel.setVisible(false);
      this.streamerElement.setVisible(false);
      return;
    }

    this.loadingSpinner.setVisible(true);
    super.context.streamerApiStore.loadStreamers(
        streamers -> super.context.renderer.runSideEffect(() -> this.onGetStreamers(streamers)),
        err -> super.context.renderer.runSideEffect(() -> this.onError(err)),
        false
    );
  }

  private void onGetStreamers(List<PublicStreamerSummary> streamers) {
    this.loadingSpinner.setVisible(false);

    String username = super.context.config.getLoginInfoEmitter().get().username;
    boolean isStreamer = Collections.any(streamers, streamer -> Objects.equals(streamer.username, username));
    if (isStreamer) {
      this.streamerElement.setVisible(true);
    } else {
      this.notStreamerLabel.setVisible(true);
    }
  }

  private void onError(Throwable error) {
      this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error));
      this.errorLabel.setVisible(true);
      this.loadingSpinner.setVisible(false);
  }

  @Override
  public void onShow() {
    this.tryShowStreamerElements();
  }

  @Override
  public void onHide() {
    this.errorLabel.setVisible(false);
    this.notStreamerLabel.setVisible(false);
    this.loadingSpinner.setVisible(false);
    this.streamerElement.setVisible(false);
  }
}
