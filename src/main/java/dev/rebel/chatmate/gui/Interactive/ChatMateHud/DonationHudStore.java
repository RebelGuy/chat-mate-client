package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.config.Config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DonationHudStore {
  private final Config config;

  private final List<IDonationListener> listeners = new ArrayList<>();
  private final List<PublicDonationData> queuedDonations = new ArrayList<>();
  private @Nullable PublicDonationData currentDonation = null;

  public DonationHudStore(Config config) {
    this.config = config;
  }

  public void addListener(IDonationListener listener) {
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

  /** Gets the donation currently shown, if any. */
  public @Nullable PublicDonationData getCurrentDonation() {
    return this.currentDonation;
  }

  private void trySetCurrentDonation() {
    if (this.currentDonation == null && this.queuedDonations.size() > 0) {
      this.currentDonation = this.queuedDonations.remove(0);
      this.listeners.forEach(l -> l.onNextDonation(this.currentDonation));
    }
  }

  public interface IDonationListener {
    /** Called when the next donation is to be displayed. */
    void onNextDonation(PublicDonationData donation);
  }
}
