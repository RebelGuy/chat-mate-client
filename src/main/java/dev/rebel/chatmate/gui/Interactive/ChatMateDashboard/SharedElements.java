package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ElementFactory;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.hud.Colour;

public class SharedElements {
  public static ElementFactory<LabelElement> ERROR_LABEL = (context, parent) -> new LabelElement(context, parent)
      .setOverflow(TextOverflow.SPLIT)
      .setMaxLines(4)
      .setColour(Colour.RED)
      .setAlignment(TextAlignment.LEFT)
      .setFontScale(0.75f)
      .setSizingMode(SizingMode.FILL)
      .setVisible(false)
      .cast();
}
