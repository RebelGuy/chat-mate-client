package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.config.Config.LoginInfo;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.stores.StreamerApiStore.StreamerState;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.Objects;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.SCALE;

/** Only renders the content element if the user is a streamer. Automatically updates when a non-streamer becomes a streamer. */
public class RequireStreamerElement extends BlockElement {
  private final EventCallback<LoginInfo> _onLoginChange = this::onLoginChange;
  private boolean isStreamer;
  private @Nullable Runnable onIsStreamer;

  private final IElement streamerElement;
  private final @Nullable LoadingSpinnerElement loadingSpinner;
  private final @Nullable LabelElement notStreamerLabel;
  private final @Nullable ErrorElement errorElement;

  public RequireStreamerElement(InteractiveScreen.InteractiveContext context, IElement parent, IElement streamerElement, boolean showLoadingSpinner, boolean showNotStreamerLabel, boolean showError) {
    super(context, parent);
    this.isStreamer = false;
    this.onIsStreamer = null;

    this.streamerElement = streamerElement.setVisible(false);
    this.loadingSpinner = !showLoadingSpinner ? null : new LoadingSpinnerElement(this.context, this)
        .setHorizontalAlignment(Layout.HorizontalAlignment.CENTRE)
        .setMaxContentWidth(gui(16))
        .setMargin(new Layout.RectExtension(gui(8)))
        .setVisible(false)
        .setSizingMode(Layout.SizingMode.FILL)
        .cast();
    this.notStreamerLabel = !showNotStreamerLabel ? null : SharedElements.INFO_LABEL.create(context, this)
        .setText("You are not a streamer. Please head over to the ChatMate website to become a streamer.")
        .setVisible(false)
        .cast();
    this.errorElement = !showError ? null : new ErrorElement(context, this, context.streamerApiStore::retry);

    super.addElement(this.streamerElement);
    super.addElement(this.loadingSpinner);
    super.addElement(this.notStreamerLabel);
    super.addElement(this.errorElement);

    super.registerStore(context.streamerApiStore, true);
    this.context.config.getLoginInfoEmitter().onChange(this._onLoginChange, this, true);
    super.addDisposer(() -> super.context.config.getLoginInfoEmitter().off(this));

    this.loadData();
  }

  @Override
  public ContainerElement setVisible(boolean visible) {
    super.setVisible(visible);
    if (!visible) {
      return this;
    }

    this.loadData();
    this.updateElements();
    return this;
  }

  @Override
  protected void onStoreUpdate() {
    this.updateElements();
  }

  private void onLoginChange(Event<LoginInfo> e) {
    if (e.getData().username == null) {
      this.isStreamer = false;
    }

    this.loadData();
    this.updateElements();
  }

  private void loadData() {
    context.streamerApiStore.loadData(null, null, false);
  }

  private boolean isStreamer() {
    @Nullable StreamerState data = super.context.streamerApiStore.getData();
    if (data == null) {
      return false;
    }

    @Nullable String username = super.context.config.getLoginInfoEmitter().get().username;
    return Collections.any(data.streamers, streamer -> Objects.equals(streamer.username, username));
  }

  private void updateElements() {
    this.onCheckStreamer();
    this.updateVisibility();

    @Nullable String error = super.context.streamerApiStore.getError();
    if (this.errorElement != null && error != null) {
      this.errorElement.setText(error);
    }
  }

  private void onCheckStreamer() {
    // don't do anything if we're already a streamer. it is assumed that we can never transition from streamer to non-streamer
    if (this.isStreamer || !super.getVisible()) {
      return;
    }

    this.isStreamer = this.isStreamer();
    if (this.onIsStreamer != null) {
      this.onIsStreamer.run();
    }
  }

  private void updateVisibility() {
    @Nullable String username = super.context.config.getLoginInfoEmitter().get().username;
    boolean isLoading = super.context.streamerApiStore.isLoading();
    @Nullable String error = super.context.streamerApiStore.getError();

    this.streamerElement.setVisible(username != null && this.isStreamer && !isLoading && error == null);

    if (this.notStreamerLabel != null) {
      this.notStreamerLabel.setVisible(username != null && !this.isStreamer && !isLoading && error == null);
    }

    if (this.loadingSpinner != null) {
      this.loadingSpinner.setVisible(username != null && isLoading);
    }

    if (this.errorElement != null) {
      this.errorElement.setVisible(username != null && !isLoading && error != null);
    }
  }

  public RequireStreamerElement onIsStreamer(@Nullable Runnable callback) {
    this.onIsStreamer = callback;
    return this;
  }

  private static class ErrorElement extends BlockElement {
    private final LabelElement errorLabel;
    private final TextButtonElement retryButton;

    public ErrorElement(InteractiveScreen.InteractiveContext context, IElement parent, Runnable onRetry) {
      super(context, parent);

      this.errorLabel = SharedElements.ERROR_LABEL.create(context, this);
      this.retryButton = SharedElements.TEXT_BUTTON_LIGHT.create(context, this)
          .setText("Retry")
          .setTextScale(SCALE)
          .setOnClick(onRetry)
          .cast();

      super.addElement(this.errorLabel);
      super.addElement(this.retryButton);
    }

    public void setText(String text) {
      this.errorLabel.setText(text);
    }
  }
}
