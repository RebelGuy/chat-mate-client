package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.config.Config.LoginInfo;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler.EventCallback;

import javax.annotation.Nullable;

public class RequireLoggedInElement extends WrapperElement {
  private final EventCallback<LoginInfo> _onLoginChange = this::onLoginChange;
  @Nullable private Runnable onLoggedIn;
  @Nullable private Runnable onLoggedOut;

  public RequireLoggedInElement(InteractiveScreen.InteractiveContext context, IElement parent, IElement content) {
    super(context, parent, content);

    this.context.config.getLoginInfoEmitter().onChange(this._onLoginChange, this);
    super.addDisposer(() -> super.context.config.getLoginInfoEmitter().off(this));
  }

  private void onLoginChange(Event<LoginInfo> e) {
    boolean isLoggedIn = e.getData().loginToken != null;
    super.setVisible(isLoggedIn);

    if (isLoggedIn && this.onLoggedIn != null) {
      this.onLoggedIn.run();
    } else if (!isLoggedIn && this.onLoggedOut != null) {
      this.onLoggedOut.run();
    }
  }

  public RequireLoggedInElement onLoggedIn(@Nullable Runnable callback) {
    this.onLoggedIn = callback;
    return this;
  }

  public RequireLoggedInElement onLoggedOut(@Nullable Runnable callback) {
    onLoggedOut = callback;
    return this;
  }
}
