package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.models.donation.GetCurrenciesResponse.GetCurrenciesResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.donation.PublicCurrency;

public class GetCurrenciesResponse extends ApiResponseBase<GetCurrenciesResponseData> {
  public static class GetCurrenciesResponseData {
    public PublicCurrency[] currencies;
  }
}
