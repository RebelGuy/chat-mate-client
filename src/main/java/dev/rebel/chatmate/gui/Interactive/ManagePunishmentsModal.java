package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.TableElement.Column;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.models.api.punishment.GetPunishmentsResponse;
import dev.rebel.chatmate.models.api.punishment.GetPunishmentsResponse.GetPunishmentsResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.proxy.PunishmentEndpointProxy;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ManagePunishmentsModal extends ModalElement {
  private final PublicUser user;
  private final PunishmentEndpointProxy punishmentEndpointProxy;

  private Mode mode;

  public ManagePunishmentsModal(InteractiveContext context, InteractiveScreen parent, PublicUser user, PunishmentEndpointProxy punishmentEndpointProxy) {
    super(context, parent);
    this.name = "ManagePunishmentsModal";
    this.user = user;
    this.punishmentEndpointProxy = punishmentEndpointProxy;

    this.mode = Mode.LIST;

    super.setTitle("Manage Punishments for " + user.userInfo.channelName);
  }

  @Override
  public void onCreate() {
    super.onCreate();

    this.loadPunishments();
  }

  @Override
  protected @Nullable Boolean validate() { return null; }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) { }

  private void loadPunishments() {
    super.setBody(new LabelElement(context, this)
        .setText("Loading punishments...")
        .setAlignment(TextAlignment.CENTRE)
        .setOverflow(TextOverflow.SPLIT));
    this.punishmentEndpointProxy.getPunishmentsAsync(this.user.id, null, this::onPunishmentsLoaded, this::onPunishmentsLoadError);
  }

  private void onPunishmentsLoadError(Throwable error) {
    super.setBody(new LabelElement(this.context, this)
        .setText("Failed to load punishments: " + EndpointProxy.getApiErrorMessage(error))
        .setAlignment(TextAlignment.CENTRE)
        .setOverflow(TextOverflow.SPLIT)
        .setColour(Colour.RED));
  }

  private void onPunishmentsLoaded(GetPunishmentsResponseData getPunishmentsResponseData) {
    super.setBody(
        new TableElement<PublicPunishment>(
            this.context,
            this,
            Collections.list(getPunishmentsResponseData.punishments),
            Collections.list(
                new Column("Type", 1, true),
                new Column("Message", 3, false),
                new Column("Active", 1, true)),
            this::getPunishmentRow
        ).setMinHeight(gui(100))
        .setSizingMode(SizingMode.FILL)
    );
  }

  private List<IElement> getPunishmentRow(PublicPunishment punishment) {
    return Collections.list(
        new LabelElement(this.context, this).setText(punishment.type.toString()).setAlignment(TextAlignment.CENTRE).setOverflow(TextOverflow.TRUNCATE),
        new LabelElement(this.context, this).setText(punishment.message).setAlignment(TextAlignment.LEFT).setOverflow(TextOverflow.TRUNCATE),
        new LabelElement(this.context, this).setText(punishment.isActive.toString()).setAlignment(TextAlignment.CENTRE).setOverflow(TextOverflow.TRUNCATE)
    );
  }

  private enum Mode {
    LIST, CREATE, DETAILS
  }
}
