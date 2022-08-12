package dev.rebel.chatmate.gui.Interactive.rank;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.DropdownMenu.Anchor;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.Interactive.rank.Adapters.*;
import dev.rebel.chatmate.gui.Interactive.rank.Adapters.EndpointAdapter.RankResult;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.models.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.models.publicObjects.rank.PublicChannelRankChange.Platform;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.rebel.chatmate.services.util.TextHelpers.*;

public class ManageRanksModal extends ModalElement {
  private final PublicUser user;
  private final Adapters adapters;

  /** onSuccess and onError callback. */
  private @Nullable BiConsumer<Runnable, Consumer<String>> onSubmit;
  /** If it returns true, the default close behaviour is suppressed. */
  private @Nullable Supplier<Boolean> onClose;
  /** If it returns true, the submit button can be pressed. If it returns false, the submit button cannot be pressed.
   * If it returns null, the submit button is not shown. */
  private @Nullable Supplier<Boolean> onValidate;

  public ManageRanksModal(InteractiveContext context, InteractiveScreen parent, PublicUser user, Adapters adapters) {
    super(context, parent);
    super.width = gui(300);
    this.user = user;
    this.adapters = adapters;
  }

  @Override
  public void onInitialise() {
    super.onInitialise();

    super.setTitle(adapters.getTitle(user));
    super.setBody(new RankList(context, this, this.user, adapters));
  }

  @Override
  protected @Nullable Boolean validate() {
    if (this.onValidate == null) {
      return null;
    } else {
      return this.onValidate.get();
    }
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    if (this.onSubmit != null) {
      this.onSubmit.accept(onSuccess, onError);
    }
  }

  @Override
  protected void close() {
    if (this.onClose == null || !this.onClose.get()) {
      super.onCloseScreen();
    }
  }

  private static class RankList extends ContainerElement {
    private final ManageRanksModal modal;
    private final Adapters adapters;
    private final EndpointAdapter endpointAdapter;
    private final TableAdapter tableAdapter;
    private final CreateAdapter createAdapter;
    private final PublicUser user;

    private final LabelElement titleLabel;
    private final TextButtonElement createNewRankButton;
    private final DropdownMenu createNewRankDropdown;
    private final WrapperElement listWrapper;
    private final ElementReference listReference;

    public RankList(InteractiveContext context, ManageRanksModal parent, PublicUser user, Adapters adapters) {
      super(context, parent, LayoutMode.INLINE);

      this.modal = parent;
      this.adapters = adapters;
      this.endpointAdapter = adapters.endpointAdapter;
      this.tableAdapter = adapters.tableAdapter;
      this.createAdapter = adapters.createAdapter;
      this.user = user;

      parent.onValidate = null;
      parent.onSubmit = null;
      parent.onClose = null;

      this.titleLabel = new LabelElement(context, this).setText(tableAdapter.tableHeader);

      this.createNewRankButton = new TextButtonElement(context, this)
          .setText("Create new")
          .setEnabled(this, false)
          .setHorizontalAlignment(HorizontalAlignment.RIGHT)
          .cast();

      // todo: move into CreateRank element
      this.createNewRankDropdown = new DropdownMenu(context, this.createNewRankButton)
          .setAnchor(Anchor.LEFT)
          .setBorder(new RectExtension(gui(1)))
          .setSizingMode(SizingMode.FILL)
          .cast();
      this.createNewRankButton.setOnClick(this.createNewRankDropdown::toggleExpanded);

      // populate the "create new" button with the accessible ranks
      this.endpointAdapter.getAccessibleRanksAsync(this::onAccessibleRanksLoaded, this::onRanksLoadError);

      this.listReference = new ElementReference(context, this).setUnderlyingElement(
          new LabelElement(context, this)
              .setText(this.tableAdapter.loadingRanksMessage)
              .setFontScale(0.75f)
              .setAlignment(TextAlignment.CENTRE)
              .setSizingMode(SizingMode.FILL)
      );
      this.listWrapper = new WrapperElement(context, this, this.listReference)
          .setMargin(new RectExtension(ZERO, gui(6)))
          .cast();

      this.endpointAdapter.getRanksAsync(this.user.id, this::onRanksLoaded, this::onRanksLoadError);
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      this.modal.setCloseText("Close");

      super.addElement(this.titleLabel);
      super.addElement(this.createNewRankButton);
      super.addElement(this.createNewRankDropdown);
      super.addElement(this.listWrapper);
    }

    private void onRanksLoadError(Throwable error) {
      this.context.renderer.runSideEffect(() -> {
        this.listReference.setUnderlyingElement(
            new LabelElement(this.context, this)
                .setText(tableAdapter.getLoadingFailedMessage(EndpointProxy.getApiErrorMessage(error)))
                .setOverflow(TextOverflow.SPLIT)
                .setColour(Colour.RED)
                .setFontScale(0.75f)
                .setMaxLines(5)
                .setAlignment(TextAlignment.CENTRE)
                .setSizingMode(SizingMode.FILL)
        );
      });
    }

    private void onAccessibleRanksLoaded(PublicRank[] accessibleRanks) {
      super.context.renderer.runSideEffect(() -> {
        this.createNewRankButton.setEnabled(this, true);
        for (PublicRank accessibleRank : accessibleRanks) {
          if (this.createAdapter.shouldIncludeRank(accessibleRank)) {
            this.createNewRankDropdown.addOption(toSentenceCase(accessibleRank.displayNameNoun), () -> this.onCreateNewRank(accessibleRank));
          }
        }
      });
    }

    private void onRanksLoaded(PublicUserRank[] ranks) {
      this.context.renderer.runSideEffect(() -> {
        IElement element;
        if (ranks.length == 0) {
          element = new LabelElement(this.context, this)
              .setText(tableAdapter.noRanksMessage)
              .setFontScale(0.75f)
              .setAlignment(TextAlignment.CENTRE)
              .setSizingMode(SizingMode.FILL);
        } else {
          element = new TableElement<PublicUserRank>(
              this.context,
              this,
              Collections.list(ranks),
              this.tableAdapter.getColumns(),
              rank -> this.tableAdapter.getRow(super.context, this, rank)
          ).setMinHeight(gui(50))
              .setOnClickItem(this::onShowDetails)
              .setSizingMode(SizingMode.FILL)
              .setPadding(new RectExtension(ZERO, gui(4)));
        }

        this.listReference.setUnderlyingElement(element);
      });
    }

    private void onShowDetails(PublicUserRank rank) {
      this.modal.setBody(new RankDetails(this.context, this.modal, this.user, rank, this.adapters));
    }

    private void onCreateNewRank(PublicRank rank) {
      this.modal.setBody(new RankCreate(this.context, this.modal, this.adapters, this.user, rank));
    }
  }

  private static class RankDetails extends ContainerElement {
    private final ManageRanksModal modal;
    private final Adapters adapters;
    private final ChannelRankChangeAdapter channelRankChangeAdapter;
    private final EndpointAdapter endpointAdapter;
    private final DetailsAdapter detailsAdapter;
    private final PublicUser user;
    private final PublicRank underlyingRank;

    private final LabelElement titleLabel;
    private final @Nullable LabelElement statusLabel;
    private final @Nullable SideBySideElement issuedAtElement; // null if rank is null
    private final @Nullable SideBySideElement expiresAtElement; // null if rank is null
    private final @Nullable SideBySideElement messageElement; // null if rank is null
    private final @Nullable SideBySideElement revokedAtElement;
    private final @Nullable SideBySideElement revokedMessageElement;
    private final @Nullable TextInputElement revokeRankTextInputElement;
    private final @Nullable LabelElement rankErrorHeader;
    private final @Nullable LabelElement rankErrorLabel;
    private final @Nullable LabelElement channelActionsHeader;
    private final @Nullable BlockElement channelActionsList;

    /** Use this constructor when cold-displaying a rank. */
    public RankDetails(InteractiveContext context, ManageRanksModal parent, PublicUser user, @Nonnull PublicUserRank rank, Adapters adapters) {
      this(context, parent, user, rank.rank, rank, adapters, null, null);
    }

    /** Use this constructor in response to an API response. */
    public RankDetails(InteractiveContext context, ManageRanksModal parent, PublicUser user, @Nonnull PublicRank underlyingRank, @Nonnull RankResult rank, Adapters adapters) {
      this(context, parent, user, underlyingRank, rank.rank, adapters, rank.rankError, rank.channelRankChanges);
    }

    private RankDetails(InteractiveContext context, ManageRanksModal parent, PublicUser user, @Nonnull PublicRank underlyingRank, @Nullable PublicUserRank rank, Adapters adapters, @Nullable String rankError, @Nullable PublicChannelRankChange[] channelRankChanges) {
      super(context, parent, LayoutMode.INLINE);

      this.modal = parent;
      this.adapters = adapters;
      this.modal.onValidate = null;
      this.modal.onSubmit = null;
      this.modal.onClose = this::onBack;

      this.channelRankChangeAdapter = adapters.channelRankChangeAdapter;
      this.endpointAdapter = adapters.endpointAdapter;
      this.detailsAdapter = adapters.detailsAdapter;
      this.user = user;
      this.underlyingRank = underlyingRank;

      String status = null;
      if (rank != null && rank.revokedAt != null) {
        status = "REVOKED";
      } else if (rank != null && rank.expirationTime != null && rank.expirationTime <= new Date().getTime()) {
        status = "EXPIRED";
      }
      this.titleLabel = new LabelElement(context, this)
          .setText(this.detailsAdapter.getHeader(underlyingRank))
          .setHorizontalAlignment(HorizontalAlignment.LEFT)
          .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(4)))
          .cast();
      this.statusLabel = status == null ? null : new LabelElement(context, this)
          .setText(status)
          .setColour(Colour.RED)
          .setHorizontalAlignment(HorizontalAlignment.RIGHT)
          .cast();

      if (rank == null) {
        this.issuedAtElement = null;
        this.expiresAtElement = null;
        this.messageElement = null;
        this.revokedAtElement = null;
        this.revokedMessageElement = null;
        this.revokeRankTextInputElement = null;
        // note: submit button already disabled above

      } else {
        this.issuedAtElement = new SideBySideElement(context, this)
            .setElementPadding(gui(4))
            .addElement(1, new LabelElement(context, this)
                .setText("Issued at:"))
            .addElement(2, new LabelElement(context, this)
                .setText(dateToSecondAccuracy(rank.issuedAt))
            ).cast();

        String expirationText;
        if (rank.expirationTime == null) {
          expirationText = "Never";
        } else if (!rank.isActive) {
          expirationText = dateToSecondAccuracy(rank.expirationTime);
        } else {
          String remaining = approximateDuration(rank.expirationTime - new Date().getTime());
          expirationText = String.format("%s (in %s)", dateToSecondAccuracy(rank.expirationTime), remaining);
        }

        this.expiresAtElement = new SideBySideElement(context, this)
            .setElementPadding(gui(4))
            .addElement(1, new LabelElement(context, this)
                .setText(rank.isActive ? "Expires at:" : "Expired at:"))
            .addElement(2, new LabelElement(context, this)
                .setText(expirationText)
            ).cast();
        this.messageElement = new SideBySideElement(context, this)
            .setElementPadding(gui(4))
            .addElement(1, new LabelElement(context, this)
                .setText("Message:"))
            .addElement(2, new LabelElement(context, this)
                .setText(nonNull(rank.message, "n/a"))
                .setOverflow(TextOverflow.SPLIT)
            ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(8)))
            .cast();

        if (rank.isActive) {
          this.revokedAtElement = null;
          this.revokedMessageElement = null;
          this.revokeRankTextInputElement = new TextInputElement(context, this)
              .setPlaceholder(toSentenceCase(this.getRevokeName()) + " reason")
              .setSizingMode(SizingMode.FILL)
              .setPadding(new RectExtension(gui(4), gui(1)))
              .setMargin(new RectExtension(ZERO, gui(4)))
              .cast();
          this.modal.onValidate = () -> true; // nothing to validate
          this.modal.onSubmit = this::onRevokeRank;

        } else if (rank.revokedAt != null) {
          this.revokedAtElement = new SideBySideElement(context, this)
              .setElementPadding(gui(4))
              .addElement(1, new LabelElement(context, this)
                  .setText("Revoked at:"))
              .addElement(2, new LabelElement(context, this)
                  .setText(dateToSecondAccuracy(rank.revokedAt))
              ).cast();
          this.revokedMessageElement = new SideBySideElement(context, this)
              .setElementPadding(gui(4))
              .addElement(1, new LabelElement(context, this)
                  .setText("Revoke message:"))
              .addElement(2, new LabelElement(context, this)
                  .setText(nonNull(rank.revokeMessage, "n/a"))
                  .setOverflow(TextOverflow.SPLIT)
              ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(8)))
              .cast();
          this.revokeRankTextInputElement = null;
          this.modal.onValidate = () -> null;

        } else {
          // rank has expired
          this.revokedAtElement = null;
          this.revokedMessageElement = null;
          this.revokeRankTextInputElement = null;
          this.modal.onValidate = () -> null;
        }
      }

      if (rankError == null) {
        this.rankErrorHeader = null;
        this.rankErrorLabel = null;
      } else {
        this.rankErrorHeader = new LabelElement(context, this)
            .setText(this.channelRankChangeAdapter.internalRankErrorHeaderMessage)
            .setSizingMode(SizingMode.FILL)
            .setMargin(new RectExtension(ZERO, ZERO, gui(4), ZERO))
            .cast();
        this.rankErrorLabel = new LabelElement(context, this)
            .setText(rankError)
            .setSizingMode(SizingMode.FILL)
            .setMargin(new RectExtension(ZERO, ZERO, gui(2), ZERO))
            .cast();
      }

      if (channelRankChanges == null) {
        this.channelActionsHeader = null;
        this.channelActionsList = null;

      } else if (channelRankChanges.length == 0) {
        this.channelActionsHeader = new LabelElement(context, this)
            .setText(this.channelRankChangeAdapter.noActionsMessage)
            .setColour(Colour.GREY);
        this.channelActionsList = null;

      } else {
        this.channelActionsHeader = new LabelElement(context, this)
            .setText(this.channelRankChangeAdapter.actionsHeaderMessage)
            .setSizingMode(SizingMode.FILL)
            .setMargin(new RectExtension(ZERO, ZERO, gui(4), ZERO))
            .cast();

        this.channelActionsList = new BlockElement(context, this)
            .setSizingMode(SizingMode.FILL)
            .setMargin(new RectExtension(ZERO, gui(6)))
            .cast();

        for (PublicChannelRankChange rankChange : channelRankChanges) {
          this.channelActionsList.addElement(new SideBySideElement(context, this)
              .setElementPadding(gui(4))
              .addElement(1, new LabelElement(context, this)
                  .setText(rankChange.channelName)
                  .setColour(rankChange.platform == Platform.YOUTUBE ? Colour.RED : Colour.DARK_PURPLE)
              ).addElement(1, new WrapperElement(context, this, // wrapper so text element size is flush to the text for a better tooltip experience
                  new LabelElement(context, this)
                      .setText(rankChange.error == null ? "SUCCESS" : "FAILURE")
                      .setColour(rankChange.error == null ? Colour.GREEN : Colour.RED)
                      .setTooltip(this.channelRankChangeAdapter.getTooltip(this.underlyingRank, rank, rankChange))
                      .setSizingMode(SizingMode.MINIMISE)
                      .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                  )
              )
          );
        }
      }
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      this.modal.setCloseText("Go back");
      this.modal.setSubmitText(toSentenceCase(this.getRevokeName()));

      super.addElement(this.titleLabel);
      super.addElement(this.statusLabel);
      super.addElement(this.issuedAtElement);
      super.addElement(this.expiresAtElement);
      super.addElement(this.messageElement);

      super.addElement(this.revokedAtElement);
      super.addElement(this.revokedMessageElement);

      super.addElement(this.revokeRankTextInputElement);

      super.addElement(this.rankErrorHeader);
      super.addElement(this.rankErrorLabel);
      super.addElement(this.channelActionsHeader);
      super.addElement(this.channelActionsList);
    }

    private Boolean onBack() {
      this.modal.setBody(new RankList(context, this.modal, this.user, this.adapters));
      return true;
    }

    private void onRevokeRank(Runnable onSuccess, Consumer<String> onError) {
      int userId = this.user.id;
      @Nullable String message = this.revokeRankTextInputElement.getText();
      this.endpointAdapter.revokeRank(userId, this.underlyingRank.name, message, result -> this.onRankUpdated(result, onSuccess), e -> this.onRevokeRankFailed(e, onError));
    }

    private void onRevokeRankFailed(Throwable error, Consumer<String> callback) {
      this.context.renderer.runSideEffect(() -> {
        String errorMessage = EndpointProxy.getApiErrorMessage(error);
        callback.accept(String.format("Failed to %s: %s", this.getRevokeName(), errorMessage));
      });
    }

    private void onRankUpdated(RankResult result, Runnable onSuccess) {
      this.context.renderer.runSideEffect(() -> {
        // the underlying rank is guaranteed to be defined, otherwise how could we have updated a rank that we don't know about?
        assert this.underlyingRank != null;
        this.modal.setBody(new RankDetails(this.context, this.modal, this.user, this.underlyingRank, result, this.adapters));
        onSuccess.run();
      });
    }

    private String getRevokeName() {
      if (this.underlyingRank.name == RankName.BAN) {
        return "unban";
      } else if (this.underlyingRank.name == RankName.TIMEOUT) {
        return "revoke timeout";
      } else if (this.underlyingRank.name == RankName.MUTE) {
        return "unmute";
      } else if (this.underlyingRank.name == RankName.MOD) {
        return "unmod";
      } else {
        return "revoke rank";
      }
    }
  }

  private static class RankCreate extends ContainerElement {
    private final ManageRanksModal modal;
    private final Adapters adapters;
    private final CreateAdapter createAdapter;
    private final EndpointAdapter endpointAdapter;
    private final PublicUser user;
    private final PublicRank rank;

    private final LabelElement titleLabel;
    private final TextInputElement createMessageInputElement;
    private final @Nullable IElement timeElements; // only for timeouts and mutes
    private final @Nullable CheckboxInputElement clearChatCheckbox;

    private @Nullable Float days = 0.0f;
    private @Nullable Float hours = 0.0f;
    private @Nullable Float minutes = 0.0f;

    public RankCreate(InteractiveContext context, ManageRanksModal parent, Adapters adapters, PublicUser user, PublicRank rank) {
      super(context, parent, LayoutMode.INLINE);

      this.modal = parent;
      this.adapters = adapters;
      this.modal.onValidate = this::onValidate;
      this.modal.onSubmit = this::onCreateRank;
      this.modal.onClose = this::onBack;

      this.createAdapter = adapters.createAdapter;
      this.endpointAdapter = adapters.endpointAdapter;
      this.user = user;
      this.rank = rank;

      this.titleLabel = new LabelElement(context, this)
          .setText(this.createAdapter.getTitle(this.rank))
          .setHorizontalAlignment(HorizontalAlignment.LEFT)
          .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(4)))
          .cast();

      this.createMessageInputElement = new TextInputElement(context, this)
          .setPlaceholder(this.createAdapter.getMessagePlaceholder(this.rank))
          .setTabIndex(1)
          .setSizingMode(SizingMode.FILL)
          .setPadding(new RectExtension(gui(4), gui(1)))
          .setMargin(new RectExtension(ZERO, gui(4)))
          .cast();

      if (this.createAdapter.allowExpiration(this.rank.name)) {
        this.timeElements = new SideBySideElement(context, this)
            .setElementPadding(gui(10))
            .addElement(1,
                new BlockElement(context, this)
                    .addElement(
                        new LabelElement(context, this)
                            .setText("Duration:")
                            .setOverflow(TextOverflow.TRUNCATE)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    ).addElement(
                        new LabelElement(context, this)
                            .setText(this.createAdapter.getExpirationSubtitle(this.rank))
                            .setColour(Colour.LTGREY)
                            .setFontScale(0.5f)
                            .setOverflow(TextOverflow.TRUNCATE)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    ).setPadding(new RectExtension(ZERO, ZERO, gui(1.5f), ZERO))
            ).addElement(0.5f,
                new TextInputElement(context, this)
                    .onTextChange(this::onDaysChange)
                    .setValidator(this::onValidateInput)
                    .setSuffix("d")
                    .setTabIndex(2)
            ).addElement(0.5f,
                new TextInputElement(context, this)
                    .onTextChange(this::onHoursChange)
                    .setValidator(this::onValidateInput)
                    .setSuffix("h")
                    .setTabIndex(3)
            ).addElement(0.5f,
                new TextInputElement(context, this)
                    .onTextChange(this::onMinutesChange)
                    .setValidator(this::onValidateInput)
                    .setSuffix("m")
                    .setTabIndex(4)
            ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
            );
      } else {
        this.timeElements = null;
      }

      if (this.createAdapter.showClearChatCheckbox(this.rank.name)) {
        this.clearChatCheckbox = new CheckboxInputElement(context, this)
            .setChecked(false)
            .setLabel("Clear all chat messages")
            .setTabIndex(5)
            .setMargin(new RectExtension(ZERO, gui(4)))
            .cast();
      } else {
        this.clearChatCheckbox = null;
      }
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      this.modal.setCloseText("Go back");
      this.modal.setSubmitText("Submit");

      super.addElement(this.titleLabel);

      super.addElement(this.createMessageInputElement);
      super.addElement(this.timeElements);
      super.addElement(this.clearChatCheckbox);
    }

    private Boolean onBack() {
      this.modal.setBody(new RankList(context, this.modal, this.user, this.adapters));
      return true;
    }

    private Boolean onValidate() {
      if (!this.createAdapter.allowExpiration(this.rank.name)) {
        return true;
      }

      // null implies invalid values, NOT blanks
      if (this.days == null || this.hours == null || this.minutes == null) {
        return false;
      }

      return this.createAdapter.validateExpirationTime(this.rank.name, this.getTotalSeconds());
    }

    private int getTotalSeconds() {
      return (int)(this.days * 24 * 3600 + this.hours * 3600 + this.minutes * 60);
    }

    private void onMinutesChange(String maybeMinutes) {
      this.minutes = this.tryGetValidTime(maybeMinutes);
    }

    private void onHoursChange(String maybeHours) {
      this.hours = this.tryGetValidTime(maybeHours);
    }

    private void onDaysChange(String maybeDays) {
      this.days = this.tryGetValidTime(maybeDays);
    }

    private boolean onValidateInput(String maybeTime) {
      return this.tryGetValidTime(maybeTime) != null;
    }

    private @Nullable Float tryGetValidTime(String maybeTime) {
      if (isNullOrEmpty(maybeTime)) {
        return 0.0f;
      }

      try {
        float time = Float.parseFloat(maybeTime);
        return time < 0 ? null : time;
      } catch (Exception ignored) {
        return null;
      }
    }

    private void onCreateRank(Runnable onSuccess, Consumer<String> onError) {
      // always valid, since the submit button is disabled if it's invalid
      @Nullable Integer durationSeconds = this.createAdapter.allowExpiration(this.rank.name) ? this.getTotalSeconds() : null;
      int userId = this.user.id;
      @Nullable String message = this.createMessageInputElement.getText();
      this.endpointAdapter.createRank(userId, this.rank.name, message, durationSeconds, r -> this.onRankCreated(r, onSuccess), e -> this.onCreateRankFailed(e, onError));
    }

    private void onCreateRankFailed(Throwable error, Consumer<String> callback) {
      String action;
      if (this.rank.name == RankName.BAN) {
        action = "ban user";
      } else if (this.rank.name == RankName.TIMEOUT) {
        action = "timeout user";
      } else if (this.rank.name == RankName.MUTE) {
        action = "mute user";
      } else if (this.rank.name == RankName.MOD) {
        action = "mod user";
      } else {
        action = "add rank";
      }

      this.context.renderer.runSideEffect(() -> {
        String errorMessage = EndpointProxy.getApiErrorMessage(error);
        callback.accept(String.format("Failed to %s user: %s", action, errorMessage));
      });
    }

    private void onRankCreated(RankResult result, Runnable callback) {
      this.context.renderer.runSideEffect(() -> {
        if (this.clearChatCheckbox != null && this.clearChatCheckbox.getChecked()) {
          super.context.minecraftChatService.clearChatMessagesByUser(this.user);
        }
        this.modal.setBody(new RankDetails(this.context, this.modal, this.user, this.rank, result, this.adapters));
        callback.run();
      });
    }
  }
}
