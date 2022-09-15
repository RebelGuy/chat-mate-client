package dev.rebel.chatmate.models.api.donation;

import dev.rebel.chatmate.models.api.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.models.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class LinkUserResponse extends ApiResponseBase<LinkUserResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class LinkUserResponseData {
    public PublicDonation updatedDonation;
  }
}
