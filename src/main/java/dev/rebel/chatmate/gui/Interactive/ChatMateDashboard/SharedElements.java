package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.StatefulEmitter;
import dev.rebel.chatmate.gui.Interactive.ButtonElement;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.CheckboxInputElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ElementFactory;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.style.Colour;

import java.util.function.BiFunction;

public class SharedElements {
  public static final float SCALE = 0.75f;

  public static ElementFactory<LabelElement> ERROR_LABEL = (context, parent) -> new LabelElement(context, parent)
      .setOverflow(TextOverflow.SPLIT)
      .setMaxOverflowLines(4)
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
      .setMargin(new RectExtension(context.dimFactory.fromGui(0), context.dimFactory.fromGui(2)))
      .cast();

  public static ElementFactory<TextButtonElement> TEXT_BUTTON_LIGHT = (context, parent) -> new TextButtonElement(context, parent)
      .setBorderColour(Colour.GREY75)
      .setBorderCornerRadius(context.dimFactory.fromGui(1))
      .setPadding(new RectExtension(context.dimFactory.fromGui(1)))
      .setMargin(new RectExtension(context.dimFactory.fromGui(0), context.dimFactory.fromGui(2)))
      .cast();

  /** Add a config option to the given checkbox element. */
  public static BiFunction<StatefulEmitter<Boolean>, CheckboxInputElement, CheckboxInputElement> CHECKBOX_WITH_CONFIG = (state, checkbox) -> checkbox
      .setChecked(state.get())
      .onCheckedChanged(state::set)
      .setScale(SCALE)
      .cast();
}
