package dev.rebel.chatmate.models.api.donation;

import dev.rebel.chatmate.models.api.donation.GetDonationsResponse.GetDonationsResponseData;
import dev.rebel.chatmate.models.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetDonationsResponse extends ApiResponseBase<GetDonationsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetDonationsResponseData {
    public PublicDonation[] donations;
  }
}
