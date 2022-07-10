package dev.rebel.chatmate.services.events;

import com.google.common.collect.Lists;
import dev.rebel.chatmate.models.api.log.GetTimestampsResponse.GetTimestampsResponseData;
import dev.rebel.chatmate.proxy.LogEndpointProxy;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.ServerLogEventService.Events;
import dev.rebel.chatmate.services.events.models.EventData;
import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOptions;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.util.ApiPoller;
import dev.rebel.chatmate.util.ApiPoller.PollType;
import dev.rebel.chatmate.util.ApiPollerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ServerLogEventService extends EventServiceBase<Events> {
  private final ApiPoller<GetTimestampsResponseData> apiPoller;

  private boolean initialised;
  private Long[] warnings;
  private Long[] errors;

  public ServerLogEventService(LogService logService, LogEndpointProxy logEndpointProxy, ApiPollerFactory apiPollerFactory) {
    super(Events.class, logService);

    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, this::onApiError, logEndpointProxy::getTimestamps, 5000, PollType.CONSTANT_INTERVAL);

    this.initialised = false;
    this.warnings = new Long[0];
    this.errors = new Long[0];
  }

  public Long[] getWarnings() {
    return this.warnings;
  }

  public Long[] getErrors() {
    return this.errors;
  }

  public void onInitialise(Function<EventIn, EventOut> callback) {
    super.addListener(Events.INITIALISE, callback, null);
  }

  public void onWarning(Function<EventIn, EventOut> callback) {
    super.addListener(Events.WARNING, callback, null);
  }

  public void onError(Function<EventIn, EventOut> callback) {
    super.addListener(Events.ERROR, callback, null);
  }

  private void onApiResponse(GetTimestampsResponseData data) {
    boolean newError = !Objects.equals(this.errors[this.errors.length - 1], data.timestamps.errors[data.timestamps.errors.length - 1]);
    boolean newWarning = !Objects.equals(this.warnings[this.warnings.length - 1], data.timestamps.warnings[data.timestamps.warnings.length - 1]);

    this.errors = data.timestamps.errors;
    this.warnings = data.timestamps.warnings;

    if (!this.initialised) {
      this.initialised = true;
      this.fireEvent(Events.INITIALISE);
    }
    if (newError) {
      this.fireEvent(Events.ERROR);
    }
    if (newWarning) {
      this.fireEvent(Events.WARNING);
    }
  }

  private void onApiError(Throwable error) {
    // treat this as another server error and just append the current time to the error array.
    // as soon as we receive a successful response, these synthetic errors will be overwritten by the server response.

    // fuck off java
    List<Long> errorsList = Collections.list(this.errors);
    errorsList.add(new Date().getTime());
    this.errors = errorsList.toArray(new Long[0]);
    this.fireEvent(Events.ERROR);
  }

  private void fireEvent(Events event) {
    for (EventHandler<EventIn, EventOut, EventOptions> listener : super.getListeners(event, EventData.Empty.class)) {
      super.safeDispatch(event, listener, new EventIn());
    }
  }

  public enum Events { INITIALISE, WARNING, ERROR }
}
