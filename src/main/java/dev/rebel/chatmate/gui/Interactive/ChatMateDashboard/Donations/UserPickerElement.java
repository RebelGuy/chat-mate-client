package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.models.api.user.SearchUserRequest;
import dev.rebel.chatmate.models.api.user.SearchUserResponse;
import dev.rebel.chatmate.models.api.user.SearchUserResponse.SearchUserResponseData;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.models.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.proxy.UserEndpointProxy;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class UserPickerElement extends ContainerElement {
  private final UserEndpointProxy userEndpointProxy;
  private final Consumer<PublicUser> onUserSelected;
  private final Debouncer searchUsersDebouncer;

  private final TextInputElement textInputElement;
  private final LoadingSpinnerElement loadingSpinnerElement;
  private final LabelElement errorLabel;
  private final DropdownMenu dropdownMenu;

  public UserPickerElement(InteractiveScreen.InteractiveContext context, IElement parent, @Nullable PublicUser defaultUser, Consumer<PublicUser> onUserSelected, UserEndpointProxy userEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);
    this.onUserSelected = onUserSelected;
    this.userEndpointProxy = userEndpointProxy;
    this.searchUsersDebouncer = new Debouncer(1000, () -> context.renderer.runSideEffect(this::onSearchUser));

    this.textInputElement = new TextInputElement(context, this, userToString(defaultUser))
        .setPlaceholder("Start typing a name") // todo: fix clipping out to the right
        .onTextChange(this::onTextChange);
    this.loadingSpinnerElement = new LoadingSpinnerElement(context, this)
        .setMaxContentWidth(gui(8))
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        .setMargin(new RectExtension(ZERO, gui(2), gui(-14), ZERO)) // place on top of the text box
        .setVisible(false)
        .cast();
    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this)
        .setPadding(new RectExtension(ZERO, ZERO, gui(2), ZERO))
        .cast();
    this.dropdownMenu = new DropdownMenu(context, this.textInputElement)
        .setSizingMode(SizingMode.FILL) // todo: set anchor to the top if we are near the lower section of the screen. if possible, also find a fix for not clipping the menu off-screen, but not high priority
        .cast();

    super.addElement(this.textInputElement);
    super.addElement(this.loadingSpinnerElement);
    super.addElement(this.errorLabel);
    super.addElement(this.dropdownMenu);
  }

  private void onTextChange(String text) {
    this.searchUsersDebouncer.doDebounce();
  }

  @Override
  public void onFocus(Events.IEvent<Events.FocusEventData> e) {
    // todo: make a new request with the current text and show dropdown
  }

  // todo: blur and focus doesn't work... is it only for input elements? normal elements should be able to switch on focusability.
  // then would need to propagete focus/blur events (but I think we might already be doing that?)
  @Override
  public void onBlur(Events.IEvent<Events.FocusEventData> e) {
    this.searchUsersDebouncer.cancel();
    this.dropdownMenu.setExpanded(false);
    this.onUserSelected.accept(null);
  }

  @Override
  public void onKeyDown(Events.IEvent<KeyboardEventData.In> e) {
    // todo: if escape, close dropdown
    // if enter, search immediately and cancel debouncer
  }

  private void onSelection(PublicUser user) {
    this.textInputElement.setTextUnsafe(userToString(user));
    this.onUserSelected.accept(user);
  }

  private void onSearchUser() {
    this.loadingSpinnerElement.setVisible(true);
    this.errorLabel.setVisible(false);

    String searchTerm = this.textInputElement.getText();
    this.userEndpointProxy.searchUser(
        new SearchUserRequest(searchTerm),
        r -> super.context.renderer.runSideEffect(() -> this.onSearchUserResponse(r)),
        e -> super.context.renderer.runSideEffect(() -> this.onSearchUserError(e))
    );
  }

  private void onSearchUserError(Throwable throwable) {
    this.loadingSpinnerElement.setVisible(false);
    this.dropdownMenu.clear();
    this.errorLabel.setText(EndpointProxy.getApiErrorMessage(throwable))
        .setVisible(true);
  }

  private void onSearchUserResponse(SearchUserResponseData response) {
    this.loadingSpinnerElement.setVisible(false);
    this.dropdownMenu.clear();

    if (response.results.length == 0) {
      this.errorLabel.setText("No users found")
          .setVisible(true);
    } else {
      this.dropdownMenu.setExpanded(true);
      for (PublicUserNames userNames : response.results) {
        this.dropdownMenu.addOption(userNames.user.userInfo.channelName, () -> this.onSelection(userNames.user));
      }
    }
  }

  private static String userToString(PublicUser user) {
    if (user == null) {
      return "";
    }

    // todo: include rank and level?
    return user.userInfo.channelName;
  }
}
