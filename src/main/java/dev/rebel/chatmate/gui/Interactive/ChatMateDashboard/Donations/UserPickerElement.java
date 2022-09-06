package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.DropdownMenuV2.AnchorBoxSizing;
import dev.rebel.chatmate.gui.Interactive.DropdownMenuV2.VerticalPosition;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.*;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.models.api.user.SearchUserRequest;
import dev.rebel.chatmate.models.api.user.SearchUserResponse;
import dev.rebel.chatmate.models.api.user.SearchUserResponse.SearchUserResponseData;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.models.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.proxy.UserEndpointProxy;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.TextHelpers.toSentenceCase;

public class UserPickerElement extends ContainerElement {
  private final UserEndpointProxy userEndpointProxy;
  private final Consumer<PublicUser> onUserSelected;
  private final Debouncer searchUsersDebouncer;

  private final TextInputElement textInputElement;
  private final LoadingSpinnerElement loadingSpinnerElement;
  private final LabelElement errorLabel;
  private final DropdownMenuV2 dropdownMenu;

  public UserPickerElement(InteractiveScreen.InteractiveContext context, IElement parent, @Nullable PublicUser defaultUser, Consumer<PublicUser> onUserSelected, UserEndpointProxy userEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);
    this.onUserSelected = onUserSelected;
    this.userEndpointProxy = userEndpointProxy;
    this.searchUsersDebouncer = new Debouncer(1000, () -> context.renderer.runSideEffect(this::onSearchUser));

    this.textInputElement = new TextInputElement(context, this, userToString(defaultUser))
        .setPlaceholder("Start typing a name")
        .onTextChange(this::onTextChange);
    this.loadingSpinnerElement = new LoadingSpinnerElement(context, this)
        .setMaxContentWidth(gui(8))
        .setLayoutGroup(LayoutGroup.CHILDREN) // don't let it influence the user picker box
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        .setMargin(new RectExtension(gui(-10), ZERO, ZERO, ZERO)) // place within the text box, to the right
        .setVisible(false)
        .cast();
    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this)
        .setPadding(new RectExtension(ZERO, ZERO, gui(2), ZERO))
        .cast();
    this.dropdownMenu = new DropdownMenuV2(context, this.textInputElement, AnchorBoxSizing.BORDER)
        .setSizingMode(SizingMode.FILL)
        .cast();

    super.addElement(this.textInputElement);
    super.addElement(this.loadingSpinnerElement);
    super.addElement(this.errorLabel);
    super.addElement(this.dropdownMenu);
  }

  private void onTextChange(String text) {
    this.searchUsersDebouncer.doDebounce();
  }

  public UserPickerElement setFontScale(float textScale) {
    this.textInputElement.setTextScale(textScale);
    return this;
  }

  @Override
  public void onMouseDown(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      this.dropdownMenu.setVisible(true);
    }
  }

  @Override
  public void onKeyDown(Events.IEvent<KeyboardEventData.In> e) {
    // todo: if escape, close dropdown
    // if enter, search immediately and cancel debouncer
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    // ensure the dropdown menu extends in the direction with more room
    Dim spaceTop = this.textInputElement.getBox().getY();
    Dim spaceBottom = context.dimFactory.getMinecraftSize().getY().minus(this.textInputElement.getBox().getBottom());
    if (spaceTop.gt(spaceBottom)) {
      this.dropdownMenu.setVerticalPosition(VerticalPosition.ABOVE);
    } else {
      this.dropdownMenu.setVerticalPosition(VerticalPosition.BELOW);
    }
  }

  private void onSelection(PublicUser user) {
    this.textInputElement.setTextUnsafe(userToString(user));
    this.dropdownMenu.setVisible(false);
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
    this.dropdownMenu.clearOptions();
    this.errorLabel.setText(EndpointProxy.getApiErrorMessage(throwable))
        .setVisible(true);
  }

  private void onSearchUserResponse(SearchUserResponseData response) {
    this.loadingSpinnerElement.setVisible(false);
    this.dropdownMenu.clearOptions();

    if (response.results.length == 0) {
      this.errorLabel.setText("No users found")
          .setVisible(true);
    } else {
      this.dropdownMenu.setVisible(true);
      for (PublicUserNames userNames : response.results) {
        LabelElement option = new LabelElement(super.context, this)
            .setText(userToString(userNames.user))
            .setOverflow(TextOverflow.SPLIT)
            .setFontScale(0.75f)
            .setOnClick(() -> this.onSelection(userNames.user))
            .setHoverFont(new Font().withColour(Colour.ACTION_HOVER))
            .setPadding(new RectExtension(gui(1), gui(1)))
            .setSizingMode(SizingMode.FILL)
            .cast();
        this.dropdownMenu.addOption(new BackgroundElement(super.context, this, option)
            .setCornerRadius(gui(2))
            .setHoverColour(Colour.LTGREY.withAlpha(0.2f))
            .setMargin(new RectExtension(gui(1), gui(1)))
        );
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
