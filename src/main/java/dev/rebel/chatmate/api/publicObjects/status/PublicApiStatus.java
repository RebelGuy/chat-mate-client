package dev.rebel.chatmate.api.publicObjects.status;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class PublicApiStatus {
  public @Nullable ApiStatus status;
  public @Nullable Long lastOk;
  public @Nullable Long avgRoundTrip;

  public enum ApiStatus {
    @SerializedName("ok") OK,
    @SerializedName("error") Error
  }
}
