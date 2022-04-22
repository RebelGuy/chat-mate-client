package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.TableElement.Column;
import dev.rebel.chatmate.gui.hud.Colour;
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
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.TextHelpers.*;

public class ManagePunishmentsModal extends ModalElement {
  private final static float FONT_SCALE = 0.75f;

  private final PublicUser user;
  private final PunishmentEndpointProxy punishmentEndpointProxy;

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
  protected @Nullable Boolean validate() { return null; }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) { }

  private class PunishmentList extends ContainerElement {
    private final LabelElement titleLabel;
    private final ButtonElement createNewPunishmentButton;
    private final ElementReference listReference;

    public PunishmentList(InteractiveContext context, IElement parent) {
      super(context, parent, LayoutMode.INLINE);

      this.titleLabel = new LabelElement(context, this).setText("All Punishments");
      this.createNewPunishmentButton = new ButtonElement(context, this)
          .setText("Create new")
          .setOnClick(this::onCreateNewPunishment)
          .setHorizontalAlignment(HorizontalAlignment.RIGHT)
          .cast();
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

      super.addElement(this.titleLabel);
      super.addElement(this.createNewPunishmentButton);
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

    private void onCreateNewPunishment() {
      ManagePunishmentsModal.super.setBody(new PunishmentCreate(this.context, ManagePunishmentsModal.this));
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
    private final @Nullable ButtonElement revokePunishmentButton;

    private LabelElement errorLabel;

    public PunishmentDetails(InteractiveContext context, IElement parent, PublicPunishment punishment) {
      super(context, parent, LayoutMode.INLINE);

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
        this.revokePunishmentTextInputElement= new TextInputElement(context, this)
            .setPlaceholder(toSentenceCase(this.getRevokeName()) + " reason")
            .setSizingMode(SizingMode.FILL)
            .setPadding(new RectExtension(gui(4), ZERO))
            .cast();
        this.revokePunishmentButton = new ButtonElement(context, this)
            .setText(toSentenceCase(this.getRevokeName()))
            .setOnClick(this::onRevokePunishment)
            .setPadding(new RectExtension(ZERO, gui(4)))
            .cast();

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
        this.revokePunishmentButton = null;
      }
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      super.addElement(this.titleLabel);
      super.addElement(this.statusLabel);
      super.addElement(this.issuedAtElement);
      super.addElement(this.messageElement);

      super.addElement(this.revokedAtElement);
      super.addElement(this.revokedMessageElement);

      super.addElement(this.revokePunishmentTextInputElement);
      super.addElement(this.revokePunishmentButton);
    }

    private void onRevokePunishment() {
      super.removeElement(this.errorLabel);
      this.revokePunishmentButton.setEnabled(this, false);

      int userId = ManagePunishmentsModal.this.user.id;
      @Nullable String message = this.revokePunishmentTextInputElement.getText();
      if (this.punishment.type == PunishmentType.BAN) {
        UnbanUserRequest request = new UnbanUserRequest(userId, message);
        ManagePunishmentsModal.this.punishmentEndpointProxy.unbanUserAsync(request, this::onUnbanUser, this::onRevokePunishmentFailed);
      } else {
        throw new RuntimeException("Cannot revoke invalid punishment type " + this.punishment.type);
      }
    }

    private void onRevokePunishmentFailed(Throwable error) {
      this.revokePunishmentButton.setEnabled(this, true);

      String errorMessage = EndpointProxy.getApiErrorMessage(error);
      this.errorLabel = new LabelElement(this.context, this)
          .setText(String.format("Failed to %s: %s", this.getRevokeName(), errorMessage))
          .setOverflow(TextOverflow.SPLIT)
          .setColour(Colour.RED)
          .setSizingMode(SizingMode.FILL)
          .setFontScale(0.75f)
          .setAlignment(TextAlignment.CENTRE)
          .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(4)))
          .cast();
      super.addElement(this.errorLabel);
    }

    private void onUnbanUser(UnbanUserResponseData unbanUserResponseData) {
      this.onPunishmentUpdated(unbanUserResponseData.updatedPunishment);
    }

    private void onPunishmentUpdated(PublicPunishment punishment) {
      ManagePunishmentsModal.super.setBody(new PunishmentDetails(this.context, ManagePunishmentsModal.this, punishment));
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
    public PunishmentCreate(InteractiveContext context, IElement parent) {
      super(context, parent, LayoutMode.INLINE);
    }

    @Override
    public void onInitialise() {
      super.onInitialise();
    }
  }
}

