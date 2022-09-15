package dev.rebel.chatmate.models.api.donation;

import dev.rebel.chatmate.models.api.donation.UnlinkUserResponse.UnlinkUserResponseData;
import dev.rebel.chatmate.models.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class UnlinkUserResponse extends ApiResponseBase<UnlinkUserResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class UnlinkUserResponseData {
    public PublicDonation updatedDonation;
  }
}
