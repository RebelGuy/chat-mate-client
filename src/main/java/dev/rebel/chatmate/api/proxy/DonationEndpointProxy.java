package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.donation.*;
import dev.rebel.chatmate.api.models.donation.CreateDonationResponse.CreateDonationsResponseData;
import dev.rebel.chatmate.api.models.donation.DeleteDonationResponse.DeleteDonationResponseData;
import dev.rebel.chatmate.api.models.donation.GetCurrenciesResponse.GetCurrenciesResponseData;
import dev.rebel.chatmate.api.models.donation.GetDonationsResponse.GetDonationsResponseData;
import dev.rebel.chatmate.api.models.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.api.models.donation.RefundDonationResponse.RefundDonationResponseData;
import dev.rebel.chatmate.api.models.donation.UnlinkUserResponse.UnlinkUserResponseData;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DonationEndpointProxy extends EndpointProxy {
  public DonationEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/donation");
  }

  public void getDonationsAsync(Consumer<GetDonationsResponseData> callback, @Nullable Consumer<Throwable> errorHandler, boolean isActiveRequest) {
    this.makeRequestAsync(Method.GET, "", GetDonationsResponse.class, callback, errorHandler, isActiveRequest);
  }

  public void createDonationAsync(CreateDonationRequest request, Consumer<CreateDonationsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "", request, CreateDonationResponse.class, callback, errorHandler, true);
  }

  public void getCurrenciesAsync(Consumer<GetCurrenciesResponseData> callback, @Nullable Consumer<Throwable> errorHandler, boolean isActiveRequest) {
    this.makeRequestAsync(Method.GET, "/currencies", GetCurrenciesResponse.class, callback, errorHandler, isActiveRequest);
  }

  public void linkUserAsync(int donationId, int userId, Consumer<LinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/link?donationId=%d&userId=%d", donationId, userId);
    this.makeRequestAsync(Method.POST, url, LinkUserResponse.class, callback, errorHandler, true);
  }

  public void unlinkUserAsync(int donationId, Consumer<UnlinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/link?donationId=%d", donationId);
    this.makeRequestAsync(Method.DELETE, url, UnlinkUserResponse.class, callback, errorHandler, true);
  }

  public void refundDonation(int donationId, Consumer<RefundDonationResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/refund?donationId=%d", donationId);
    this.makeRequestAsync(Method.POST, url, RefundDonationResponse.class, callback, errorHandler, true);
  }

  public void deleteDonation(int donationId, Consumer<DeleteDonationResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("?donationId=%d", donationId);
    this.makeRequestAsync(Method.DELETE, url, DeleteDonationResponse.class, callback, errorHandler, true);
  }
}
