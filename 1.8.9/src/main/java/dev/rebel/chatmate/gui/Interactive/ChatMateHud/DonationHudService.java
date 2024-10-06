package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.GuiService;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.SoundService;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.events.models.DonationEventData;
import dev.rebel.chatmate.stores.DonationApiStore;
import dev.rebel.chatmate.util.Collections;

import java.util.Objects;

public class DonationHudService implements DonationHudStore.IDonationHudStoreListener {
  private final DonationHudStore donationHudStore;
  private final ChatMateHudStore chatMateHudStore;
  private final GuiService guiService;
  private final DimFactory dimFactory;
  private final SoundService soundService;
  private final ChatMateEventService chatMateEventService;
  private final LogService logService;
  private final DonationApiStore donationApiStore;

  public DonationHudService(ChatMateHudStore chatMateHudStore,
                            DonationHudStore donationHudStore,
                            GuiService guiService,
                            DimFactory dimFactory,
                            SoundService soundService,
                            ChatMateEventService chatMateEventService,
                            LogService logService,
                            DonationApiStore donationApiStore) {
    this.donationHudStore = donationHudStore;
    this.chatMateHudStore = chatMateHudStore;
    this.guiService = guiService;
    this.dimFactory = dimFactory;
    this.soundService = soundService;
    this.chatMateEventService = chatMateEventService;
    this.logService = logService;
    this.donationApiStore = donationApiStore;

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
    PublicDonationData newDonation = event.getData().donation;

    // if we already know about the donation, there is a very high chance (close to 100%) that the user created it
    // within the client just before. in that case, showing the notification is unnecessary.
    if (Collections.any(this.donationApiStore.getData(), d -> Objects.equals(d.id, newDonation.id))) {
      this.logService.logInfo(this, "Ignoring donation with id", newDonation.id, "because we already know about it.");
      return;
    }

    this.donationHudStore.addDonation(newDonation);
  }

  private void onOpenDashboard(PublicDonationData donation) {
    this.guiService.displayDashboard(new DashboardRoute.LinkDonationRoute(donation));
  }

  private void onCloseDonation() {
    this.donationHudStore.dismissCurrentDonation();
  }
}
