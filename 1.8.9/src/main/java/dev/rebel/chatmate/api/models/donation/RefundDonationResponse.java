package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.models.donation.RefundDonationResponse.RefundDonationResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;

public class RefundDonationResponse extends ApiResponseBase<RefundDonationResponseData> {
  public static class RefundDonationResponseData {
    public PublicDonation updatedDonation;
  }
}
