package dev.rebel.chatmate.services;

import dev.rebel.chatmate.ChatMate;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class FilterService
{
  private final char censorChar;
  private final String[] filteredStrings;

  public FilterService(char censorChar, String filterFile) throws Exception {
    this.censorChar = censorChar;

    try {
      InputStream stream = ChatMate.class.getResourceAsStream(filterFile);
      // this is so dumb...
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

      this.filteredStrings = reader.lines().flatMap(FilterService::parseLine).toArray(String[]::new);
    } catch (Exception e) {
      throw new Exception("Could not instantiate filter FilterService: " + e.getMessage());
    }
  }

  public String filterNaughtyWords(@Nonnull String text) {
    // this is a very naive implementation, and does not allow for wildcard characters,
    // special treatment of spaces or punctuation, etc - it's trivial to bypass.

    // might have multiple matches, so instead create a mask of censored characters.
    // initialise to all 0 (yuck!)
    Integer[] mask = Arrays.stream(new Integer[text.length()]).map(b -> 0).toArray(Integer[]::new);
    String _text = text.toLowerCase();

    for (String word: this.filteredStrings) {
      ArrayList<Integer> occurrences = getAllOccurrences(_text, word);
      occurrences.forEach(occ -> addToMask(mask, occ, word.length()));
    }

    String result = applyMask(mask, text, this.censorChar);
    return result;
  }

  private static ArrayList<Integer> getAllOccurrences(String text, String word) {
    int startAt = 0;
    ArrayList<Integer> occurrences = new ArrayList<>();

    do {
      int nextIndex = text.indexOf(word, startAt);
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

  private static void addToMask(Integer[] mask, Integer start, Integer count) {
    for (int i = start; i < start + count; i++) {
      mask[i]++;
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
