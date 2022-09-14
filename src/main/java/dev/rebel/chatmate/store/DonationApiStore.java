package dev.rebel.chatmate.store;

import dev.rebel.chatmate.models.api.donation.GetDonationsResponse;
import dev.rebel.chatmate.models.api.donation.GetDonationsResponse.GetDonationsResponseData;
import dev.rebel.chatmate.models.api.donation.LinkUserResponse;
import dev.rebel.chatmate.models.api.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.models.api.donation.UnlinkUserResponse;
import dev.rebel.chatmate.models.api.donation.UnlinkUserResponse.UnlinkUserResponseData;
import dev.rebel.chatmate.models.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.services.events.ChatMateEventService;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DonationApiStore {
  private final DonationEndpointProxy donationEndpointProxy;

  private @Nullable List<PublicDonation> donations;
  private @Nullable String getDonationsError;
  private boolean loading;

  public DonationApiStore(DonationEndpointProxy donationEndpointProxy) {
    this.donationEndpointProxy = donationEndpointProxy;

    this.donations = null;
    this.getDonationsError = null;
    this.loading = false;
  }

  public void invalidateStore() {
    this.donations = null;
  }

  /** Fetches donations from the server. */
  public void loadDonations(Consumer<List<PublicDonation>> callback, @Nullable Consumer<Throwable> errorHandler, boolean forceLoad) {
    if (this.donations != null && !forceLoad) {
      callback.accept(this.donations);
      return;
    } else if (this.loading) {
      callback.accept(new ArrayList<>());
      return;
    }

    this.loading = true;
    this.donationEndpointProxy.getDonationsAsync(res -> {
      this.donations = Collections.orderBy(Collections.list(res.donations), d -> d.time);
      this.getDonationsError = null;
      this.loading = false;
      callback.accept(this.donations);
    }, err -> {
      this.donations = null;
      this.getDonationsError = EndpointProxy.getApiErrorMessage(err);
      this.loading = false;
      errorHandler.accept(err);
    });
  }

  /** Gets loaded donations, sorted by time in ascending order. */
  public @Nonnull List<PublicDonation> getDonations() {
    if (this.donations == null) {
      if (!this.loading) {
        this.loadDonations(r -> {}, e -> {}, false);
      }
      return new ArrayList<>();
    } else {
      return this.donations;
    }
  }

  public @Nullable String getError() {
    return this.getDonationsError;
  }

  public void linkUser(int donationId, int userId, Consumer<LinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.donationEndpointProxy.linkUserAsync(
        donationId,
        userId,
        res -> {
          this.updateDonation(res.updatedDonation);
          callback.accept(res);
        }, errorHandler);
  }

  public void unlinkUser(int donationId, Consumer<UnlinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.donationEndpointProxy.unlinkUserAsync(
        donationId,
        res -> {
          this.updateDonation(res.updatedDonation);
          callback.accept(res);
        }, errorHandler
    );
  }

  private void updateDonation(PublicDonation updatedDonation) {
    if (this.donations == null) {
      this.donations = Collections.list(updatedDonation);
    } else {
      // copy the collection so the reference changes
      this.donations = Collections.replaceOne(Collections.list(this.donations), updatedDonation, d -> Objects.equals(d.id, updatedDonation.id));
    }
  }
}
