package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.CheckboxInputElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ElementFactory;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.style.Colour;

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

  /** Light-themed checkbox. */
  public static ElementFactory<CheckboxInputElement> CHECKBOX_LIGHT = (context, parent) -> new CheckboxInputElement(context, parent)
      .setCheckboxBorderColour(Colour.GREY75)
      .setSizingMode(SizingMode.FILL)
      .setMargin(new Layout.RectExtension(context.dimFactory.fromGui(0), context.dimFactory.fromGui(2)))
      .cast();
}
