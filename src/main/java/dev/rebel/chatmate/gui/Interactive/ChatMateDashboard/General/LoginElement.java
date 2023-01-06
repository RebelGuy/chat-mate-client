package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.models.account.LoginRequest;
import dev.rebel.chatmate.api.proxy.AccountEndpointProxy;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.config.Config.LoginInfo;
import dev.rebel.chatmate.events.models.ConfigEventData;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.Interactive.TextInputElement.InputType;
import dev.rebel.chatmate.util.Objects;
import dev.rebel.chatmate.util.TextHelpers;

import java.util.function.Function;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.ERROR_LABEL;
import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.SCALE;
import static dev.rebel.chatmate.util.TextHelpers.isNullOrEmpty;

public class LoginElement extends BlockElement {
  private final TextInputElement usernameInput;
  private final TextInputElement passwordInput;
  private final ButtonElement loginButton;
  private final LabelElement loggedInLabel;
  private final ButtonElement logoutButton;
  private final LoadingSpinnerElement loadingSpinner;
  private final LabelElement errorLabel;
  private final AccountEndpointProxy accountEndpointProxy;
  private final Function<ConfigEventData.In<LoginInfo>, ConfigEventData.Out<LoginInfo>> _onLoginChange = this::onLoginChange;

  public LoginElement(InteractiveContext context, IElement parent, AccountEndpointProxy accountEndpointProxy) {
    super(context, parent);

    this.usernameInput = new TextInputElement(context, this)
        .setOnSubmit(this::onRequestLogin)
        .setPlaceholder("Username")
        .setTextScale(SCALE)
        .onTextChange(this::onUsernameChange)
        .setTabIndex(0)
        .setMargin(new RectExtension(ZERO, gui(2), gui(2), gui(2)))
        .setMaxWidth(gui(120))
        .setMinWidth(gui(40))
        .cast();
    this.passwordInput = new TextInputElement(context, this)
        .setOnSubmit(this::onRequestLogin)
        .setPlaceholder("Password")
        .setType(InputType.PASSWORD)
        .setTextScale(SCALE)
        .onTextChange(this::onPasswordChange)
        .setTabIndex(1)
        .setMargin(new RectExtension(gui(2), ZERO, gui(2), gui(2)))
        .setMaxWidth(gui(120))
        .setMinWidth(gui(40))
        .cast();
    this.loginButton = new TextButtonElement(context, this)
        .setText("Login")
        .setTextScale(SCALE)
        .setEnabled(this.usernameInput, false)
        .setEnabled(this.passwordInput, false)
        .setOnClick(this::onRequestLogin)
        .setMargin(new RectExtension(gui(4), ZERO))
        .cast();
    this.loggedInLabel = new LabelElement(context, this)
        .setFontScale(SCALE)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setMargin(RectExtension.fromRight(gui(4)))
        .cast();
    this.logoutButton = new TextButtonElement(context, this)
        .setText("Logout")
        .setTextScale(SCALE)
        .setOnClick(this::onRequestLogout)
        .cast();
    this.loadingSpinner = new LoadingSpinnerElement(context, this)
        .setLineWidth(gui(1))
        .setMaxContentWidth(gui(8))
        .setPadding(new RectExtension(gui(2)))
        .setVisible(false)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();
    this.errorLabel = ERROR_LABEL.create(context, this);
    this.accountEndpointProxy = accountEndpointProxy;

    super.addElement(new InlineElement(context, this)
        .addElement(this.usernameInput)
        .addElement(this.passwordInput)
        .addElement(this.loginButton)
        .addElement(this.loggedInLabel)
        .addElement(this.logoutButton)
        .addElement(this.loadingSpinner)
        .setAllowShrink(true)
    );
    super.addElement(this.errorLabel);

    context.config.getLoginInfoEmitter().onChange(this._onLoginChange, this, true);
  }

  private ConfigEventData.Out<LoginInfo> onLoginChange(ConfigEventData.In<LoginInfo> in) {
    boolean isLoggedOut = in.data.loginToken == null;

    this.usernameInput.setVisible(isLoggedOut);
    this.passwordInput.setVisible(isLoggedOut);
    this.loginButton.setVisible(isLoggedOut);
    this.loggedInLabel
        .setText(String.format("Hi, %s!", context.config.getLoginInfoEmitter().get().username))
        .setVisible(!isLoggedOut);
    this.logoutButton.setVisible(!isLoggedOut);

    return new ConfigEventData.Out<>();
  }

  private void onUsernameChange(String username) {
    this.loginButton.setEnabled(this.usernameInput, !isNullOrEmpty(username));
  }

  private void onPasswordChange(String password) {
    this.loginButton.setEnabled(this.passwordInput, password.length() > 0);
  }

  private void onRequestLogin() {
    if (!this.loginButton.getEnabled()) {
      return;
    }

    this.onRequestStart();

    LoginRequest request = new LoginRequest(this.usernameInput.getText(), this.passwordInput.getText());
    this.accountEndpointProxy.loginAsync(
        request,
        r -> super.context.renderer.runSideEffect(() -> { this.onLoginResponse(request.username, r.loginToken); this.onRequestEnd(); }),
        e -> super.context.renderer.runSideEffect(() -> { this.onResponseError(e); this.onRequestEnd(); })
    );
  }

  private void onRequestLogout() {
    this.onRequestStart();

    this.accountEndpointProxy.logoutAsync(
        r -> super.context.renderer.runSideEffect(() -> { this.onLogout(); this.onRequestEnd(); }),
        e -> super.context.renderer.runSideEffect(() -> { this.onResponseError(e); this.onRequestEnd(); })
    );
  }

  private void onLoginResponse(String username, String loginToken) {
    super.context.config.getLoginInfoEmitter().set(new LoginInfo(username, loginToken));
  }

  private void onLogout() {
    super.context.config.getLoginInfoEmitter().set(new LoginInfo(null, null));
  }

  private void onResponseError(Throwable e) {
    String errorMessage = EndpointProxy.getApiErrorMessage(e);
    this.errorLabel.setText(errorMessage);
    this.errorLabel.setVisible(true);
  }

  private void onRequestStart() {
    this.loadingSpinner.setVisible(true);
    this.errorLabel.setVisible(false);
    this.usernameInput.setEnabled(this, false);
    this.passwordInput.setEnabled(this, false);
    this.loginButton.setEnabled(this, false);
    this.logoutButton.setEnabled(this, false);
  }

  private void onRequestEnd() {
    this.loadingSpinner.setVisible(false);
    this.usernameInput.setEnabled(this, true);
    this.passwordInput.setEnabled(this, true);
    this.loginButton.setEnabled(this, true);
    this.logoutButton.setEnabled(this, true);
  }
}
