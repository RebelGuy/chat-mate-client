package dev.rebel.chatmate.services;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class ClipboardService {
  public ClipboardService() {

  }

  public void setClipboardString(@Nullable String string) {
    StringSelection stringselection = new StringSelection(string);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
  }

  public @Nullable String getClipboardString() {
    Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

    if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      try {
        return (String)transferable.getTransferData(DataFlavor.stringFlavor);
      } catch (Exception ignored) {
        return null;
      }
    }

    return null;
  }
}
