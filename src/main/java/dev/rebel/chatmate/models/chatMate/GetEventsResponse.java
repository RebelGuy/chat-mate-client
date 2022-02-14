package dev.rebel.chatmate.models.chatMate;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetEventsResponse extends ApiResponseBase<GetEventsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 2;
  }

  public static class GetEventsResponseData {
    public Long reusableTimestamp;
    public Event[] events;
  }

  public static class Event {
    public EventType type;
    public Long timestamp;
    public Object data; // this is a LinkedTreeMap when originally parsing the JSON

    public <Data extends IEventData> Data getData(Class<Data> dataClass) {
      try {
        // convert back to json, then parse using the now known object type
        Data data = new Gson().fromJson(new Gson().toJson(this.data), dataClass);
        if (data == null) {
          throw new Exception("Could not get the event data because it could not be parsed to type " + dataClass.getSimpleName());
        } else if (data.getEventType() != this.type) {
          throw new Exception("Could not get the event data because the supplied class' event type did not match with the actual event type");
        }
        return data;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public enum EventType {
    @SerializedName("levelUp") LEVEL_UP
  }

  private interface IEventData {
    EventType getEventType();
  }

  public static class LevelUpData implements IEventData {
    public String channelName;
    public Integer oldLevel;
    public Integer newLevel;

    @Override
    public EventType getEventType() { return EventType.LEVEL_UP; }
  }
}
