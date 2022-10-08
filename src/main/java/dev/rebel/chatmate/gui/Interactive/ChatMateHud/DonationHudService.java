package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHudStore.IDonationListener;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.GuiService;
import dev.rebel.chatmate.services.SoundService;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.events.models.DonationEventData;

public class DonationHudService implements IDonationListener {
  private final DonationHudStore donationHudStore;
  private final ChatMateHudStore chatMateHudStore;
  private final GuiService guiService;
  private final DimFactory dimFactory;
  private final SoundService soundService;
  private final ChatMateEventService chatMateEventService;

  public DonationHudService(ChatMateHudStore chatMateHudStore, DonationHudStore donationHudStore, GuiService guiService, DimFactory dimFactory, SoundService soundService, ChatMateEventService chatMateEventService) {
    this.donationHudStore = donationHudStore;
    this.chatMateHudStore = chatMateHudStore;
    this.guiService = guiService;
    this.dimFactory = dimFactory;
    this.soundService = soundService;
    this.chatMateEventService = chatMateEventService;

    chatMateEventService.onDonation(this::onNewDonation, null);
    donationHudStore.addListener(this);
  }

  @Override
  public void onNextDonation(PublicDonationData donation) {
    this.chatMateHudStore.addElement((context, parent) -> new DonationHudElement(context, parent, this.chatMateHudStore, this::onCloseDonation, this::onOpenDashboard, donation))
        .setDefaultPosition(this.dimFactory.getMinecraftRect().getTopCentre(), HudElement.Anchor.TOP_CENTRE);
    this.soundService.playDragonKill(1.2f);
  }

  private DonationEventData.Out onNewDonation(DonationEventData.In in) {
    this.donationHudStore.addDonation(in.donation);
    return new DonationEventData.Out();
  }

  private void onOpenDashboard(PublicDonationData donation) {
    this.guiService.displayDashboard(new DashboardRoute.LinkDonationRoute(donation));
  }

  private void onCloseDonation() {
    this.donationHudStore.dismissCurrentDonation();
  }
}
