package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.GuiService;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.SoundService;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.events.models.DonationEventData;

public class DonationHudService implements DonationHudStore.IDonationHudStoreListener {
  private final DonationHudStore donationHudStore;
  private final ChatMateHudStore chatMateHudStore;
  private final GuiService guiService;
  private final DimFactory dimFactory;
  private final SoundService soundService;
  private final ChatMateEventService chatMateEventService;
  private final LogService logService;

  public DonationHudService(ChatMateHudStore chatMateHudStore,
                            DonationHudStore donationHudStore,
                            GuiService guiService,
                            DimFactory dimFactory,
                            SoundService soundService,
                            ChatMateEventService chatMateEventService,
                            LogService logService) {
    this.donationHudStore = donationHudStore;
    this.chatMateHudStore = chatMateHudStore;
    this.guiService = guiService;
    this.dimFactory = dimFactory;
    this.soundService = soundService;
    this.chatMateEventService = chatMateEventService;
    this.logService = logService;

    chatMateEventService.onDonation(this::onNewDonation);
    donationHudStore.addListener(this);
  }

  @Override
  public void onNextDonation(PublicDonationData donation) {
    this.chatMateHudStore.addElement((context, parent) -> new DonationHudElement(context, parent, this.chatMateHudStore, this::onCloseDonation, this::onOpenDashboard, donation))
        .setDefaultPosition(this.dimFactory.getMinecraftRect().getTopCentre(), HudElement.Anchor.TOP_CENTRE);
    this.soundService.playDragonKill(1.2f);
  }

  @Override
  public void onClear() {
    for (HudElement element : this.chatMateHudStore.getElements()) {
      if (element instanceof DonationHudElement) {
        this.chatMateHudStore.removeElement(element);
      }
    }
  }

  private void onNewDonation(Event<DonationEventData> event) {
    this.donationHudStore.addDonation(event.getData().donation);
  }

  private void onOpenDashboard(PublicDonationData donation) {
    this.guiService.displayDashboard(new DashboardRoute.LinkDonationRoute(donation));
  }

  private void onCloseDonation() {
    this.donationHudStore.dismissCurrentDonation();
  }
}
