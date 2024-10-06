package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class CreateDonationRequest extends ApiRequestBase {
  public final Float amount;
  public final String currencyCode;
  public final String name;
  public final @Nullable String message;

  public CreateDonationRequest(Float amount, String currencyCode, String name, @Nullable String message) {
    this.amount = amount;
    this.currencyCode = currencyCode;
    this.name = name;
    this.message = message;
  }
}
