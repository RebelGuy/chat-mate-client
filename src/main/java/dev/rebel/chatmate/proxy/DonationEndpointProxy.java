package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.api.donation.GetDonationsResponse;
import dev.rebel.chatmate.models.api.donation.GetDonationsResponse.GetDonationsResponseData;
import dev.rebel.chatmate.models.api.donation.LinkUserResponse;
import dev.rebel.chatmate.models.api.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.models.api.donation.UnlinkUserResponse;
import dev.rebel.chatmate.models.api.donation.UnlinkUserResponse.UnlinkUserResponseData;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DonationEndpointProxy extends EndpointProxy {
  public DonationEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/donation");
  }

  public void getDonationsAsync(Consumer<GetDonationsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "", GetDonationsResponse.class, callback, errorHandler, true);
  }

  public void linkUserAsync(int donationId, int userId, Consumer<LinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/link?donationId=%d&userId=%d", donationId, userId);
    this.makeRequestAsync(Method.POST, url, LinkUserResponse.class, callback, errorHandler, true);
  }

  public void unlinkUserAsync(int donationId, Consumer<UnlinkUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/link?donationId=%d", donationId);
    this.makeRequestAsync(Method.DELETE, url, UnlinkUserResponse.class, callback, errorHandler, true);
  }
}
