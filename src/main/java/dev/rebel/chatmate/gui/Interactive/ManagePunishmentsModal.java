package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.TableElement.Column;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.models.api.punishment.GetPunishmentsResponse.GetPunishmentsResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.proxy.PunishmentEndpointProxy;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.TextHelpers.toSentenceCase;

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
      this.listReference.setUnderlyingElement(
          new TableElement<PublicPunishment>(
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
          ).setMinHeight(gui(100))
              .setOnClickItem(this::onShowDetails)
              .setSizingMode(SizingMode.FILL)
              .setPadding(new RectExtension(ZERO, gui(4)))
      );
    }

    private List<IElement> getPunishmentRow(PublicPunishment punishment) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      String dateStr = dateFormat.format(new Date(punishment.issuedAt));
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
    public PunishmentDetails(InteractiveContext context, IElement parent, PublicPunishment punishment) {
      super(context, parent, LayoutMode.INLINE);
    }

    @Override
    public void onInitialise() {
      super.onInitialise();
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

