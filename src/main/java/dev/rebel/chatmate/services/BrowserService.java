package dev.rebel.chatmate.services;

import java.net.URI;

public class BrowserService {
  private final LogService logService;

  public BrowserService(LogService logService) {
    this.logService = logService;
  }

  // Stolen from GuiScreen.
  public void openWebLink(URI url) {
    if (url == null) {
      return;
    }

    try {
      Class<?> desktopClass = Class.forName("java.awt.Desktop");
      Object desktop = desktopClass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
      desktopClass.getMethod("browse", new Class[] { URI.class }).invoke(desktop, new Object[] { url });
    } catch (Throwable throwable) {
      this.logService.logError(String.format("Failed to open link '%s'", url), throwable);
    }
  }
}
