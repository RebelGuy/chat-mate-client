package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DonationHudStore {
  private final Config config;
  private final LogService logService;

  private final List<IDonationHudStoreListener> listeners = new ArrayList<>();
  private final List<PublicDonationData> queuedDonations = new ArrayList<>();
  private @Nullable PublicDonationData currentDonation = null;

  public DonationHudStore(Config config, LogService logService) {
    this.config = config;
    this.logService = logService;
  }

  public void addListener(IDonationHudStoreListener listener) {
    this.listeners.add(listener);
  }

  /** Adds the donation to be shown or, if one is already shown, adds it to the queue. */
  public void addDonation(PublicDonationData donation) {
    if (!this.config.getHudEnabledEmitter().get()) {
      return;
    }

    this.queuedDonations.add(donation);

    this.trySetCurrentDonation();
  }

  /** Stop showing the current donation, and show the next one in the queue, if one exists. */
  public void dismissCurrentDonation() {
    if (this.currentDonation != null) {
      this.currentDonation = null;
      this.trySetCurrentDonation();
    }
  }

  public void clear() {
    this.queuedDonations.clear();
    this.dismissCurrentDonation();
    this.listeners.forEach(IDonationHudStoreListener::onClear);
  }

  /** Gets the donation currently shown, if any. */
  public @Nullable PublicDonationData getCurrentDonation() {
    return this.currentDonation;
  }

  private void trySetCurrentDonation() {
    try {
      if (this.currentDonation == null && this.queuedDonations.size() > 0) {
        this.currentDonation = this.queuedDonations.remove(0);
        this.listeners.forEach(l -> l.onNextDonation(this.currentDonation));
      }
    } catch (Exception e) {
      this.logService.logError(this, "Enountered error while notifying listeners of a new donation. Resetting all donations.", e);
      this.clear();
    }
  }

  public interface IDonationHudStoreListener {
    /** Called when the next donation is to be displayed. */
    void onNextDonation(PublicDonationData donation);
    void onClear();
  }
}
