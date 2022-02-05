package dev.rebel.chatmate.models.chatMate;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetEventsResponse extends ApiResponseBase {
  public Long timestamp;
  public Event[] events;

  @Override
  public Number GetExpectedSchema() {
    return 1;
  }

  public static class Event {
    public EventType type;
    public Long timestamp;
    public Object data; // this is a LinkedTreeMap when original parsing the JSon

    public <Data extends IEventData> Data getData(Class<Data> dataClass) {
      try {
        Data data = new Gson().fromJson(this.data.toString(), dataClass);
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
