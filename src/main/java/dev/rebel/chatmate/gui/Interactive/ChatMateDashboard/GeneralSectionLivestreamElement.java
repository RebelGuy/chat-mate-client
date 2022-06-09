package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.TextInputElement;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamRequest;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamResponse;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamResponse.SetActiveLivestreamResponseData;
import dev.rebel.chatmate.models.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.services.util.TaskWrapper;
import dev.rebel.chatmate.services.util.TextHelpers;

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

  private String livestream;

  public GeneralSectionLivestreamElement(InteractiveContext context, IElement parent, ChatMateEndpointProxy chatMateEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);

    this.chatMateEndpointProxy = chatMateEndpointProxy;

    this.label = new LabelElement(context, this)
        .setText("Active livestream link:")
        .setMaxWidth(gui(200))
        .setSizingMode(SizingMode.FILL)
        .cast();
    this.livestreamInputField = new TextInputElement(context, this)
        .onTextChange(this::onChange)
        .setPlaceholder("Loading...")
        .setEnabled(this, false)
        .setMaxWidth(gui(100))
        .cast();

    Dim iconWidth = gui(context.fontRenderer.FONT_HEIGHT);
    RectExtension buttonMargin = new RectExtension(gui(2), ZERO);
    this.clearButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_CLEAR_ICON)
        .setMaxWidth(iconWidth)
        .setOnClick(this::onClear)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.confirmButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_TICK_ICON)
        .setMaxWidth(iconWidth)
        .setOnClick(this::onConfirm)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.refreshButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_REFRESH_ICON)
        .setMaxWidth(iconWidth)
        .setOnClick(this::onRefresh)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.copyButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_COPY_ICON)
        .setMaxWidth(iconWidth)
        .setOnClick(this::onCopy)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();
    this.openInBrowserButton = new IconButtonElement(context, this)
        .setImage(Asset.GUI_WEB_ICON)
        .setMaxWidth(iconWidth)
        .setOnClick(this::onOpenInBrowser)
        .setEnabled(this, false)
        .setMargin(buttonMargin)
        .cast();

    this.errorLabel = new LabelElement(context, this)
        .setOverflow(TextOverflow.SPLIT)
        .setMaxLines(4)
        .setColour(Colour.RED)
        .setAlignment(TextAlignment.CENTRE)
        .setFontScale(0.75f)
        .setSizingMode(SizingMode.FILL)
        .setVisible(false)
        .cast();

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

  private void onChange(String livestream) {
    this.livestream = livestream;
    this.enableControls();
  }

  private void onClear() {
    this.livestreamInputField.setText("");
  }

  private void onConfirm() {
    this.disableControls();
    SetActiveLivestreamRequest request = new SetActiveLivestreamRequest(this.livestream);
    this.chatMateEndpointProxy.setActiveLivestreamAsync(request, this::onSetLivestreamSuccess, this::onSetLivestreamError);
  }

  private void onRefresh() {
    this.disableControls();
    this.chatMateEndpointProxy.getStatusAsync(this::onGetStatusSuccess, this::onGetStatusError);
  }

  private void onCopy() {
    super.context.clipboardService.setClipboardString(this.livestream);
    this.setTemporaryTooltip("Copied to clipboard", 2000L);
  }

  private void onOpenInBrowser() {
    try {
      URI url = new URI(this.livestream);
      super.context.browserService.openWebLink(url);
    } catch (Exception e) {
      this.setTemporaryTooltip("Unable to open browser: " + e.getMessage(), 4000L);
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
    PublicLivestreamStatus status = getStatusResponseData.livestreamStatus;
    this.livestream = status == null ? "" : status.livestreamLink;
    this.livestreamInputField.setText(this.livestream);
    this.enableControls();
    this.errorLabel.setVisible(false);
  }

  private void onGetStatusError(Throwable error) {
    this.enableControls();
    this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error)).setVisible(true);
  }

  private void onSetLivestreamSuccess(SetActiveLivestreamResponseData setActiveLivestreamResponseData) {
    this.livestream = setActiveLivestreamResponseData.livestreamLink;
    this.livestreamInputField.setText(this.livestream);
    this.enableControls();
    this.errorLabel.setVisible(false);
  }

  private void onSetLivestreamError(Throwable error) {
    this.enableControls();
    this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error)).setVisible(true);
  }

  private void disableControls() {
    this.livestreamInputField.setEnabled(this, false);
    this.clearButton.setEnabled(this, false);
    this.confirmButton.setEnabled(this, false);
    this.refreshButton.setEnabled(this, false);
    this.copyButton.setEnabled(this, false);
    this.openInBrowserButton.setEnabled(this, false);
  }

  private void enableControls() {
    boolean empty = TextHelpers.isNullOrEmpty(this.livestream);
    this.livestreamInputField.setEnabled(this, true);
    this.clearButton.setEnabled(this, !empty);
    this.confirmButton.setEnabled(this, true);
    this.refreshButton.setEnabled(this, true);
    this.copyButton.setEnabled(this, !empty);
    this.openInBrowserButton.setEnabled(this, !empty);
  }
}
