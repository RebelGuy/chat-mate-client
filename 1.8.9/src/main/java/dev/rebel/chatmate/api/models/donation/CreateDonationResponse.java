package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;

import static dev.rebel.chatmate.api.models.donation.CreateDonationResponse.*;

public class CreateDonationResponse extends ApiResponseBase<CreateDonationsResponseData> {
  public static class CreateDonationsResponseData {
    public PublicDonation newDonation;
  }
}
