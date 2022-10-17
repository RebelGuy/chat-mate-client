package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Memoiser;
import dev.rebel.chatmate.util.TextHelpers;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class ComplexDisplayTextInputElement extends BlockElement {
  private final Memoiser memoiser;
  private final TextInputElement textInput;
  private final UserVariablesListElement userVariablesListElement;

  public ComplexDisplayTextInputElement(InteractiveContext context, IElement parent, Runnable onSwitchToSimpleMode) {
    super(context, parent);
    this.memoiser = new Memoiser();

    this.textInput = new TextInputElement(context, this)
        .setTextUnsafe("Counter value: {{x}}")
        .setTextFormatter(this::formatText)
        .setTabIndex(0)
        .setAutoFocus(true)
        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        .setMinWidth(gui(10))
        .cast();

    IElement textElement = new InlineElement(context, this)
        .addElement(new LabelElement(context, this)
            .setText("Text:")
            .setOverflow(TextOverflow.TRUNCATE)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setMargin(RectExtension.fromRight(gui(4)))
        ).addElement(this.textInput)
        .addElement(new IconButtonElement(context, this)
            .setImage(Asset.GUI_TEXT_ICON)
            .setOnClick(onSwitchToSimpleMode)
            .setBorderCornerRadius(gui(2))
            .setMaxWidth(gui(12))
            .setPadding(new RectExtension(gui(1)))
            .setMargin(RectExtension.fromLeft(gui(4)))
            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        ).setAllowShrink(true)
        .setMargin(RectExtension.fromBottom(gui(5)));

    this.userVariablesListElement = new UserVariablesListElement(context, this)
        .setMinWidth(gui(50))
        .cast();

    IElement variablesElement = new InlineElement(context, this)
        .addElement(new LabelElement(context, this)
            .setText("Variables:")
            .setOverflow(TextOverflow.TRUNCATE)
            .setMargin(RectExtension.fromRight(gui(4)).top(gui(3)))
        ).addElement(this.userVariablesListElement)
        .setAllowShrink(true)
        .setMargin(RectExtension.fromBottom(gui(5)));

    super.addElement(textElement);
    super.addElement(variablesElement);
  }

  // this highlights variables and the brackets surrounding them
  private List<Tuple2<String, Font>> formatText(String text) {
    return this.memoiser.memoise("formatText", () -> {
      Font normalFont = new Font();
      Font bracketFont = new Font().withColour(Colour.GREY50).withItalic(true);
      Font variableFont = new Font().withColour(Colour.GREEN).withItalic(true);

      List<Tuple2<Integer, String>> userVariables = this.extractUserVariables(text);

      // {{xy}}
      // a b c d
      // a: bracketStart
      // b: variableStart
      // c: variableEnd
      // d: bracketEnd
      List<Integer> variablesStart = Collections.map(userVariables, var -> var._1);
      List<Integer> variablesEnd = Collections.map(userVariables, var -> var._1 + var._2.length());
      List<Integer> bracketsStart = Collections.map(variablesStart, i -> i - 2);
      List<Integer> bracketsEnd = Collections.map(variablesEnd, i -> i + 2);
      List<Tuple2<String, Font>> result = new ArrayList<>();

      String currentChunk = "";
      for (int i = 0; i <= text.length(); i++) {
        // the ordering of these branches is important - don't change it
        if (bracketsEnd.contains(i)) {
          result.add(new Tuple2<>(currentChunk, bracketFont));
          currentChunk = "";
        }
        if (bracketsStart.contains(i)) {
          // this marks the end of the in-between-variables text (this may be an empty string, but that's fine!
          result.add(new Tuple2<>(currentChunk, normalFont));
          currentChunk = "";
        }
        if (variablesStart.contains(i)) {
          // this marks the end of the opening brackets
          result.add(new Tuple2<>(currentChunk, bracketFont));
          currentChunk = "";
        }
        if (variablesEnd.contains(i)) {
          result.add(new Tuple2<>(currentChunk, variableFont));
          currentChunk = "";
        }

        if (i < text.length()) {
          currentChunk += text.charAt(i);
        } else {
          result.add(new Tuple2<>(currentChunk, normalFont));
        }
      }

      return Collections.filter(result, r -> r._1.length() > 0);
    }, text); // todo: also memoise on the user variables
  }

  /** E.g. from "a{{xy}}b" returns (3, "xy") */
  private List<Tuple2<Integer, String>> extractUserVariables(String text) {
    return this.memoiser.memoise("extractUserVariables", () -> {
      List<Integer> openingBrackets = TextHelpers.getAllOccurrences(text, new TextHelpers.WordFilter("{{"), false);
      List<Integer> closingBrackets = TextHelpers.getAllOccurrences(text, new TextHelpers.WordFilter("}}"), false);

      boolean isVariableActive = false;
      String currentVariable = "";
      List<Tuple2<Integer, String>> result = new ArrayList<>();
      for (int i = 0; i <= text.length(); i++) {
        if (closingBrackets.contains(i) && isVariableActive) {
          // end of variable
          isVariableActive = false;
          result.add(new Tuple2<>(i - currentVariable.length(), currentVariable));
          currentVariable = "";
        }

        if (i < text.length() && isVariableActive) {
          currentVariable += text.charAt(i);
        }

        int i_ = i; // fml
        if (openingBrackets.contains(i - 1) && !isVariableActive && Collections.any(closingBrackets, cb -> cb > i_)) {
          // we have just entered the brackets
          isVariableActive = true;
        }
      }

      return result;
    }, text);
  }

  private boolean isValid() {
    // todo: check that all extracted variables are also defined by the user, otherwise return false
    return !Collections.any(this.extractUserVariables(this.textInput.getText()), var -> var._2.length() == 0);
  }
}
