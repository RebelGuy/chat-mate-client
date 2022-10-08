package dev.rebel.chatmate.api.publicObjects.status;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicApiStatus extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public @Nullable ApiStatus status;
  public @Nullable Long lastOk;
  public @Nullable Long avgRoundTrip;

  public enum ApiStatus {
    @SerializedName("ok") OK,
    @SerializedName("error") Error
  }
}
