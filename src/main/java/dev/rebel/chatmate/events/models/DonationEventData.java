package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;

import java.util.Date;

public class DonationEventData {
  public final Date date;
  public final PublicDonationData donation;

  public DonationEventData(Date date, PublicDonationData donation) {
    this.date = date;
    this.donation = donation;
  }
}
