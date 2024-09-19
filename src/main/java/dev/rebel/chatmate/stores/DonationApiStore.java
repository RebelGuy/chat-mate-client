package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.models.donation.CreateDonationRequest;
import dev.rebel.chatmate.api.models.donation.CreateDonationResponse.CreateDonationsResponseData;
import dev.rebel.chatmate.api.models.donation.DeleteDonationResponse.DeleteDonationResponseData;
import dev.rebel.chatmate.api.models.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.api.models.donation.RefundDonationResponse.RefundDonationResponseData;
import dev.rebel.chatmate.api.models.donation.UnlinkUserResponse.UnlinkUserResponseData;
import dev.rebel.chatmate.api.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Memoiser;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DonationApiStore extends ApiStore<PublicDonation> {
  private final DonationEndpointProxy donationEndpointProxy;
  private final Memoiser memoiser;

  public DonationApiStore(DonationEndpointProxy donationEndpointProxy, Config config) {
    super(config, true);

    this.donationEndpointProxy = donationEndpointProxy;
    this.memoiser = new Memoiser();
  }

  @Override
  public @Nullable List<PublicDonation> getData() {
    @Nullable List<PublicDonation> data = super.getData();
    if (data == null) {
      return null;
    }

    // retain the re-ordered object until the reference of the underlying data changes
    return this.memoiser.memoiseOne(
        () -> Collections.reverse(Collections.orderBy(Collections.list(data), d -> d.time)),
        data
    );
  }

  public void linkUser(int donationId, int userId, Consumer<LinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.donationEndpointProxy.linkUserAsync(
        donationId,
        userId,
        res -> {
          super.updateOne(res.updatedDonation);
          callback.accept(res);
        },
        errorHandler
    );
  }

  public void unlinkUser(int donationId, Consumer<UnlinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.donationEndpointProxy.unlinkUserAsync(
        donationId,
        res -> {
          super.updateOne(res.updatedDonation);
          callback.accept(res);
        },
        errorHandler
    );
  }

  public void refundDonation(int donationId, Consumer<RefundDonationResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.donationEndpointProxy.refundDonation(
        donationId,
        res -> {
          super.updateOne(res.updatedDonation);
          callback.accept(res);
        },
        errorHandler
    );
  }

  public void deleteDonation(int donationId, Consumer<DeleteDonationResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.donationEndpointProxy.deleteDonation(
        donationId,
        res -> {
          PublicDonation donation = Collections.first(super.getData(), d -> d.id == donationId);
          if (donation != null) {
            super.deleteOne(donation);
          }

          callback.accept(res);
        },
        errorHandler
    );
  }

  public void createDonation(CreateDonationRequest request, Consumer<CreateDonationsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.donationEndpointProxy.createDonationAsync(
        request,
        res -> {
          super.addOne(res.newDonation);
          callback.accept(res);
        },
        errorHandler
    );
  }

  @Override
  protected void onFetchData(Consumer<List<PublicDonation>> onData, Consumer<Throwable> onError, boolean isActiveRequest) {
    this.donationEndpointProxy.getDonationsAsync(
        res -> onData.accept(Collections.list(res.donations)),
        onError,
        isActiveRequest
    );
  }

  @Override
  protected boolean onMatchItems(PublicDonation a, PublicDonation b) {
    return Objects.equals(a.id, b.id);
  }
}
