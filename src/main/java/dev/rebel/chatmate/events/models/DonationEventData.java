package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.events.models.DonationEventData.In;
import dev.rebel.chatmate.events.models.DonationEventData.Options;
import dev.rebel.chatmate.events.models.DonationEventData.Out;

import java.util.Date;

public class DonationEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final Date date;
    public final PublicDonationData donation;

    public In(Date date, PublicDonationData donation) {
      this.date = date;
      this.donation = donation;
    }
  }

  public static class Out extends EventOut {

  }

  public static class Options extends EventOptions {

  }
}
