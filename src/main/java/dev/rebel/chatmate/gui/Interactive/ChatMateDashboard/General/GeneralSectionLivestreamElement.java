package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.Interactive.TextInputElement;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamRequest;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamResponse.SetActiveLivestreamResponseData;
import dev.rebel.chatmate.models.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.services.util.TaskWrapper;
import dev.rebel.chatmate.services.util.TextHelpers;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;
import java.util.Timer;

public class GeneralSectionLivestreamElement extends ContainerElement {
  private final ChatMateEndpointProxy chatMateEndpointProxy;

  private final LabelElement label;
  private final TextInputElement livestreamInputField;
  private final IconButtonElement clearButton;
  private final IconButtonElement confirmButton;
  private final IconButtonElement refreshButton;
  private final IconButtonElement copyButton;
  private final IconButtonElement openInBrowserButton;
  private final LabelElement errorLabel;

  private @Nonnull String livestream;

  public GeneralSectionLivestreamElement(InteractiveContext context, IElement parent, ChatMateEndpointProxy chatMateEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);
    super.setName("GeneralSectionLivestreamElement");

    this.chatMateEndpointProxy = chatMateEndpointProxy;

    this.label = new LabelElement(context, this)
        .setText("Active livestream:")
        .setMaxWidth(gui(200))
        .setSizingMode(SizingMode.MINIMISE)
        .setPadding(new RectExtension(ZERO, gui(4), ZERO, ZERO))
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();
    this.livestreamInputField = new TextInputElement(context, this)
        .onTextChange(this::onChange)
        .setPlaceholder("No active livestream")
        .setEnabled(this, false)
        .setMaxWidth(gui(120))
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();

    Dim iconWidth = gui(context.fontEngine.FONT_HEIGHT).plus(gui(4));
    RectExtension buttonMargin = new RectExtension(gui(2), ZERO);
    this.clearButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_CLEAR_ICON)
        .setMaxContentWidth(iconWidth)
        .setOnClick(this::onClear)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.confirmButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_TICK_ICON)
        .setMaxContentWidth(iconWidth)
        .setOnClick(this::onConfirm)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.refreshButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_REFRESH_ICON)
        .setMaxContentWidth(iconWidth)
        .setOnClick(this::onRefresh)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.copyButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_COPY_ICON)
        .setMaxContentWidth(iconWidth)
        .setOnClick(this::onCopy)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.openInBrowserButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_WEB_ICON)
        .setMaxContentWidth(iconWidth)
        .setOnClick(this::onOpenInBrowser)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();

    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this);

    this.livestream = "";

    super.addElement(this.label);
    super.addElement(this.livestreamInputField);
    super.addElement(this.clearButton);
    super.addElement(this.confirmButton);
    super.addElement(this.refreshButton);
    super.addElement(this.copyButton);
    super.addElement(this.openInBrowserButton);
    super.addElement(this.errorLabel);
  }

  public void onShow() {
    this.onRefresh();
  }

  public void onHide() {
    // no op
  }

  private void onChange(String livestream) {
    this.livestream = livestream;
    this.disableLoadingState();
  }

  private void onClear() {
    this.livestreamInputField.setText("");
  }

  private void onConfirm() {
    this.enableLoadingState();
    SetActiveLivestreamRequest request = new SetActiveLivestreamRequest(Objects.equals(this.livestream, "") ? null : this.livestream);
    this.chatMateEndpointProxy.setActiveLivestreamAsync(request, this::onSetLivestreamSuccess, this::onSetLivestreamError);
  }

  private void onRefresh() {
    this.enableLoadingState();
    this.chatMateEndpointProxy.getStatusAsync(this::onGetStatusSuccess, this::onGetStatusError, true);
  }

  private void onCopy() {
    super.context.clipboardService.setClipboardString(this.livestream);
    this.setTemporaryTooltip("Copied to clipboard", 2000L);
  }

  private void onOpenInBrowser() {
    boolean result;
    try {
      URI url = new URI(this.livestream);
      result = super.context.urlService.openUrl(url);
    } catch (Exception ignore) {
      result = false;
    }

    if (!result) {
      this.setTemporaryTooltip("Unable to open the browser", 4000L);
    }
  }

  private void setTemporaryTooltip(String tooltip, long timeout) {
    super.setTooltip(tooltip);

    // um...
    new Timer().schedule(new TaskWrapper(() -> {
      if (Objects.equals(super.getTooltip(), tooltip)) {
        super.context.renderer.runSideEffect(() -> super.setTooltip(null));
      }
    }), timeout);
  }

  private void onGetStatusSuccess(GetStatusResponseData getStatusResponseData) {
    // we need to run these all as side effects because we don't want to modify the element tree during the render process
    super.context.renderer.runSideEffect(() -> {
      PublicLivestreamStatus status = getStatusResponseData.livestreamStatus;
      this.livestream = status == null ? "" : status.livestream.livestreamLink;
      this.livestreamInputField.setTextUnsafe(this.livestream);
      this.disableLoadingState();
    });
  }

  private void onGetStatusError(Throwable error) {
    super.context.renderer.runSideEffect(() -> {
      this.disableLoadingState();
      this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error)).setVisible(true);
    });
  }

  private void onSetLivestreamSuccess(SetActiveLivestreamResponseData setActiveLivestreamResponseData) {
    super.context.renderer.runSideEffect(() -> {
      this.livestream = setActiveLivestreamResponseData.livestreamLink == null ? "" : setActiveLivestreamResponseData.livestreamLink;
      this.livestreamInputField.setTextUnsafe(this.livestream);
      this.disableLoadingState();
    });
  }

  private void onSetLivestreamError(Throwable error) {
    super.context.renderer.runSideEffect(() -> {
      this.disableLoadingState();
      this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error)).setVisible(true);
    });
  }

  private void enableLoadingState() {
    this.livestreamInputField.setEnabled(this, false);
    this.clearButton.setEnabled(this, false);
    this.confirmButton.setEnabled(this, false);
    this.refreshButton.setEnabled(this, false);
    this.copyButton.setEnabled(this, false);
    this.openInBrowserButton.setEnabled(this, false);
    this.errorLabel.setVisible(false);
  }

  private void disableLoadingState() {
    boolean empty = TextHelpers.isNullOrEmpty(this.livestream);
    this.livestreamInputField.setEnabled(this, true);
    this.clearButton.setEnabled(this, !empty);
    this.confirmButton.setEnabled(this, true);
    this.refreshButton.setEnabled(this, true);
    this.copyButton.setEnabled(this, !empty);
    this.openInBrowserButton.setEnabled(this, !empty);
  }
}
