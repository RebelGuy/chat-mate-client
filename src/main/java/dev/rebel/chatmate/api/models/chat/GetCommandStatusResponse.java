package dev.rebel.chatmate.api.models.chat;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class GetCommandStatusResponse extends ApiResponseBase<GetCommandStatusResponse.GetCommandStatusResponseData> {
  public static class GetCommandStatusResponseData {
    public CommandStatus status;
    public @Nullable String message;
    public @Nullable Long durationMs;
  }

  public enum CommandStatus {
    @SerializedName("success") SUCCESS,
    @SerializedName("error") ERROR,
    @SerializedName("pending") PENDING
  }
}
