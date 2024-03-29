package dev.rebel.chatmate.gui.Interactive.CounterModal;

// supports +, -, and * of integers, including negatives and grouping via parentheses. good luck extending this to floating-point numbers and division :)

import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.EnumHelpers;
import net.minecraft.world.chunk.Chunk;
import scala.Tuple2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class Expression {
  /** DisplayExpression should be a string template that can reference variables. */
  public static Function<Integer, String> createDisplayFunction(String displayExpression, List<Tuple2<String, String>> variableDefinitions) {
    return x -> {
      String xString = String.format("%d", x);
      String result = displayExpression.replace("{{x}}", xString);
      List<Tuple2<String, String>> variableValues = new ArrayList<>();
      variableValues.add(new Tuple2<>("x", xString));

      int i = 0;
      for (Tuple2<String, String> variable : variableDefinitions) {
        String variableName = variable._1;
        String variableExpression = variable._2;
        int value = evaluateExpression(variableExpression, Collections.list(variableDefinitions.subList(0, i)), x);
        variableValues.add(new Tuple2<>(variableName, String.format("%d", value)));

        String variableString = String.format("{{%s}}", variableName);
        String valueString = String.format("%d", value);
        result = result.replace(variableString, valueString);
        i++;
      }

      return result;
    };
  }

  /** Assumes that a variable "x" is defined implicitly, whose value will be set when calling this function.
   * All other variables must be defined using valid expressions, and a variable's expression can only use variables that have already been defined beforehand in the provided list. */
  public static int evaluateExpression(String text, List<Tuple2<String, String>> variableDefinitions, int xValue) {
    variableDefinitions.add(0, new Tuple2<>("x", String.format("%d", xValue))); // inject x

    Map<String, String> expandedVariables = new HashMap<>();
    for (Tuple2<String, String> variable : Collections.reverse(variableDefinitions)) {
      for (String existingVariable : expandedVariables.keySet()) {
        String variableString = String.format("{{%s}}", variable._1);
        String expandedVariable = String.format("(%s)", variable._2);
        expandedVariables.put(existingVariable, expandedVariables.get(existingVariable).replace(variableString, expandedVariable));
      }

      expandedVariables.put(variable._1, variable._2);
    }

    for (String variable : expandedVariables.keySet()) {
      String variableString = String.format("{{%s}}", variable);
      String expandedVariable = String.format("(%s)", expandedVariables.get(variable));
      text = text.replace(variableString, expandedVariable);
    }

    // at this point, there are no more variables.
    List<Chunk> parsedChunks = parseChunks(text);

    // wrap multiplication inside brackets
    parsedChunks = wrapMultiplications(parsedChunks);

    return evaluateNumericExpression(parsedChunks);
  }

  /** Returns the next group, which is either a single number or a list of chunks wrapped in brackets. */
  private static List<Chunk> getNextGroupedValue(List<Chunk> chunks) {
    int bracketDepth = 0;
    List<Chunk> result = new ArrayList<>();
    for (Chunk chunk : chunks) {
      result.add(chunk);
      if (chunk.type == ChunkType.GROUP_START) {
        bracketDepth++;
      } else if (chunk.type == ChunkType.GROUP_END) {
        bracketDepth--;
      }

      if (bracketDepth == 0) {
        break;
      }
    }

    if (bracketDepth > 0) {
      throw new RuntimeException("Cannot get next grouped value because brackets in the given list of chunks were not balanced");
    }

    return result;
  }

  private static List<Chunk> wrapMultiplications(List<Chunk> chunks) {
    List<Chunk> result = new ArrayList<>();
    for (int i = 0; i < chunks.size(); i++) {
      Chunk chunk = chunks.get(i);
      if (Objects.equals(chunk.string, "*")) {
        List<Chunk> leftGroup = reverse(getNextGroupedValue(reverse(result)));
        List<Chunk> rightGroup = getNextGroupedValue(chunks.subList(i + 1, chunks.size()));
        result.add(result.size() - leftGroup.size(), new Chunk(ChunkType.GROUP_START, "("));
        result.add(chunk);
        result.addAll(wrapMultiplications(rightGroup));
        result.add(new Chunk(ChunkType.GROUP_END, ")"));
        i += rightGroup.size();
      } else {
        result.add(chunk);
      }
    }

    return result;
  }

  private static List<Chunk> reverse(List<Chunk> chunks) {
    return Collections.reverse(Collections.map(chunks, c -> {
      if (c.type == ChunkType.GROUP_START) {
        return new Chunk(ChunkType.GROUP_END, c.string);
      } else if (c.type == ChunkType.GROUP_END) {
        return new Chunk(ChunkType.GROUP_START, c.string);
      } else {
        return c;
      }
    }));
  }

  private static int evaluateNumericExpression(List<Chunk> chunks) {
    // ok so I KNOW this can be done much more elegantly using a binary tree (I could do it on paper), but I don't know how to generalise it into code

    // evaluate outermost brackets (there may be multiple, e.g. `(1 + 1) + (1 + 1)`). we will be left with an expression that can be evaluated from left to right
    int bracketDepth = 0;
    List<Chunk> currentBracketContents = new ArrayList<>();
    @Nullable Integer result = null; // accumulator
    @Nullable String operator = null;
    for (int i = 0; i < chunks.size(); i++) {
      Chunk chunk = chunks.get(i);
      @Nullable Integer chunkNumber = null;

      // collect or evaluate brackets
      if (chunk.type == ChunkType.GROUP_START) {
        bracketDepth++;
        if (bracketDepth > 1) {
          currentBracketContents.add(chunk);
        }
        continue;
      } else if (chunk.type == ChunkType.GROUP_END) {
        bracketDepth--;
        if (bracketDepth == 0) {
          // replace the chunk with the result of the group
          chunk = new Chunk(ChunkType.NUMBER, String.format("%d", evaluateNumericExpression(currentBracketContents)));
          currentBracketContents.clear();
        } else {
          currentBracketContents.add(chunk);
          continue;
        }
      } else if (bracketDepth > 0) {
        currentBracketContents.add(chunk);
        continue;
      }

      // set operator state
      if (chunk.type == ChunkType.OPERATOR) {
        if (operator != null) {
          throw new RuntimeException("Operator state was not reset before encountering the next operator");
        }
        operator = chunk.string;

      // initialise result or set chunk number
      } else if (chunk.type == ChunkType.NUMBER) {
        if (result == null) {
          if (operator != null) {
            throw new RuntimeException("Result state was not yet set but operator state was already set");
          }
          result = Integer.parseInt(chunk.string);
        } else {
          if (operator == null) {
            throw new RuntimeException("Operator state was not set when attempting to operate a number on the result");
          }
          chunkNumber = Integer.parseInt(chunk.string);
        }
      }

      // perform arithmetic
      if (chunkNumber != null) {
        switch (operator) {
          case "+":
            result += chunkNumber;
            break;
          case "-":
            result -= chunkNumber;
            break;
          case "*":
            result *= chunkNumber;
            break;
          default:
            throw new RuntimeException("Invalid operator " + operator);
        }
        operator = null;
      }
    }

    if (result == null) {
      throw new RuntimeException("Result is null");
    }
    return result;
  }

  /** Does not handle variables. */
  private static List<Chunk> parseChunks(String text) {
    text = text.replace(" ", "");
    if (text.startsWith("-")) {
      text = "0" + text;
    }

    List<Chunk> chunks = new ArrayList<>();
    String currentChunk = "";
    ChunkType currentType = null;
    for (char c : text.toCharArray()) {
      if (currentType == null) {
        currentChunk += c;
        currentType = Chunk.getType(c);
        continue;
      }

      if (!Chunk.isCompatible(currentType, currentChunk, c)) {
        @Nullable Chunk prevChunk = Collections.last(chunks);
        if (prevChunk != null && (prevChunk.type == ChunkType.OPERATOR || prevChunk.type == ChunkType.GROUP_START) && currentType == ChunkType.OPERATOR) {
          if (currentChunk.equals("+") || currentChunk.equals("-")) {
            // convert the operator into a number
            if (Character.isDigit(c)) {
              currentType = ChunkType.NUMBER;
              currentChunk += c;
              continue;

            // multiply the group by -1 explicitly
            } else if (c == '(') {
              chunks.add(new Chunk(ChunkType.NUMBER, currentChunk + "1"));
              chunks.add(new Chunk(ChunkType.OPERATOR, "*"));
              currentChunk = "" + c;
              currentType = Chunk.getType(c);
              continue;
            }
          } else {
            throw new RuntimeException("Cannot have two sequential operators");
          }
        }

        chunks.add(new Chunk(currentType, currentChunk));
        currentChunk = "";
        currentType = Chunk.getType(c);
      }

      currentChunk += c;
    }

    if (currentChunk.length() > 0) {
      chunks.add(new Chunk(currentType, currentChunk));
    }

    return chunks;
  }

  public static class Chunk {
    public final ChunkType type;
    public final String string;

    public Chunk(ChunkType type, @Nonnull String string) {
      if (type == ChunkType.OPERATOR) {
        if (string.equals("++") || string.equals("--")) {
          string = "+";
        } else if (string.equals("-+") || string.equals("+-")) {
          string = "-";
        } else if (string.length() > 1) {
          throw new RuntimeException("Invalid operator string " + string);
        }
      }

      this.type = type;
      this.string = string;
    }

    public static ChunkType getType(char c) {
      if (Character.isDigit(c)) {
        return ChunkType.NUMBER;
      } else if (c == '(') {
        return ChunkType.GROUP_START;
      } else if (c == ')') {
        return ChunkType.GROUP_END;
      } else if (c == '+' || c == '-' || c == '*') {
        return ChunkType.OPERATOR;
      } else {
        throw new RuntimeException("Unable to assign character " + c + " to chunk type");
      }
    }

    public static boolean isCompatible(ChunkType currentType, String currentChunk, char c) {
      switch (currentType) {
        case NUMBER:
          return Character.isDigit(c);
        case GROUP_START:
        case GROUP_END:
        case OPERATOR:
          return false;
        default:
          throw EnumHelpers.<ChunkType>assertUnreachable(currentType);
      }
    }

    @Override
    public String toString() {
      return this.string;
    }
  }

  public enum ChunkType {
    NUMBER,
    GROUP_START,
    GROUP_END,
    OPERATOR
  }
}
