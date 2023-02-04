package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.DonationRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.LinkDonationRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream.LivestreamStatus;
import dev.rebel.chatmate.api.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.proxy.UserEndpointProxy;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.Objects.casted;
import static dev.rebel.chatmate.util.TextHelpers.dateToDayAccuracy;

public class DonationsSectionElement extends ContainerElement implements ISectionElement {
  private final @Nullable Integer highlightDonation;
  private final ApiRequestService apiRequestService;
  private final Consumer<Boolean> _onApiServiceActive = this::onApiServiceActive;

  private final CheckboxInputElement unlinkedDonationsCheckbox;
  private final CheckboxInputElement currentLivestreamCheckbox;
  private final LoadingSpinnerElement loadingSpinner;
  private final LabelElement errorLabel;
  private final DonationsTable donationsTable;

  public DonationsSectionElement(InteractiveContext context,
                                 IElement parent,
                                 @Nullable DonationRoute route,
                                 StatusService statusService,
                                 ApiRequestService apiRequestService,
                                 UserEndpointProxy userEndpointProxy,
                                 MessageService messageService) {
    super(context, parent, LayoutMode.BLOCK);
    super.setSizingMode(SizingMode.FILL);

    this.apiRequestService = apiRequestService;
    this.apiRequestService.onActive(this._onApiServiceActive);

    this.unlinkedDonationsCheckbox = SharedElements.CHECKBOX_LIGHT.create(context, this)
        .setChecked(true)
        .onCheckedChanged(this::onFilterChanged)
        .setLabel("Show only unlinked donations")
        .setScale(0.75f);
    boolean isActiveLivestream = statusService.getLivestreamStatus() != null && statusService.getLivestreamStatus().livestream.status == LivestreamStatus.Live;
    this.currentLivestreamCheckbox = SharedElements.CHECKBOX_LIGHT.create(context, this)
        .setChecked(isActiveLivestream)
        .onCheckedChanged(this::onFilterChanged)
        .setLabel("Show only donations from the current livestream")
        .setScale(0.75f)
        .setEnabled(this, isActiveLivestream)
        .setMargin(new RectExtension(ZERO, ZERO, gui(2), gui(8)))
        .cast();

    this.loadingSpinner = new LoadingSpinnerElement(this.context, this)
        .setHorizontalAlignment(HorizontalAlignment.CENTRE)
        .setMaxContentWidth(gui(16))
        .setMargin(new RectExtension(gui(8)))
        .setVisible(false)
        .setSizingMode(SizingMode.FILL)
        .cast();

    this.donationsTable = new DonationsTable(context, this, this::onError, statusService.getLivestreamStatus(), userEndpointProxy, messageService)
        .setVisible(false)
        .setMargin(new RectExtension(ZERO, gui(4)))
        .cast();

    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this);

    this.highlightDonation = casted(LinkDonationRoute.class, route, d -> d.donation.id);

    // todo: save checkbox values in config
    super.addElement(this.unlinkedDonationsCheckbox);
    super.addElement(this.currentLivestreamCheckbox);
    super.addElement(this.loadingSpinner);
    super.addElement(this.donationsTable);
    super.addElement(this.errorLabel);
  }

  private void onGetDonations(List<PublicDonation> donations) {
    super.context.renderer.runSideEffect(() -> {
      this.loadingSpinner.setVisible(false);
      this.donationsTable.setVisible(true);
      this.donationsTable.setDonations(donations);
    });
  }

  private void onApiServiceActive(Boolean isActive) {
    if (isActive) {
      this.errorLabel.setVisible(false);
    }
  }

  private void onError(Throwable error) {
    super.context.renderer.runSideEffect(() -> {
      this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error));
      this.errorLabel.setVisible(true);
      this.loadingSpinner.setVisible(false);
    });
  }

  private void onFilterChanged(boolean x) {
    this.donationsTable.setFilter(this.currentLivestreamCheckbox.getChecked(), this.unlinkedDonationsCheckbox.getChecked());
  }

  @Override
  public void onShow() {
    super.context.donationApiStore.loadDonations(this::onGetDonations, this::onError, false);
    this.loadingSpinner.setVisible(true);
  }

  @Override
  public void onHide() {
    this.errorLabel.setVisible(false);
    this.donationsTable.setVisible(false);
    this.loadingSpinner.setVisible(false);
  }

  private static class DonationsTable extends ContainerElement {
    private final Consumer<Throwable> onError;
    private final @Nullable PublicLivestreamStatus livestreamStatus;
    private final UserEndpointProxy userEndpointProxy;
    private final MessageService messageService;

    private @Nullable List<PublicDonation> donations = null;
    private Map<PublicDonation, PublicUser> editingDonations = new HashMap<>();
    private Map<PublicDonation, IElement> editingElements = new HashMap<>();
    private boolean showCurrentLivestreamOnly = true;
    private boolean showUnlinkedOnly = true;

    private final TableElement<PublicDonation> table;
    private final LabelElement nothingToShowLabel;

    public DonationsTable(InteractiveContext context,
                          IElement parent,
                          Consumer<Throwable> onError,
                          @Nullable PublicLivestreamStatus livestreamStatus,
                          UserEndpointProxy userEndpointProxy,
                          MessageService messageService) {
      super(context, parent, LayoutMode.BLOCK);
      this.onError = onError;
      this.livestreamStatus = livestreamStatus;
      this.userEndpointProxy = userEndpointProxy;
      this.messageService = messageService;

      List<TableElement.Column> columns = Collections.list(
          new TableElement.Column("Date", 0.75f, 1, true),
          new TableElement.Column("User", 0.75f, 2, false),
          new TableElement.Column("Amount", 0.75f, 1, true),
          new TableElement.Column("Message", 0.75f, 3, false),
          new TableElement.Column("", 0.75f, 0.75f, true)
      );
      this.table = new TableElement<>(context, this, new ArrayList<>(), columns, don -> this.getRow(don, false));

      this.nothingToShowLabel = new LabelElement(this.context, this)
          .setText("Nothing to show")
          .setFontScale(0.75f)
          .setAlignment(TextAlignment.CENTRE)
          .setSizingMode(SizingMode.FILL)
          .setVisible(false)
          .setMargin(new RectExtension(ZERO, gui(8)))
          .cast();

      super.addElement(this.table);
      super.addElement(this.nothingToShowLabel);
    }

    // the complexity of this function escalated very quickly and I apologise
    private List<IElement> getRow(PublicDonation donation, boolean isUpdating) {
      String dateStr = dateToDayAccuracy(donation.time);

      IElement actionElement;
      Dim iconHeight = super.context.fontEngine.FONT_HEIGHT_DIM;
      if (isUpdating) {
        actionElement = new LoadingSpinnerElement(super.context, this).setTargetHeight(iconHeight).setHorizontalAlignment(HorizontalAlignment.CENTRE).setVerticalAlignment(VerticalAlignment.MIDDLE);
      } else if (this.editingDonations.containsKey(donation)) {
        // it is invalid to try to link to a null user
        boolean valid = donation.linkedUser != null || donation.linkedUser == null && this.editingDonations.get(donation) != null;
        IconButtonElement confirmIconButton = new IconButtonElement(super.context, this)
            .setImage(Asset.GUI_TICK_ICON)
            .setEnabledColour(Colour.GREEN)
            .setEnabled(this, valid)
            .setOnClick(() -> this.onConfirmLinkOrUnlink(donation))
            .setTargetHeight(iconHeight)
            .setBorder(new RectExtension(ZERO))
            .setPadding(new RectExtension(ZERO))
            .setMargin(new RectExtension(ZERO, gui(2), ZERO, ZERO))
            .cast();
        confirmIconButton.image.setPadding(new RectExtension(ZERO));
        IconButtonElement cancelIconButton = new IconButtonElement(super.context, this)
            .setImage(Asset.GUI_CLEAR_ICON)
            .setEnabledColour(Colour.RED)
            .setOnClick(() -> this.onCancelLinkOrUnlink(donation))
            .setTargetHeight(iconHeight)
            .setBorder(new RectExtension(ZERO))
            .setPadding(new RectExtension(ZERO))
            .cast();
        cancelIconButton.image.setPadding(new RectExtension(ZERO));
        actionElement = new InlineElement(context, this)
            .addElement(confirmIconButton)
            .addElement(cancelIconButton);
      } else {
        Texture icon = donation.linkedUser == null ? Asset.GUI_LINK_ICON : Asset.GUI_BIN_ICON;
        Runnable onClick = () -> this.onLinkOrUnlink(donation);
        boolean disableButton = Collections.any(this.getDonationsWithLinkIdentifier(donation), d -> this.editingDonations.containsKey(d));
        actionElement = new IconButtonElement(super.context, this)
            .setImage(icon)
            .setEnabled(this, !disableButton)
            .setOnClick(onClick)
            .setTargetHeight(iconHeight)
            .setBorder(new RectExtension(ZERO))
            .setPadding(new RectExtension(ZERO))
            .setTooltip(disableButton ? null : donation.linkedUser == null ? "Link donation to a user" : "Unlink current user from donation");
        casted(IconButtonElement.class, actionElement, el -> el.image.setPadding(new RectExtension(ZERO)));
      }

      IElement userNameElement;
      if (this.editingDonations.containsKey(donation) && donation.linkedUser == null) {
        if (this.editingElements.containsKey(donation)) {
          // this saves the state of the element over multiple calls to `getRow`
          userNameElement = this.editingElements.get(donation);
        } else {
          Consumer<PublicUser> onUserSelected = newUser -> {
            this.editingDonations.put(donation, newUser);
            this.updateDonation(donation);
          };
          userNameElement = new UserPickerElement(super.context, this, donation.linkedUser, onUserSelected, this.userEndpointProxy, this.messageService)
              .setFontScale(0.75f);
          this.editingElements.put(donation, userNameElement);
        }
      } else {

        // if another donation with the same linkIdentifier is being edited, display that donation's user
        PublicUser userToShow = donation.linkedUser;
        if (!this.editingDonations.containsKey(donation)) {
          for (PublicDonation editingDonation : this.editingDonations.keySet()) {
            if (Objects.equals(editingDonation.linkIdentifier, donation.linkIdentifier)) {
              userToShow = this.editingDonations.get(editingDonation);
              break;
            }
          }
        }

        // when we are editing this donation, just fallback to the default name in all cases for simplicity (it might be overwritten by the text box)
        String user = userToShow == null || this.editingDonations.containsKey(donation) ? donation.name : userToShow.channel.displayName;
        userNameElement = new LabelElement(super.context, this).setText(user).setFontScale(0.75f).setOverflow(TextOverflow.SPLIT);

        // clean up, in case we are transitioning from editing to non-editing
        this.editingElements.remove(donation);
      }

      return Collections.list(
          new LabelElement(super.context, this).setText(dateStr).setFontScale(0.75f),
          userNameElement,
          new LabelElement(super.context, this).setText(donation.formattedAmount).setFontScale(0.75f).setAlignment(TextAlignment.CENTRE),
          new MessagePartsElement(super.context, this).setMessageParts(Collections.list(donation.messageParts)).setScale(0.75f),
          actionElement
      );
    }

    public DonationsTable setDonations(@Nullable List<PublicDonation> donations) {
      this.donations = donations;
      this.updateTable();
      return this;
    }

    public DonationsTable setFilter(boolean showCurrentLivestreamOnly, boolean showUnlinkedOnly) {
      this.showCurrentLivestreamOnly = showCurrentLivestreamOnly;
      this.showUnlinkedOnly = showUnlinkedOnly;
      this.updateTable();
      return this;
    }

    @Override
    public ContainerElement setVisible(boolean visible) {
      if (!visible) {
        // do some cleaning up, since donation object references will no longer match when we next fetch the donations
        this.editingElements.clear();
        this.editingDonations.clear();
      }

      return super.setVisible(visible);
    }

    private void onLinkOrUnlink(PublicDonation donation) {
      // show the editing UI
      this.editingDonations.put(donation, null);
      this.updateDonation(donation);
    }

    private void onConfirmLinkOrUnlink(PublicDonation donation) {
      if (donation.linkedUser == null) {
        PublicUser userToLink = this.editingDonations.get(donation);
        if (userToLink == null) {
          // this should never happen
          this.onError.accept(new Exception("No user selected"));
          return;
        }
        super.context.donationApiStore.linkUser(
            donation.id,
            userToLink.primaryUserId,
            r -> this.onResponse(userToLink.primaryUserId, r.updatedDonation, null),
            e -> this.onResponse(userToLink.primaryUserId, donation, e)
        );
      } else {
        super.context.donationApiStore.unlinkUser(
            donation.id,
            r -> this.onResponse(donation.linkedUser.primaryUserId, r.updatedDonation, null),
            e -> this.onResponse(donation.linkedUser.primaryUserId, donation, e)
        );
      }

      // show loading spinner
      this.table.updateItem(donation, this.getRow(donation, true));
    }

    private void onCancelLinkOrUnlink(PublicDonation donation) {
      // hide the editing UI
      this.editingDonations.remove(donation);
      this.updateDonation(donation);
    }

    private void onResponse(int affectedUserId, PublicDonation donation, @Nullable Throwable e) {
      super.context.rankApiStore.invalidateUserRanks(affectedUserId);
      super.context.renderer.runSideEffect(() -> {
        if (e != null) {
          this.onError.accept(e);
        } else {
          this.editingDonations.keySet().removeIf(d -> Objects.equals(d.id, donation.id));
          this.donations = Collections.replaceOne(this.donations, donation, d -> Objects.equals(d.id, donation.id));

          // all donations that share the linkIdentifier have also been linked/unlinked - update these as well so we don't need to make another server request
          for (PublicDonation linkedDonation : this.getDonationsWithLinkIdentifier(donation)) {
            if (linkedDonation == donation) {
              continue;
            }

            linkedDonation.linkedUser = donation.linkedUser;
            linkedDonation.linkedAt = donation.linkedAt;
          }
        }
        this.updateTable();
      });
    }

    private void updateTable() {
      List<PublicDonation> donationsToShow = Collections.filter(this.donations, d -> {
        // filter doesn't apply if livestream hasn't started, or there is no active livestream
        if (this.showCurrentLivestreamOnly &&
            this.livestreamStatus != null &&
            this.livestreamStatus.livestream.status == LivestreamStatus.Live &&
            d.time < this.livestreamStatus.livestream.startTime) {
          return false;
        }

        if (this.showUnlinkedOnly && d.linkedUser != null) {
          return false;
        }

        return true;
      });

      boolean showTable = donationsToShow.size() > 0;
      this.table.setItems(donationsToShow).setVisible(showTable);
      this.nothingToShowLabel.setVisible(!showTable);
    }

    private List<PublicDonation> getDonationsWithLinkIdentifier(PublicDonation donation) {
      return Collections.filter(this.donations, d -> Objects.equals(d.linkIdentifier, donation.linkIdentifier));
    }

    private void updateDonation(PublicDonation donation) {
      this.table.updateItem(donation, this.getRow(donation, false));
      this.getDonationsWithLinkIdentifier(donation).forEach(d -> this.table.updateItem(d, this.getRow(d, false)));
    }
  }
}
