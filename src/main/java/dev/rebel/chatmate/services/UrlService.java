package dev.rebel.chatmate.services;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.Objects;

public class UrlService {
  private final LogService logService;

  public UrlService(LogService logService) {
    this.logService = logService;
  }

  // Stolen from GuiScreen, but greatly enhanced.
  /** Returns true if the URL was successfully opened in an external application, and false otherwise. */
  public boolean openUrl(URI url) {
    if (url == null) {
      return false;
    }

    try {
      Desktop desktop = Desktop.getDesktop();

      if (Objects.equals(url.getScheme(), "file")) {
        desktop.open(new File(url));
      } else {
        desktop.browse(url);
      }

      return true;
    } catch (Throwable throwable) {
      this.logService.logError(String.format("Failed to open link '%s'", url), throwable);
      return false;
    }
  }
}
