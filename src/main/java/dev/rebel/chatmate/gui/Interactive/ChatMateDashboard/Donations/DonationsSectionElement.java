package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
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
import dev.rebel.chatmate.models.api.donation.GetDonationsResponse.GetDonationsResponseData;
import dev.rebel.chatmate.models.api.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.models.api.donation.UnlinkUserResponse.UnlinkUserResponseData;
import dev.rebel.chatmate.models.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.models.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.Objects.casted;
import static dev.rebel.chatmate.services.util.TextHelpers.dateToDayAccuracy;

public class DonationsSectionElement extends ContainerElement implements ISectionElement {
  private final DonationEndpointProxy donationEndpointProxy;
  private final @Nullable Integer highlightDonation;
  private final ApiRequestService apiRequestService;

  private final CheckboxInputElement unlinkedDonationsCheckbox;
  private final CheckboxInputElement currentLivestreamCheckbox;
  private final LabelElement nothingToShowLabel;
  private final LoadingSpinnerElement loadingSpinner;
  private final LabelElement errorLabel;
  private final DonationsTable donationsTable;

  public DonationsSectionElement(InteractiveContext context, IElement parent, @Nullable DonationRoute route, DonationEndpointProxy donationEndpointProxy, StatusService statusService, ApiRequestService apiRequestService) {
    super(context, parent, LayoutMode.INLINE);
    this.donationEndpointProxy = donationEndpointProxy;
    this.apiRequestService = apiRequestService;
    this.apiRequestService.onActive(this::onApiServiceActive);

    this.unlinkedDonationsCheckbox = new CheckboxInputElement(context, this)
        .setChecked(true)
        .onCheckedChanged(this::onFilterChanged)
        .setLabel("Show only unlinked donations")
        .setSizingMode(SizingMode.FILL)
        .setMargin(new RectExtension(ZERO, gui(2)))
        .cast();
    this.currentLivestreamCheckbox = new CheckboxInputElement(context, this)
        .setChecked(true)
        .onCheckedChanged(this::onFilterChanged)
        .setLabel("Show only donations from the current livestream")
        .setSizingMode(SizingMode.FILL)
        .setMargin(new RectExtension(ZERO, ZERO, gui(2), gui(8)))
        .cast();

    this.nothingToShowLabel = new LabelElement(this.context, this)
        .setText("Nothing to show")
        .setFontScale(0.75f)
        .setAlignment(TextAlignment.CENTRE)
        .setSizingMode(SizingMode.FILL)
        .setVisible(false)
        .cast();
    this.loadingSpinner = new LoadingSpinnerElement(this.context, this)
        .setHorizontalAlignment(HorizontalAlignment.CENTRE)
        .setMaxContentWidth(gui(16))
        .setMargin(new RectExtension(gui(8)))
        .setVisible(false)
        .setSizingMode(SizingMode.FILL)
        .cast();

    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this);

    this.donationsTable = new DonationsTable(context, this, this::onError, statusService.getLivestreamStatus(), donationEndpointProxy)
        .setVisible(false)
        .cast();

    // todo: highlight the donation in the list that we want to link (e.g. outline, or lighter background)
    this.highlightDonation = casted(LinkDonationRoute.class, route, d -> d.donation.id);

    super.addElement(this.unlinkedDonationsCheckbox);
    super.addElement(this.currentLivestreamCheckbox);
    super.addElement(this.nothingToShowLabel);
    super.addElement(this.loadingSpinner);
    super.addElement(this.errorLabel);
    super.addElement(this.donationsTable);
  }

  private void onDonationsResponse(GetDonationsResponseData getDonationsResponseData) {
    this.loadingSpinner.setVisible(false);
    if (getDonationsResponseData.donations.length == 0) {
      this.nothingToShowLabel.setVisible(true);
    } else {
      this.donationsTable.setDonations(Arrays.asList(getDonationsResponseData.donations));
    }
  }

  private void onApiServiceActive(Boolean isActive) {
    if (isActive) {
      this.errorLabel.setVisible(false);
    }
  }

  private void onError(Throwable error) {
    this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error));
    this.errorLabel.setVisible(true);
    this.loadingSpinner.setVisible(false);
  }

  private void onFilterChanged(boolean x) {
    this.donationsTable.setFilter(this.currentLivestreamCheckbox.getChecked(), this.unlinkedDonationsCheckbox.getChecked());
  }

  public void onShow() {
    this.donationEndpointProxy.getDonationsAsync(this::onDonationsResponse, this::onError);
    this.loadingSpinner.setVisible(true);
  }

  public void onHide() {
    this.errorLabel.setVisible(false);
    this.nothingToShowLabel.setVisible(false);
    this.donationsTable.setVisible(false);
    this.loadingSpinner.setVisible(false);
  }

  private static class DonationsTable extends ContainerElement {
    private final Consumer<Throwable> onError;
    private final @Nullable PublicLivestreamStatus livestreamStatus;
    private final DonationEndpointProxy donationEndpointProxy;
    private @Nullable List<PublicDonation> donations = null;
    private boolean showCurrentLivestreamOnly = true;
    private boolean showUnlinkedOnly = true;

    private TableElement<PublicDonation> table;

    public DonationsTable(InteractiveContext context, IElement parent, Consumer<Throwable> onError, @Nullable PublicLivestreamStatus livestreamStatus, DonationEndpointProxy donationEndpointProxy) {
      super(context, parent, LayoutMode.BLOCK);
      this.onError = onError;
      this.livestreamStatus = livestreamStatus;
      this.donationEndpointProxy = donationEndpointProxy;

      List<TableElement.Column> columns = Collections.list(
        new TableElement.Column("Date", 0.75f, 1, true),
        new TableElement.Column("User", 0.75f, 2, false),
        new TableElement.Column("Amount", 0.75f, 1, true),
        new TableElement.Column("Message", 0.75f, 3, false),
        new TableElement.Column("", 0.75f, 0.5f, true)
      );
      this.table = new TableElement<>(context, this, new ArrayList<>(), columns, don -> this.getRow(don, false));
    }

    private List<IElement> getRow(PublicDonation donation, boolean isUpdating) {
      String dateStr = dateToDayAccuracy(donation.time);
      String user = donation.linkedUser == null ? donation.name : donation.linkedUser.userInfo.channelName;
      String formattedAmount = String.format("$%.2f", donation.amount);

      IElement iconElement;
      if (isUpdating) {
        iconElement = new LoadingSpinnerElement(super.context, this).setTargetHeight(super.context.fontEngine.FONT_HEIGHT_DIM).setHorizontalAlignment(HorizontalAlignment.CENTRE).setVerticalAlignment(VerticalAlignment.MIDDLE);
      } else {
        Texture icon = donation.linkedUser == null ? Asset.GUI_LINK_ICON : Asset.GUI_UNLINK_ICON;
        Runnable onClick = isUpdating ? null : () -> this.onLinkOrUnlink(donation);
        iconElement = new IconButtonElement(super.context, this).setImage(icon).setOnClick(onClick).setTargetHeight(super.context.fontEngine.FONT_HEIGHT_DIM);
      }

      return Collections.list(
          new LabelElement(super.context, this).setText(dateStr).setFontScale(0.75f),
          new LabelElement(super.context, this).setText(user).setFontScale(0.75f).setOverflow(TextOverflow.SPLIT),
          new LabelElement(super.context, this).setText(formattedAmount).setFontScale(0.75f).setAlignment(TextAlignment.CENTRE),
          new LabelElement(super.context, this).setText(donation.message).setFontScale(0.75f).setOverflow(TextOverflow.SPLIT),
          iconElement
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

    private void onLinkOrUnlink(PublicDonation donation) {
      if (donation.linkedUser == null) {
        this.donationEndpointProxy.linkUserAsync(donation.id, 1, this::onLinkResponse, this.onError);
      } else {
        this.donationEndpointProxy.unlinkUserAsync(donation.id, this::onUnlinkResponse, this.onError);
      }

      this.table.updateItem(donation, this.getRow(donation, true));
    }

    private void onLinkResponse(LinkUserResponseData data) {
      this.table.updateItem(data.updatedDonation, this.getRow(data.updatedDonation, false));
    }

    private void onUnlinkResponse(UnlinkUserResponseData data) {
      this.table.updateItem(data.updatedDonation, this.getRow(data.updatedDonation, false));
    }

    private void updateTable() {
      this.table.setItems(Collections.filter(this.donations, d -> {
        // filter doesn't apply if livestream hasn't started, or there is no active livestream\
        if (this.showCurrentLivestreamOnly && this.livestreamStatus != null && this.livestreamStatus.startTime != null && d.time < this.livestreamStatus.startTime) {
          return false;
        }

        if (this.showUnlinkedOnly && d.linkedUser != null) {
          return false;
        }

        return true;
      }));
    }
  }

  @FunctionalInterface
  public interface AsyncRequest<T> {
    void onRequest(T data, Runnable onDone);
  }
}
