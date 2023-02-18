package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.models.chat.GetCommandStatusResponse.CommandStatus;
import dev.rebel.chatmate.api.models.chat.GetCommandStatusResponse.GetCommandStatusResponseData;
import dev.rebel.chatmate.api.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.util.LruCache;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CommandApiStore {
  private static final int MIN_PADDING_MS = 500;

  private final ChatEndpointProxy chatEndpointProxy;

  private final Set<Integer> loading = new HashSet<>();

  // only stores successful responses
  private final LruCache<Integer, Tuple2<Long, GetCommandStatusResponseData>> cache = new LruCache<>(100);

  public CommandApiStore(ChatEndpointProxy chatEndpointProxy) {
    this.chatEndpointProxy = chatEndpointProxy;
  }

  public void clear() {
    this.loading.clear();
    this.cache.clear();
  }

  public void clearCommand(int commandId) {
    this.loading.remove(commandId);
    this.cache.remove(commandId);
  }

  @Nullable
  public GetCommandStatusResponseData getCommandStatus(int commandId) {
    if (this.requiresUpdate(commandId)) {
      this.load(commandId);
    }

    Tuple2<Long, GetCommandStatusResponseData> stored = this.cache.get(commandId);
    return stored == null ? null : stored._2;
  }

  private boolean requiresUpdate(int commandId) {
    if (this.loading.contains(commandId)) {
      return false;
    } else if (!this.cache.has(commandId)) {
      return true;
    } else {
      Tuple2<Long, GetCommandStatusResponseData> stored = this.cache.get(commandId);

      if (stored._2.status == CommandStatus.SUCCESS || stored._2.status == CommandStatus.ERROR) {
        // final state
        return false;
      } else if (new Date().getTime() - stored._1 < MIN_PADDING_MS) {
        // not enough time has passed since the last check
        return false;
      } else {
        return true;
      }
    }
  }

  private void load(int commandId) {
    this.loading.add(commandId);
    this.chatEndpointProxy.getCommandStatus(
        commandId,
        res -> {
          // we have been cleared
          if (!this.loading.contains(commandId)) {
            return;
          }

          this.cache.set(commandId, new Tuple2<>(new Date().getTime(), res));
          this.loading.remove(commandId);
        }, err -> {
          // errors that are not connection errors are final states
          if (err instanceof ConnectException) {
            // force a refresh
            this.loading.remove(commandId);
            this.cache.remove(commandId);
            return;
          }

          String message = "Unable to get information about the command.";
          if (err instanceof ChatMateApiException) {
            ChatMateApiException exception = (ChatMateApiException)err;
            if (exception.apiResponseError.errorCode == 404) {
              message = exception.apiResponseError.message;
            }
          }

          String errorMessage = message; // yes java whatever you say
          this.cache.set(commandId, new Tuple2<>(new Date().getTime(), new GetCommandStatusResponseData() {{
            status = CommandStatus.ERROR;
            message = errorMessage;
            durationMs = 0L;
          }}));
          this.loading.remove(commandId);
        });
  }
}
