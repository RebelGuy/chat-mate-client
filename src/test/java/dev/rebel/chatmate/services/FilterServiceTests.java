package dev.rebel.chatmate.services;

import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.services.util.TextHelpers;
import dev.rebel.chatmate.services.util.TextHelpers.StringMask;
import dev.rebel.chatmate.services.util.TextHelpers.WordFilter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class FilterServiceTests {
  //region Parsing tests
  @Test
  public void filterFileParsing_ignoresEmptyLines() {
    FilterFileParseResult parsed = parse("", " ", "\n");

    Assert.assertEquals(0, parsed.filtered.length);
    Assert.assertEquals(0, parsed.whitelisted.length);
  }

  @Test
  public void filterFileParsing_ignoresComments() {
    FilterFileParseResult parsed = parse("#test", " # test", "#");

    Assert.assertEquals(0, parsed.filtered.length);
    Assert.assertEquals(0, parsed.whitelisted.length);
  }

  @Test
  public void filterFileParsing_respectsSplitLineCharacters() {
    FilterFileParseResult parsed = parse("first/second", "third / fourth / fifth", "another ,,one");

    Assert.assertEquals(0, parsed.whitelisted.length);

    Assert.assertArrayEquals(new String[] { "first", "second", "third", "fourth", "fifth", "another", "one" }, Arrays.stream(parsed.filtered).map(w -> w.word).toArray());
  }

  @Test
  public void filterFileParsing_detectsWhitelistedWords() {
    FilterFileParseResult parsed = parse("first", "+second, seconds");

    Assert.assertArrayEquals(new String[] { "first" }, Arrays.stream(parsed.filtered).map(w -> w.word).toArray());

    Assert.assertArrayEquals(new String[] { "second", "seconds" }, Arrays.stream(parsed.whitelisted).map(w -> w.word).toArray());
  }

  @Test
  public void filterFileParsing_detectsMatchBeginningEnd() {
    FilterFileParseResult parsed = parse("first", "+[second, seconds]", "[third]");

    Assert.assertArrayEquals(new String[] { "first", "third" }, Arrays.stream(parsed.filtered).map(w -> w.word).toArray());
    Assert.assertArrayEquals(new Boolean[] { false, true }, Arrays.stream(parsed.filtered).map(w -> w.startOnly).toArray());
    Assert.assertArrayEquals(new Boolean[] { false, true }, Arrays.stream(parsed.filtered).map(w -> w.endOnly).toArray());

    Assert.assertArrayEquals(new String[] { "second", "seconds" }, Arrays.stream(parsed.whitelisted).map(w -> w.word).toArray());
    Assert.assertArrayEquals(new Boolean[] { true, false }, Arrays.stream(parsed.whitelisted).map(w -> w.startOnly).toArray());
    Assert.assertArrayEquals(new Boolean[] { false, true }, Arrays.stream(parsed.whitelisted).map(w -> w.endOnly).toArray());
  }
  //endregion


  //region Finding algorithm tests
  @Test
  public void filter_findsWord() {
    StringMask mask = FilterService.filterWords("Hello there, world!", new WordFilter("the"));

    assertMatch("Hello ***re, world!", mask);
  }

  @Test
  public void filter_findsWordContainingPartial() {
    // test a specific issue that was fixed last week during "holidays"
    StringMask mask = FilterService.filterWords("Rebel Rebel Guy", new WordFilter("Rebel Guy"));

    assertMatch("Rebel *********", mask);
  }

  @Test
  public void filter_findsMultipleOverlappingInstances() {
    // without considering overlapping, there would only be a single match here
    // however, with overlapping, there are three matches.
    StringMask mask = FilterService.filterWords("aaaaa", new WordFilter("aaa"));

    assertMatch("*****", mask);
  }

  @Test
  public void filter_findsMultipleSeparateWords() {
    StringMask mask = FilterService.filterWords("Hello there, world!", TextHelpers.makeWordFilters("world", "hello"));

    assertMatch("***** there, *****!", mask);
  }

  @Test
  public void filter_findsMultipleOverlappingWords() {
    StringMask mask = FilterService.filterWords("123456789", TextHelpers.makeWordFilters("5678", "3456"));

    assertMatch("12******9", mask);
  }

  @Test
  public void filter_findsWordMultipleTimes() {
    StringMask mask = FilterService.filterWords("test test", TextHelpers.makeWordFilters("test"));

    assertMatch("**** ****", mask);
  }

  @Test
  public void filter_doesNotFindPartial() {
    StringMask mask = FilterService.filterWords("Hello", TextHelpers.makeWordFilters("Helllo"));

    assertMatch("Hello", mask);
  }

  @Test
  public void filter_respectsWildcard() {
    StringMask mask = FilterService.filterWords("Analyze analyse", TextHelpers.makeWordFilters("Analy*e"));

    assertMatch("******* *******", mask);
  }

  @Test
  public void filter_pureWildcardFindsAll() {
    StringMask mask = FilterService.filterWords("Hello", TextHelpers.makeWordFilters("*"));

    assertMatch("*****", mask);
  }

  @Test
  public void filter_startOnlyMatchesStartOnly() {
    StringMask mask = FilterService.filterWords("Hello hell ohello ohell", TextHelpers.makeWordFilters("[Hell"));

    assertMatch("****o **** ohello ohell", mask);
  }

  @Test
  public void filter_endOnlyMatchesEndOnly() {
    StringMask mask = FilterService.filterWords("Hello hell ohello ohell", TextHelpers.makeWordFilters("Hell]"));

    assertMatch("Hello **** ohello o****", mask);
  }

  @Test
  public void filter_startAndEndOnlyMatchesExactWordOnly() {
    StringMask mask = FilterService.filterWords("Hello hell ohello ohell", TextHelpers.makeWordFilters("[Hell]"));

    assertMatch("Hello **** ohello ohell", mask);
  }

  @Test
  public void filter_clientSideWildcard_IsIgnored() {
    StringMask mask = FilterService.filterWords("F*ck", TextHelpers.makeWordFilters("Fuck"));

    assertMatch("F*ck", mask, '^');
  }

  @Test
  public void filter_skipsFormatting() {
    // as seen in https://www.youtube.com/watch?v=Ya7Wgwf3J6c :)
    StringMask mask = FilterService.filterWords("CCC please ban this fu§rck", TextHelpers.makeWordFilters("fuck"));

    assertMatch("CCC please ban this **§r**", mask);
  }

  @Test
  public void filter_skipsFormatting_DoubleSectionSign() {
    // the extracted text should not be interpreted as three letters, but four!
    StringMask mask = FilterService.filterWords("Te§§st", TextHelpers.makeWordFilters("test"));

    assertMatch("**§§**", mask);
  }

  @Test
  public void filter_skipsFormatting_SingleTrailingSectionSign() {
    // the extracted text should not be interpreted as three letters, but four!
    StringMask mask = FilterService.filterWords("§", TextHelpers.makeWordFilters("test"));

    assertMatch("§", mask);
  }
  //endregion


  //region Censor tests
  @Test
  public void filter_whitelistOverridesCensor() {
    // this should match when appearing as part of a word, but not if being the actual word
    FilterService service = new FilterService(TextHelpers.makeWordFilters("[Hell", "Hell]"), TextHelpers.makeWordFilters("[Hell]"));
    String actualCensored = service.censorNaughtyWords("Hello hell ohello ohell");

    Assert.assertEquals("****o hell ohello o****", actualCensored);
  }

  @Test
  public void filter_whitelistOverridesCensor_WhenOverlapping() {
    FilterService service = new FilterService(TextHelpers.makeWordFilters("bcd"), TextHelpers.makeWordFilters("cdef"));
    String actualCensored = service.censorNaughtyWords("abcdef");

    Assert.assertEquals("a*cdef", actualCensored);
  }
  //endregion
  


  private static FilterFileParseResult parse(String... lines) {
    return FilterService.parseFilterFile(Arrays.stream(lines));
  }

  private static void assertMatch(String expectedMaskSelection, StringMask mask) {
    assertMatch(expectedMaskSelection, mask, '*');
  }
  private static void assertMatch(String expectedMaskSelection, StringMask mask, char matchChar) {
    Assert.assertEquals(expectedMaskSelection.length(), mask.length);

    char[] chars = expectedMaskSelection.toCharArray();
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < chars.length; i++) {
      if (mask.mask[i] && chars[i] != matchChar) {
        builder.append("Expected match at index " + i + "\n");
      } else if (!mask.mask[i] && chars[i] == matchChar) {
        builder.append("Did not expect match at index " + i + "\n");
      }
    }

    Assert.assertEquals("", builder.toString());
  }
}
