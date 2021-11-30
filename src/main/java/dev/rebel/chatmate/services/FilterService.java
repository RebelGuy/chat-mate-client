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
  private final String[] filteredStrings;
  private final String[] whitelistedStrings;

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
      this.filteredStrings = Arrays.stream(lines)
        .filter(str -> !str.startsWith("+"))
        .toArray(String[]::new);
      this.whitelistedStrings = Arrays.stream(lines)
        .filter(str -> str.startsWith("+"))
        .map(str -> str.substring(1))
        .toArray(String[]::new);
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

    for (String word: this.filteredStrings) {
      ArrayList<Integer> occurrences = getAllOccurrences(_text, word);
      occurrences.forEach(occ -> bulkMaskUpdate(mask, occ, word.length(), x -> x + 1));
    }

    for (String word: this.whitelistedStrings) {
      ArrayList<Integer> occurrences = getAllOccurrences(_text, word);
      occurrences.forEach(occ -> bulkMaskUpdate(mask, occ, word.length(), x -> 0));
    }

    String result = applyMask(mask, text, this.censorChar);
    return result;
  }

  private static ArrayList<Integer> getAllOccurrences(String text, String word) {
    int startAt = 0;
    ArrayList<Integer> occurrences = new ArrayList<>();

    do {
      int nextIndex = indexOf(text, word, startAt);
      if (nextIndex == -1) {
        break;
      } else {
        occurrences.add(nextIndex);
        // there could be overlap so start looking at the next character
        startAt++;
      }
    } while (true);

    return occurrences;
  }

  // custom implementation of String.indexOf that allows for wildcard matches
  private static int indexOf(String text, String word, int startAt) {
    if (text.length() - startAt < word.length()) {
      return -1;
    }

    char[] wordChars = word.toCharArray();
    char[] textChars = text.toCharArray();

    int charIndex = 0;
    for (int i = startAt; i < text.length(); i++) {
      if (textChars[i] == wordChars[charIndex] || textChars[i] == '*') {
        if (charIndex == wordChars.length - 1) {
          return i - charIndex;
        }
        
        charIndex++;
      } else {
        // reset search
        charIndex = 0;
      }
    }

    return -1;
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
}
