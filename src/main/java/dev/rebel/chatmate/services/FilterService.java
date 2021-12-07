package dev.rebel.chatmate.services;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.services.util.TextUtilityService;
import dev.rebel.chatmate.services.util.TextUtilityService.WordFilter;
import dev.rebel.chatmate.services.util.TextUtilityService.StringMask;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static dev.rebel.chatmate.services.util.TextUtilityService.getAllOccurrences;

public class FilterService
{
  private final TextUtilityService textUtilityService;
  private final char censorChar;
  private final WordFilter[] filtered;
  private final WordFilter[] whitelisted;

  public FilterService(TextUtilityService textUtilityService, char censorChar, String filterFile) throws Exception {
    this.textUtilityService = textUtilityService;
    this.censorChar = censorChar;

    try {
      InputStream stream = ChatMate.class.getResourceAsStream(filterFile);
      // this is so dumb...
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      String[] lines = reader.lines()
        .filter(str -> !str.startsWith("#"))
        .flatMap(FilterService::parseLine)
        .toArray(String[]::new);
      this.filtered = Arrays.stream(lines)
        .filter(str -> !str.startsWith("+"))
        .map(l -> this.textUtilityService.new WordFilter(l))
        .toArray(WordFilter[]::new);
      this.whitelisted = Arrays.stream(lines)
        .filter(str -> str.startsWith("+"))
        .map(str -> str.substring(1))
          .map(l -> this.textUtilityService.new WordFilter(l))
          .toArray(WordFilter[]::new);
    } catch (Exception e) {
      throw new Exception("Could not instantiate FilterService: " + e.getMessage());
    }
  }

  public String censorNaughtyWords(@Nonnull String text) {
    // this is a somewhat naive implementation that is easy to bypass.
    // spaces and punctuation are not treated specially, and it does 
    // allow for wildcard characters and whitelisted words.

    // might have multiple matches, so instead create a mask of censored characters.
    // initialise to all 0 (yuck!)
    StringMask profanityMask = this.filterWords(text, this.filtered);
    StringMask whitelistMask = this.filterWords(text, this.whitelisted);
    return applyMask(profanityMask.subtract(whitelistMask), text, this.censorChar);
  }

  // returns the mask matching the given words (not case sensitive).
  public StringMask filterWords(@Nonnull String text, WordFilter... words) {
    StringMask mask = this.textUtilityService.new StringMask(text.length());
    text = text.toLowerCase();

    for (WordFilter word: words) {
      ArrayList<Integer> occurrences = getAllOccurrences(text, word);
      occurrences.forEach(occ -> mask.updateRange(occ, word.length, true));
    }

    return mask;
  }

  private static String applyMask(StringMask mask, String text, char censorChar) {
    char[] chars = text.toCharArray();
    mask.forEach((v, i) -> chars[i] = v ? censorChar : chars[i]);
    return new String(chars);
  }

  private static Stream<String> parseLine(String line) {
    // match '/' or ',' (note that the regex doesn't work when including the starting and ending '/'. thanks java)
    String[] split = line == null ? new String[0] : line.split("[,/]");
    return Arrays.stream(split).map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty());
  }
}
