package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.models.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class LinkUserResponse extends ApiResponseBase<LinkUserResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class LinkUserResponseData {
    public PublicDonation updatedDonation;
  }
}
