package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Memoiser;
import dev.rebel.chatmate.util.TextHelpers;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class CounterModalController {
  private final Memoiser memoiser = new Memoiser();

  // this highlights variables and the brackets surrounding them
  public List<Tuple2<String, Font>> formatText(String text, List<String> accessibleVariables) {
    return this.memoiser.memoise("formatText", () -> {
      Font normalFont = new Font();
      Font bracketFont = new Font().withColour(Colour.GREY50).withItalic(true);
      Font newlineFont = new Font().withColour(Colour.GREY25);
      Font variableFontValid = new Font().withColour(Colour.GREEN).withItalic(true);
      Font variableFontInvalid = new Font().withColour(Colour.RED).withItalic(true);

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

      boolean isWithinBrackets = false;
      String currentChunk = "";
      for (int i = 0; i <= text.length(); i++) {
        // the ordering of these branches is important - don't change it
        if (bracketsEnd.contains(i)) {
          result.add(new Tuple2<>(currentChunk, bracketFont));
          currentChunk = "";
          isWithinBrackets = false;
        }
        if (bracketsStart.contains(i)) {
          // this marks the end of the in-between-variables text (this may be an empty string, but that's fine!)
          result.add(new Tuple2<>(currentChunk, normalFont));
          currentChunk = "";
          isWithinBrackets = true;
        }
        if (variablesStart.contains(i)) {
          // this marks the end of the opening brackets
          result.add(new Tuple2<>(currentChunk, bracketFont));
          currentChunk = "";
        }
        if (variablesEnd.contains(i)) {
          result.add(new Tuple2<>(currentChunk, accessibleVariables.contains(currentChunk) ? variableFontValid : variableFontInvalid));
          currentChunk = "";
        }

        if (!isWithinBrackets && currentChunk.endsWith("\\n")) {
          result.add(new Tuple2<>(currentChunk.substring(0, currentChunk.length() - 2), normalFont));
          result.add(new Tuple2<>(currentChunk.substring(currentChunk.length() - 2), newlineFont));
          currentChunk = "";
        }

        if (i < text.length()) {
          currentChunk += text.charAt(i);
        } else {
          result.add(new Tuple2<>(currentChunk, normalFont));
        }
      }

      return Collections.filter(result, r -> r._1.length() > 0);
    }, text, accessibleVariables);
  }

  /** E.g. from "a{{xy}}b" returns (3, "xy") */
  public List<Tuple2<Integer, String>> extractUserVariables(String text) {
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

  public boolean usesInaccessibleVariables(String text, List<String> accessibleVariables) {
    List<String> variablesUsed = Collections.map(this.extractUserVariables(text), var -> var._2);
    return Collections.any(variablesUsed, var -> !accessibleVariables.contains(var));
  }
}
