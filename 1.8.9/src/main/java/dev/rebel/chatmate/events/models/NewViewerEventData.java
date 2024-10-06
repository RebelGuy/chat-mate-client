package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.event.PublicNewViewerData;

import java.util.Date;

public class NewViewerEventData {
  public final Date date;
  public final PublicNewViewerData newViewer;

  public NewViewerEventData(Date date, PublicNewViewerData newViewer) {
    this.date = date;
    this.newViewer = newViewer;
  }
}
