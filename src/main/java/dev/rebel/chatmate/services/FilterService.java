package dev.rebel.chatmate.services;

import dev.rebel.chatmate.ChatMate;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public class FilterService
{
  private final char censorChar;
  private final FilteredWord[] filtered;
  private final FilteredWord[] whitelisted;

  public FilterService(char censorChar, String filterFile) throws Exception {
    this.censorChar = censorChar;

    try {
      InputStream stream = ChatMate.class.getResourceAsStream(filterFile);
      // this is so dumb...
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      String[] lines = reader.lines()
        .flatMap(FilterService::parseLine)
        .filter(str -> !str.startsWith("#"))
        .toArray(String[]::new);
      this.filtered = Arrays.stream(lines)
        .filter(str -> !str.startsWith("+"))
        .map(FilteredWord::new)
        .toArray(FilteredWord[]::new);
      this.whitelisted = Arrays.stream(lines)
        .filter(str -> str.startsWith("+"))
        .map(str -> str.substring(1))
        .map(FilteredWord::new)
        .toArray(FilteredWord[]::new);
    } catch (Exception e) {
      throw new Exception("Could not instantiate FilterService: " + e.getMessage());
    }
  }

  public String filterNaughtyWords(@Nonnull String text) {
    // this is a somewhat naive implementation that is easy to bypass.
    // spaces and punctuation are not treated specially, and it does 
    // allow for wildcard characters and whitelisted words.

    // might have multiple matches, so instead create a mask of censored characters.
    // initialise to all 0 (yuck!)
    Integer[] mask = Arrays.stream(new Integer[text.length()]).map(b -> 0).toArray(Integer[]::new);
    String _text = text.toLowerCase();

    for (FilteredWord word: this.filtered) {
      ArrayList<Integer> occurrences = getAllOccurrences(_text, word);
      occurrences.forEach(occ -> bulkMaskUpdate(mask, occ, word.length, x -> x + 1));
    }

    for (FilteredWord word: this.whitelisted) {
      ArrayList<Integer> occurrences = getAllOccurrences(_text, word);
      occurrences.forEach(occ -> bulkMaskUpdate(mask, occ, word.length, x -> 0));
    }

    String result = applyMask(mask, text, this.censorChar);
    return result;
  }

  private static ArrayList<Integer> getAllOccurrences(String text, FilteredWord word) {
    int startAt = 0;
    ArrayList<Integer> occurrences = new ArrayList<>();

    do {
      int nextIndex = indexOf(text, word, startAt);
      if (nextIndex == -1) {
        break;
      } else {
        occurrences.add(nextIndex);
        // there could be overlap so start looking at the very next character
        startAt = nextIndex + 1;
      }
    } while (true);

    return occurrences;
  }

  // custom implementation of String.indexOf that allows for wildcard matches
  private static int indexOf(String text, FilteredWord word, int startAt) {
    if (text.length() - startAt < word.length) {
      return -1;
    }

    char[] wordChars = word.word.toCharArray();
    char[] textChars = text.toCharArray();

    int charIndex = 0;
    boolean startOfWord = true;
    boolean endOfWord = true;
    for (int i = startAt; i < text.length(); i++) {
      if ((textChars[i] == wordChars[charIndex] || textChars[i] == '*')) {
        // found the next character

        if (word.startOnly && charIndex == 0 && !isStartOfWord(textChars, startAt, i)
          || word.endOnly && charIndex == wordChars.length - 1 && !isEndOfWord(textChars, i)) {
          // we found a match, but it's at an invalid location
          charIndex = 0;
        } else {
          if (charIndex == wordChars.length - 1) {
            return i - charIndex;
          } else {
            charIndex++;
          }
        }
      } else {
        // reset search
        charIndex = 0;
      }
    }

    return -1;
  }

  private static boolean isEndOfWord(char[] text, int i) {
    return i == text.length - 1 || !isSpaceOrPunctuation(text[i]) && isSpaceOrPunctuation(text[i + 1]);
  }

  private static boolean isStartOfWord(char[] text, int startAt, int i) {
    return i == startAt || isSpaceOrPunctuation(text[i - 1]) && !isSpaceOrPunctuation(text[i]);
  }

  private static boolean isSpaceOrPunctuation(char c) {
    return c == ' ' || c == '.' || c == ',' || c == '-';
  }

  // applies the updater function to the mask for a consecutive number of elements
  private static void bulkMaskUpdate(Integer[] mask, Integer start, Integer count, Function<Integer, Integer> updater) {
    for (int i = start; i < start + count; i++) {
      mask[i] = updater.apply(mask[i]);
    }
  }

  private static String applyMask(Integer[] mask, String text, char censorChar) {
    char[] chars = text.toCharArray();

    for (int i = 0; i < mask.length; i++) {
      if (mask[i] > 0) {
        chars[i] = censorChar;
      }
    }

    return new String(chars);
  }

  private static Stream<String> parseLine(String line) {
    // match '/' or ',' (note that the regex doesn't work when including the starting and ending '/'. thanks java)
    String[] split = line == null ? new String[0] : line.split("[,/]");
    return Arrays.stream(split).map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty());
  }

  private class FilteredWord {
    public final int length;
    public final boolean startOnly;
    public final boolean endOnly;
    public final String word;

    FilteredWord (String word) {
      boolean startOnly = false;
      boolean endOnly = false;
      if (word.startsWith("[")) {
        startOnly = true;
        word = word.substring(1);
      }
      if (word.endsWith("]")) {
        endOnly = true;
        word = word.substring(0, word.length() - 1);
      }

      this.length = word.length();
      this.startOnly = startOnly;
      this.endOnly = endOnly;
      this.word = word;
    }
  }
}
