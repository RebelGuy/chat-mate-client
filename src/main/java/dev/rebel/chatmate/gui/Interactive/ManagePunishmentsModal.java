package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.DropdownMenu.Anchor;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.Interactive.TableElement.Column;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.models.api.punishment.BanUserRequest;
import dev.rebel.chatmate.models.api.punishment.BanUserResponse.BanUserResponseData;
import dev.rebel.chatmate.models.api.punishment.GetPunishmentsResponse.GetPunishmentsResponseData;
import dev.rebel.chatmate.models.api.punishment.UnbanUserRequest;
import dev.rebel.chatmate.models.api.punishment.UnbanUserResponse.UnbanUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment.PunishmentType;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.proxy.PunishmentEndpointProxy;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.rebel.chatmate.services.util.TextHelpers.*;

public class ManagePunishmentsModal extends ModalElement {
  private final static float FONT_SCALE = 0.75f;

  private final PublicUser user;
  private final PunishmentEndpointProxy punishmentEndpointProxy;

  /** onSuccess and onError callback. */
  private @Nullable BiConsumer<Runnable, Consumer<String>> onSubmit;
  /** If it returns true, the default close behaviour is suppressed. */
  private @Nullable Supplier<Boolean> onClose;
  /** If it returns true, the submit button can be pressed. If it returns false, the submit button cannot be pressed.
   * If it returns null, the submit button is not shown. */
  private @Nullable Supplier<Boolean> onValidate;

  public ManagePunishmentsModal(InteractiveContext context, InteractiveScreen parent, PublicUser user, PunishmentEndpointProxy punishmentEndpointProxy) {
    super(context, parent);
    this.name = "ManagePunishmentsModal";
    super.width = gui(300);
    this.user = user;
    this.punishmentEndpointProxy = punishmentEndpointProxy;
  }

  @Override
  public void onInitialise() {
    super.onInitialise();

    super.setTitle("Manage Punishments for " + user.userInfo.channelName);
    super.setBody(new PunishmentList(context, this));
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

  private class PunishmentList extends ContainerElement {
    private final LabelElement titleLabel;
    private final ButtonElement createNewPunishmentButton;
    private final DropdownMenu createNewPunishmentDropdown;
    private final ElementReference listReference;

    public PunishmentList(InteractiveContext context, IElement parent) {
      super(context, parent, LayoutMode.INLINE);

      ManagePunishmentsModal.this.onValidate = null;
      ManagePunishmentsModal.this.onSubmit = null;
      ManagePunishmentsModal.this.onClose = null;

      this.titleLabel = new LabelElement(context, this).setText("All Punishments");

      this.createNewPunishmentButton = new ButtonElement(context, this)
          .setText("Create new")
          .setHorizontalAlignment(HorizontalAlignment.RIGHT)
          .cast();
      this.createNewPunishmentDropdown = new DropdownMenu(context, this.createNewPunishmentButton)
          .addOption("Mute", () -> this.onCreateNewPunishment(PunishmentType.MUTE))
          .addOption("Timeout", () -> this.onCreateNewPunishment(PunishmentType.TIMEOUT))
          .addOption("Ban", () -> this.onCreateNewPunishment(PunishmentType.BAN))
          .setAnchor(Anchor.LEFT)
          .setBorder(new RectExtension(gui(1)))
          .setSizingMode(SizingMode.FILL)
          .cast();
      this.createNewPunishmentButton.setOnClick(this.createNewPunishmentDropdown::toggleExpanded);

      this.listReference = new ElementReference(context, this).setUnderlyingElement(
          new LabelElement(context, this)
              .setText("Loading punishments...")
              .setOverflow(TextOverflow.SPLIT)
              .setHorizontalAlignment(HorizontalAlignment.CENTRE)
      );
      ManagePunishmentsModal.this.punishmentEndpointProxy.getPunishmentsAsync(ManagePunishmentsModal.this.user.id, null, this::onPunishmentsLoaded, this::onPunishmentsLoadError);
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      ManagePunishmentsModal.this.setCloseText("Close");

      super.addElement(this.titleLabel);
      super.addElement(this.createNewPunishmentButton);
      super.addElement(this.createNewPunishmentDropdown);
      super.addElement(this.listReference);
    }

    private void onPunishmentsLoadError(Throwable error) {
      this.listReference.setUnderlyingElement(
          new LabelElement(this.context, this)
              .setText("Failed to load punishments: " + EndpointProxy.getApiErrorMessage(error))
              .setOverflow(TextOverflow.SPLIT)
              .setColour(Colour.RED)
              .setHorizontalAlignment(HorizontalAlignment.CENTRE)
      );
    }

    private void onPunishmentsLoaded(GetPunishmentsResponseData getPunishmentsResponseData) {
      IElement element;
      if (getPunishmentsResponseData.punishments.length == 0) {
        element = new LabelElement(this.context, this)
            .setText("No punishments to show.")
            .setOverflow(TextOverflow.SPLIT)
            .setColour(Colour.RED)
            .setHorizontalAlignment(HorizontalAlignment.CENTRE);
      } else {
        element = new TableElement<PublicPunishment>(
            this.context,
            this,
            Collections.list(getPunishmentsResponseData.punishments),
            Collections.list(
                new Column("Date", FONT_SCALE, 2, true),
                new Column("Type", FONT_SCALE, 1.5f, true),
                new Column("Message", FONT_SCALE, 3, false),
                new Column("Perm", FONT_SCALE, 1, true),
                new Column("Active", FONT_SCALE, 1, true)),
            this::getPunishmentRow
        ).setMinHeight(gui(50))
            .setOnClickItem(this::onShowDetails)
            .setSizingMode(SizingMode.FILL)
            .setPadding(new RectExtension(ZERO, gui(4)));
      }

      this.listReference.setUnderlyingElement(element);
    }

    private List<IElement> getPunishmentRow(PublicPunishment punishment) {
      String dateStr = dateToDayAccuracy(punishment.issuedAt);
      return Collections.list(
          new LabelElement(this.context, this).setText(dateStr).setOverflow(TextOverflow.TRUNCATE).setFontScale(FONT_SCALE).setHorizontalAlignment(HorizontalAlignment.LEFT),
          new LabelElement(this.context, this).setText(toSentenceCase(punishment.type.toString())).setOverflow(TextOverflow.TRUNCATE).setFontScale(FONT_SCALE).setHorizontalAlignment(HorizontalAlignment.LEFT),
          new LabelElement(this.context, this).setText(punishment.message).setOverflow(TextOverflow.SPLIT).setFontScale(FONT_SCALE).setHorizontalAlignment(HorizontalAlignment.LEFT).setSizingMode(SizingMode.FILL),
          new LabelElement(this.context, this).setText(punishment.expirationTime != null ? "Yes" : "No").setOverflow(TextOverflow.TRUNCATE).setFontScale(FONT_SCALE),
          new LabelElement(this.context, this).setText(punishment.isActive ? "Yes" : "No").setOverflow(TextOverflow.TRUNCATE).setFontScale(FONT_SCALE)
      );
    }

    private void onShowDetails(PublicPunishment punishment) {
      ManagePunishmentsModal.super.setBody(new PunishmentDetails(this.context, ManagePunishmentsModal.this, punishment));
    }

    private void onCreateNewPunishment(PunishmentType type) {
      ManagePunishmentsModal.super.setBody(new PunishmentCreate(this.context, ManagePunishmentsModal.this, type));
    }
  }

  private class PunishmentDetails extends ContainerElement {
    private final PublicPunishment punishment;

    private final LabelElement titleLabel;
    private final @Nullable LabelElement statusLabel;
    private final SideBySideElement issuedAtElement;
    private final SideBySideElement messageElement;
    private final @Nullable SideBySideElement revokedAtElement;
    private final @Nullable SideBySideElement revokedMessageElement;
    private final @Nullable TextInputElement revokePunishmentTextInputElement;

    public PunishmentDetails(InteractiveContext context, IElement parent, PublicPunishment punishment) {
      super(context, parent, LayoutMode.INLINE);
      ManagePunishmentsModal.this.onValidate = null;
      ManagePunishmentsModal.this.onSubmit = null;
      ManagePunishmentsModal.this.onClose = this::onBack;

      this.name = "PunishmentDetails";
      this.punishment = punishment;

      String status = null;
      if (punishment.revokedAt != null) {
        status = "REVOKED";
      } else if (punishment.expirationTime != null && punishment.expirationTime <= new Date().getTime()) {
        status = "EXPIRED";
      }
      this.titleLabel = new LabelElement(context, this)
          .setText(String.format("Details for %s", punishment.type.toString().toLowerCase()))
          .setHorizontalAlignment(HorizontalAlignment.LEFT)
          .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(4)))
          .cast();
      this.statusLabel = status == null ? null : new LabelElement(context, this)
          .setText(status)
          .setColour(Colour.RED)
          .setHorizontalAlignment(HorizontalAlignment.RIGHT)
          .cast();

      this.issuedAtElement = new SideBySideElement(context, this)
          .setElementPadding(gui(4))
          .addElement(1, new LabelElement(context, this)
              .setText("Issued at:"))
          .addElement(2, new LabelElement(context, this)
              .setText(dateToSecondAccuracy(punishment.issuedAt))
          ).cast();
      this.messageElement = new SideBySideElement(context, this)
          .setElementPadding(gui(4))
          .addElement(1, new LabelElement(context, this)
              .setText("Message:"))
          .addElement(2, new LabelElement(context, this)
              .setText(nonNull(punishment.message, "n/a"))
              .setOverflow(TextOverflow.SPLIT)
          ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(8)))
          .cast();

      if (punishment.revokedAt == null) {
        this.revokedAtElement = null;
        this.revokedMessageElement = null;
        this.revokePunishmentTextInputElement = new TextInputElement(context, this)
            .setPlaceholder(toSentenceCase(this.getRevokeName()) + " reason")
            .setSizingMode(SizingMode.FILL)
            .setPadding(new RectExtension(gui(4), gui(1)))
            .setMargin(new RectExtension(ZERO, gui(4)))
            .cast();
        ManagePunishmentsModal.this.onValidate = () -> true; // nothing to validate
        ManagePunishmentsModal.this.onSubmit = this::onRevokePunishment;

      } else {
        this.revokedAtElement = new SideBySideElement(context, this)
            .setElementPadding(gui(4))
            .addElement(1, new LabelElement(context, this)
                .setText("Revoked at:"))
            .addElement(2, new LabelElement(context, this)
                .setText(dateToSecondAccuracy(punishment.revokedAt))
            ).cast();
        this.revokedMessageElement = new SideBySideElement(context, this)
            .setElementPadding(gui(4))
            .addElement(1, new LabelElement(context, this)
                .setText("Revoke message:"))
            .addElement(2, new LabelElement(context, this)
                .setText(nonNull(punishment.revokeMessage, "n/a"))
                .setOverflow(TextOverflow.SPLIT)
            ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(8)))
            .cast();
        this.revokePunishmentTextInputElement = null;
      }
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      ManagePunishmentsModal.this.setCloseText("Go back");
      ManagePunishmentsModal.this.setSubmitText(toSentenceCase(this.getRevokeName()));

      super.addElement(this.titleLabel);
      super.addElement(this.statusLabel);
      super.addElement(this.issuedAtElement);
      super.addElement(this.messageElement);

      super.addElement(this.revokedAtElement);
      super.addElement(this.revokedMessageElement);

      super.addElement(this.revokePunishmentTextInputElement);
    }

    private Boolean onBack() {
      ManagePunishmentsModal.this.setBody(new PunishmentList(context, parent));
      return true;
    }

    private void onRevokePunishment(Runnable onSuccess, Consumer<String> onError) {
      int userId = ManagePunishmentsModal.this.user.id;
      @Nullable String message = this.revokePunishmentTextInputElement.getText();
      if (this.punishment.type == PunishmentType.BAN) {
        UnbanUserRequest request = new UnbanUserRequest(userId, message);
        ManagePunishmentsModal.this.punishmentEndpointProxy.unbanUserAsync(request, r -> this.onUnbanUser(r, onSuccess), r -> this.onRevokePunishmentFailed(r, onError));
      } else {
        throw new RuntimeException("Cannot revoke invalid punishment type " + this.punishment.type);
      }
    }

    private void onRevokePunishmentFailed(Throwable error, Consumer<String> callback) {
      String errorMessage = EndpointProxy.getApiErrorMessage(error);
      callback.accept(String.format("Failed to %s: %s", this.getRevokeName(), errorMessage));
    }

    private void onUnbanUser(UnbanUserResponseData unbanUserResponseData, Runnable callback) {
      this.onPunishmentUpdated(unbanUserResponseData.updatedPunishment, callback);
    }

    private void onPunishmentUpdated(PublicPunishment punishment, Runnable callback) {
      ManagePunishmentsModal.super.setBody(new PunishmentDetails(this.context, ManagePunishmentsModal.this, punishment));
      callback.run();
    }

    private String getRevokeName() {
      if (this.punishment.type == PunishmentType.BAN) {
        return "unban";
      } else if (this.punishment.type == PunishmentType.TIMEOUT) {
        return "revoke timeout";
      } else if (this.punishment.type == PunishmentType.MUTE) {
        return "unmute";
      } else {
        throw new RuntimeException("Invalid punishment type " + this.punishment.type);
      }
    }
  }

  private class PunishmentCreate extends ContainerElement {
    private final static int MIN_DURATION_TIMEOUT_SECONDS = 60 * 5; // this is the minimum masterchat/youtube timeout period

    private final PunishmentType type;

    private final LabelElement titleLabel;
    private final TextInputElement punishmentReasonInputElement;
    private final IElement timeElements;

    private @Nullable Float days = 0.0f;
    private @Nullable Float hours = 0.0f;
    private @Nullable Float minutes = 0.0f;

    public PunishmentCreate(InteractiveContext context, IElement parent, PunishmentType type) {
      super(context, parent, LayoutMode.INLINE);

      ManagePunishmentsModal.this.onValidate = this::onValidate;
      ManagePunishmentsModal.this.onSubmit = this::onCreatePunishment;
      ManagePunishmentsModal.this.onClose = this::onBack;

      this.type = type;

      this.titleLabel = new LabelElement(context, this)
          .setText(String.format("Create new %s", this.punishmentType()))
          .setHorizontalAlignment(HorizontalAlignment.LEFT)
          .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(4)))
          .cast();

      this.punishmentReasonInputElement = new TextInputElement(context, this)
          .setPlaceholder(toSentenceCase(type + " reason"))
          .setTabIndex(1)
          .setSizingMode(SizingMode.FILL)
          .setPadding(new RectExtension(gui(4), gui(1)))
          .setMargin(new RectExtension(ZERO, gui(4)))
          .cast();

      this.timeElements = new SideBySideElement(context, this)
          .setElementPadding(gui(10))
          .addElement(1,
              new ListElement(context, this)
                  .addElement(
                      new LabelElement(context, this)
                          .setText("Duration:")
                          .setOverflow(TextOverflow.TRUNCATE)
                          .setVerticalAlignment(VerticalAlignment.MIDDLE)
                  ).addElement(
                      new LabelElement(context, this)
                          .setText(String.format("(leave blank for permanent %s)", this.punishmentType()))
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
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      ManagePunishmentsModal.this.setCloseText("Go back");
      ManagePunishmentsModal.this.setSubmitText("Finalise " + this.punishmentType());

      super.addElement(this.titleLabel);

      super.addElement(this.punishmentReasonInputElement);
      super.addElement(this.timeElements);
    }

    private Boolean onBack() {
      ManagePunishmentsModal.this.setBody(new PunishmentList(context, parent));
      return true;
    }

    private Boolean onValidate() {
      if (this.days == null || this.hours == null || this.minutes == null) {
        return false;
      }

      int totalSeconds = this.getTotalSeconds();
      if (totalSeconds == 0) {
        return true;
      } else if (this.type == PunishmentType.TIMEOUT) {
        return totalSeconds >= MIN_DURATION_TIMEOUT_SECONDS;
      } else {
        return true;
      }
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

    private void onCreatePunishment(Runnable onSuccess, Consumer<String> onError) {
      int userId = ManagePunishmentsModal.this.user.id;
      @Nullable String message = this.punishmentReasonInputElement.getText();
      int durationSeconds = this.getTotalSeconds(); // if zero, is permanent
      if (this.type == PunishmentType.BAN) {
        BanUserRequest request = new BanUserRequest(userId, message);
        ManagePunishmentsModal.this.punishmentEndpointProxy.banUserAsync(request, r -> this.onBanUser(r, onSuccess), r -> this.onCreatePunishmentFailed(r, onError));
      } else {
        throw new RuntimeException("Cannot create invalid punishment type " + this.punishmentType());
      }
    }

    private void onCreatePunishmentFailed(Throwable error, Consumer<String> callback) {
      String errorMessage = EndpointProxy.getApiErrorMessage(error);
      callback.accept(String.format("Failed to %s user: %s", this.punishmentType(), errorMessage));
    }

    private void onBanUser(BanUserResponseData banUserResponseData, Runnable callback) {
      this.onPunishmentCreated(banUserResponseData.newPunishment, callback);
    }

//    private void onTimeoutUser(TimeoutUserResponseData timeoutUserResponseData) {
//
//    }

    private void onPunishmentCreated(PublicPunishment punishment, Runnable callback) {
      callback.run();
      ManagePunishmentsModal.super.setBody(new PunishmentDetails(this.context, ManagePunishmentsModal.this, punishment));
    }

    private String punishmentType() {
      return this.type.toString().toLowerCase();
    }
  }
}
