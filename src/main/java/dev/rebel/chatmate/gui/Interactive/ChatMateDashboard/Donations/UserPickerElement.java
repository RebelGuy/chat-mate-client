package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.api.publicObjects.user.PublicUserSearchResults;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.DropdownMenu.AnchorBoxSizing;
import dev.rebel.chatmate.gui.Interactive.DropdownMenu.VerticalPosition;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.*;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.chat.Styles;
import dev.rebel.chatmate.api.models.user.SearchUserRequest;
import dev.rebel.chatmate.api.models.user.SearchUserResponse.SearchUserResponseData;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.proxy.UserEndpointProxy;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Debouncer;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.gui.chat.Styles.getLevelStyle;
import static dev.rebel.chatmate.gui.chat.Styles.styledText;
import static dev.rebel.chatmate.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.util.TextHelpers.toSentenceCase;

public class UserPickerElement extends ContainerElement {
  private final UserEndpointProxy userEndpointProxy;
  private final Consumer<PublicUser> onUserSelected;
  private final Debouncer searchUsersDebouncer;
  private final MessageService messageService;

  private final TextInputElement textInputElement;
  private final LoadingSpinnerElement loadingSpinnerElement;
  private final LabelElement errorLabel;
  private final DropdownMenu dropdownMenu;

  public UserPickerElement(InteractiveScreen.InteractiveContext context, IElement parent, @Nullable PublicUser defaultUser, Consumer<PublicUser> onUserSelected, UserEndpointProxy userEndpointProxy, MessageService messageService) {
    super(context, parent, LayoutMode.INLINE);
    this.onUserSelected = onUserSelected;
    this.userEndpointProxy = userEndpointProxy;
    this.searchUsersDebouncer = new Debouncer(500, () -> context.renderer.runSideEffect(this::onSearchUser));
    this.messageService = messageService;

    this.textInputElement = new TextInputElement(context, this, this.userToString(defaultUser, null))
        .setPlaceholder("Start typing a name")
        .onTextChange(this::onTextChange);
    this.loadingSpinnerElement = new LoadingSpinnerElement(context, this)
        .setLineWidth(gui(1))
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
    this.dropdownMenu = new DropdownMenu(context, this.textInputElement, AnchorBoxSizing.BORDER)
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
    if (e.getData().isPressed(Keyboard.KEY_ESCAPE) && this.dropdownMenu.getVisible()) {
      this.searchUsersDebouncer.cancel();
      this.dropdownMenu.setVisible(false);
      e.stopPropagation();
    } else if (e.getData().isPressed(Keyboard.KEY_RETURN)) {
      this.searchUsersDebouncer.cancel();
      this.onSearchUser();
      e.stopPropagation();
    }
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

  private void onSelection(PublicUser user, String displayName) {
    this.textInputElement.setTextUnsafe(displayName);
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
      for (PublicUserSearchResults result : Collections.reverse(Collections.orderBy(Collections.list(response.results), r -> r.user.levelInfo.level + r.user.levelInfo.levelProgress))) {
        boolean hasRanks = result.user.activeRanks.length > 0;

        Font font = Styles.VIEWER_NAME_FONT.create(super.context.dimFactory);
        LabelElement nameElement = new LabelElement(super.context, this)
            .setText(this.userToString(result.user, result.matchedChannel.displayName))
            .setOverflow(TextOverflow.SPLIT)
            .setFontScale(0.75f)
            .setFont(font)
            .setMinWidth(gui(10))
            .setPadding(new RectExtension(gui(1), gui(1), gui(1), hasRanks ? ZERO : gui(1)))
            .cast();
        InlineElement nameContainer = new InlineElement(super.context, this)
            .setAllowShrink(true)
            .addElement(nameElement)
            .setSizingMode(SizingMode.FILL)
            .cast();

        if (result.user.registeredUser != null) {
            ImageElement verificationBadgeElement = new ImageElement(super.context, this)
                .setImage(Asset.GUI_VERIFICATION_ICON_WHITE_SMALL)
                .setColour(font.getColour())
                .setMaxContentWidth(super.fontEngine.FONT_HEIGHT_DIM.times(0.75f / 2))
                .setMargin(RectExtension.fromLeft(gui(-1.5f)))
                .cast();
            nameContainer.addElement(verificationBadgeElement);
        }

        LabelElement ranksElement = !hasRanks ? null : new LabelElement(super.context, this)
            .setText(String.join(", ", Collections.map(Collections.list(result.user.activeRanks), r -> toSentenceCase(r.rank.displayNameNoun))))
            .setOverflow(TextOverflow.SPLIT)
            .setFontScale(0.5f)
            .setColour(Colour.GREY50)
            .setPadding(new RectExtension(gui(1), gui(1), ZERO, gui(1)))
            .setSizingMode(SizingMode.FILL)
            .cast();

        BlockElement menuItemContainer = new BlockElement(super.context, this)
            .addElement(nameContainer)
            .addElement(ranksElement)
            .setOnClick(() -> this.onSelection(result.user, result.matchedChannel.displayName))
            .cast();
        this.dropdownMenu.addOption(new BackgroundElement(super.context, this, menuItemContainer)
            .setCornerRadius(gui(2))
            .setHoverColour(Colour.GREY75.withAlpha(0.2f))
            .setMargin(new RectExtension(gui(1), gui(1)))
        );
      }
    }
  }

  private String userToString(PublicUser user, @Nullable String displayName) {
    if (user == null) {
      return "";
    }

    Integer level = user.levelInfo.level;
    IChatComponent levelComponent = styledText(level.toString(), getLevelStyle(level));
    IChatComponent nameComponent = this.messageService.getUserComponent(user, displayName);
    return joinComponents(" ", Collections.list(levelComponent, nameComponent)).getFormattedText();
  }
}
