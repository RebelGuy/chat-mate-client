package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.api.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.api.proxy.UserEndpointProxy;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.services.StatusService;

import javax.annotation.Nullable;

public class DonationsSectionMainElement extends ContainerElement implements ChatMateDashboardElement.ISectionElement {
  private final DonationEndpointProxy donationEndpointProxy;

  private final DonationsListElement donationsListElement;
  private @Nullable CreateDonationElement createDonationElement;

  public DonationsSectionMainElement(InteractiveScreen.InteractiveContext context,
                                     IElement parent,
                                     @Nullable DashboardRoute.DonationRoute route,
                                     StatusService statusService,
                                     ApiRequestService apiRequestService,
                                     UserEndpointProxy userEndpointProxy,
                                     MessageService messageService,
                                     DonationEndpointProxy donationEndpointProxy) {
    super(context, parent, LayoutMode.BLOCK);
    this.donationEndpointProxy = donationEndpointProxy;

    this.donationsListElement = new DonationsListElement(context, this, route, statusService, apiRequestService, userEndpointProxy, messageService, this::onShowCreateDonationElement);
    this.createDonationElement = null;

    super.addElement(this.donationsListElement);
  }

  @Override
  public void onShow() {
    if (this.donationsListElement.getVisible()) {
      this.donationsListElement.onShow();
    }
  }

  @Override
  public void onHide() {
    if (this.donationsListElement.getVisible()) {
      this.donationsListElement.onHide();
    }
  }

  private void onShowCreateDonationElement() {
    this.createDonationElement = new CreateDonationElement(context, this, this::onFinishCreateDonation, this.donationEndpointProxy);
    super.addElement(this.createDonationElement);

    // setting visibility breaks things because container elements also set the visibility of their children.
    // instead, remove the list element and re-add it below when needed.
    super.removeElement(this.donationsListElement);
    this.donationsListElement.onHide();
  }

  private void onFinishCreateDonation() {
    if (this.createDonationElement != null) {
      super.removeElement(this.createDonationElement);
      this.createDonationElement = null;
    }

    super.addElement(this.donationsListElement);
    this.donationsListElement.onShow();
  }
}
