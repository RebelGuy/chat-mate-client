package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.models.donation.GetDonationsResponse.GetDonationsResponseData;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetDonationsResponse extends ApiResponseBase<GetDonationsResponseData> {
  public static class GetDonationsResponseData {
    public PublicDonation[] donations;
  }
}
