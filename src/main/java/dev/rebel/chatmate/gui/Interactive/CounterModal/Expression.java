package dev.rebel.chatmate.gui.Interactive.CounterModal;

// supports +, -, and * of integers. good luck extending this to floating-point numbers, brackets, and division

import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.EnumHelpers;
import net.minecraft.world.chunk.Chunk;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.*;

public class Expression {
  /** Assumes that a variable "x" is defined, whose value will be set when calling this function.
   * All other variables must be defined using valid expressions, and a variable's expression can only use variables that have already been defined beforehand in the provided list. */
  public static int evaluateExpression(String text, List<Tuple2<String, String>> variableDefinitions, int xValue) {
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
      text = text.replace(variableString, expandedVariables.get(variable));
    }

    // at this point, the expression is in terms of x.
    text = text.replace("{{x}}", String.format("%d", xValue)).replace(" ", "");

    List<Chunk> parsedChunks = parseChunks(text);

    // wrap multiplication inside brackets
    for (Chunk chunk : Collections.list(parsedChunks)) {
      if (Objects.equals(chunk.string, "*")) {
        parsedChunks.add(new Chunk(ChunkType.GROUP_START, "("));
        parsedChunks.add(chunk);
        parsedChunks.add(new Chunk(ChunkType.GROUP_END, ")"));
      } else {
        parsedChunks.add(chunk);
      }
    }

    return evaluateNumericExpression(parsedChunks);

    // go through each character
    // check if we can switch type (e.g. if the chunk is * and we have * coming up, that's not allowed - throw
    //


    // or

    // parse into chunks
    // while there are more than 1 chunks,
    // find the one with the highest priority
    // tell it to collapse the chunks around it, removing them from the list and replacing them with a single chunk (that has children)

    // or

    // given the value of the first variable x,
    // for every variable (in order, from top to bottom of hierarchy list),
    // replace the variable string with the variable value wrapped in brackets (which may contain more variables, but they will all be expanded eventually)
    // then we have a variable-less expression that we can evaluate
  }

  private static int evaluateNumericExpression(List<Chunk> chunks) {
    // construct a tree of groupings. from left to right, every time we encounter GROUP_START, we go deeper, and GROUP_END we go back up one step (recursive?)

    // simplify and check the tree. if a node has one child, this child can be moved to the nearest ancestor branch
    // if a node has an even number of children, it is invalid (because we need operators separating children)
    // otherwise, if all the children are non-groups, evaluate them from left to right (always collapse 3 into 1 until only 1 child remains).
    // eventually, the group will collapse into a single value that can be propagated upwards.

    // every child that has no further child groups can then be evaluated upwards, collapsing branches as we go, until we end with a single item. that will be the result

    // perhaps every node can have a "getValue()" method that causes it to collapse its children by also calling getValue(), then performing the operations??

    // ok so I KNOW this can be done much more elegantly using a binary tree (I could do it on paper), but I don't know how to generalise it into code

    // evaluate outermost brackets (there may be multiple, e.g. `(1 + 1) + (1 + 1)`. we will be left with an expression that can be evaluated from left to right
    int bracketDepth = 0;
    List<Chunk> currentBracketContents = new ArrayList<>();
    @Nullable Integer result = null; // accumulator
    @Nullable String operator = null;
    for (int i = 0; i < chunks.size(); i++) {
      Chunk chunk = chunks.get(i);
      @Nullable Integer chunkNumber = null;

      // collect or evaluate brackets
      if (bracketDepth > 0) {
        currentBracketContents.add(chunk);
        continue;
      }
      if (chunk.type == ChunkType.GROUP_START) {
        bracketDepth++;
      } else if (chunk.type == ChunkType.GROUP_END) {
        bracketDepth--;
        if (bracketDepth == 0) {
          chunkNumber = evaluateNumericExpression(currentBracketContents);
          currentBracketContents.clear();
        }

      // set operator state
      } else if (chunk.type == ChunkType.OPERATOR) {
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
      } else {
        throw EnumHelpers.<ChunkType>assertUnreachable(chunk.type);
      }

      // perform arithmetic
      if (chunkNumber != null) {
        if (operator == "+") {
          result += chunkNumber;
        } else if (operator == "-") {
          result -= chunkNumber;
        } else if (operator == "*") {
          result *= chunkNumber;
        } else {
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
    List<Chunk> chunks = new ArrayList<>();
    String currentChunk = "";
    ChunkType currentType = null;
    for (char c : text.toCharArray()) {
      if (currentType == null) {
        currentChunk += c;
        currentType = Chunk.getType(c);
        continue;
      }

      if (!Chunk.isCompatible(currentType, c)) {
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

    public Chunk(ChunkType type, String string) {
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

    public static boolean isCompatible(ChunkType chunkType, char c) {
      switch (chunkType) {
        case NUMBER:
          return Character.isDigit(c);
        case GROUP_START:
        case GROUP_END:
        case OPERATOR:
          return false;
        default:
          throw EnumHelpers.<ChunkType>assertUnreachable(chunkType);
      }
    }
  }

  public static class Value extends Chunk {
    public final int value;

    public Value(ChunkType type, String string, int value) {
      super(type, string);
      this.value = value;
    }
  }

  public static class Operator extends Chunk {
    private final OperatorType operatorType;

    public Operator(ChunkType type, String string, OperatorType operatorType) {
      super(type, string);
      this.operatorType = operatorType;
    }

    public int getPriority() {
      switch (this.operatorType) {
        case ADDITION:
        case SUBTRACTION:
          return 0;
        case MULTIPLICATION:
          return 1;
        default:
          throw EnumHelpers.<OperatorType>assertUnreachable(this.operatorType);
      }
    }

    public enum OperatorType {
      ADDITION, SUBTRACTION, MULTIPLICATION
    }
  }

  // for order of operations
  public static class Group extends Chunk {

    public Group(ChunkType type, String string) {
      super(type, string);
    }
  }

  public enum ChunkType {
    NUMBER,
    GROUP_START,
    GROUP_END,
    OPERATOR
  }
}
