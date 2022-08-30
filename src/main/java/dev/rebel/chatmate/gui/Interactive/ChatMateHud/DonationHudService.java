package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHudStore.IDonationListener;
import dev.rebel.chatmate.gui.hud.IHudComponent;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.GuiService;

public class DonationHudService implements IDonationListener {
  private final DonationHudStore donationHudStore;
  private final ChatMateHudStore chatMateHudStore;
  private final GuiService guiService;
  private final DimFactory dimFactory;

  public DonationHudService(ChatMateHudStore chatMateHudStore, DonationHudStore donationHudStore, GuiService guiService, DimFactory dimFactory) {
    this.donationHudStore = donationHudStore;
    this.chatMateHudStore = chatMateHudStore;
    this.guiService = guiService;
    this.dimFactory = dimFactory;

    donationHudStore.addListener(this);
  }

  @Override
  public void onNextDonation(PublicDonationData donation) {
    this.chatMateHudStore.addElement((context, parent) -> new DonationHudElement(context, parent, this.chatMateHudStore, this::onCloseDonation, this::onOpenDashboard, donation))
        .setDefaultPosition(this.dimFactory.getMinecraftRect().getTopCentre(), IHudComponent.Anchor.TOP_CENTRE);
  }

  private void onOpenDashboard(PublicDonationData donation) {
    this.guiService.displayDashboard(new DashboardRoute.LinkDonationRoute(donation));
  }

  private void onCloseDonation() {
    this.donationHudStore.dismissCurrentDonation();
  }
}
