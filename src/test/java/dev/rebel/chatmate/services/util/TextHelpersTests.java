package dev.rebel.chatmate.services.util;

import org.junit.Assert;
import org.junit.Test;

import dev.rebel.chatmate.services.util.TextHelpers.ExtractedFormatting;

public class TextHelpersTests {
  @Test
  public void extractFormatting_noFormatting() {
    ExtractedFormatting extractedFormatting = TextHelpers.extractFormatting("Hello world");

    Assert.assertEquals(0, extractedFormatting.extracted.length);
    Assert.assertEquals("Hello world", extractedFormatting.unformattedText);
  }

  @Test
  public void extractFormatting_singleInteriorFormatting() {
    ExtractedFormatting extractedFormatting = TextHelpers.extractFormatting("Hel§rlo world");

    Assert.assertEquals(1, extractedFormatting.extracted.length);
    Assert.assertEquals(3, extractedFormatting.extracted[0].index);
    Assert.assertEquals('r', extractedFormatting.extracted[0].formatChar);
    Assert.assertEquals("Hello world", extractedFormatting.unformattedText);
  }
  
  @Test
  public void extractFormatting_multipleUnconventialFormatting() {
    ExtractedFormatting extractedFormatting = TextHelpers.extractFormatting("§rHel§§lo world§§§§§");

    Assert.assertEquals(4, extractedFormatting.extracted.length);

    Assert.assertEquals(0, extractedFormatting.extracted[0].index);
    Assert.assertEquals(5, extractedFormatting.extracted[1].index);
    Assert.assertEquals(15, extractedFormatting.extracted[2].index);
    Assert.assertEquals(17, extractedFormatting.extracted[3].index);

    Assert.assertEquals('r', extractedFormatting.extracted[0].formatChar);
    Assert.assertEquals('§', extractedFormatting.extracted[1].formatChar);
    Assert.assertEquals('§', extractedFormatting.extracted[2].formatChar);
    Assert.assertEquals('§', extractedFormatting.extracted[3].formatChar);

    Assert.assertEquals("Hello world§", extractedFormatting.unformattedText);
  }

  @Test
  public void approximateDuration() {
    long second = 1000L;
    long minute = second * 60L;
    long hour = minute * 60L;
    long day = hour * 24L;

    Assert.assertEquals("0 seconds", TextHelpers.approximateDuration(0));
    Assert.assertEquals("1 second", TextHelpers.approximateDuration(second));
    Assert.assertEquals("1 minute", TextHelpers.approximateDuration(minute));
    Assert.assertEquals("1 hour", TextHelpers.approximateDuration(hour));
    Assert.assertEquals("2 hours", TextHelpers.approximateDuration(hour * 2 + minute * 59 + second * 59));
    Assert.assertEquals("1 day", TextHelpers.approximateDuration(day));
    Assert.assertEquals("1 day", TextHelpers.approximateDuration(day + hour + minute + second));
    Assert.assertEquals("2 days", TextHelpers.approximateDuration(day * 2));
  }
}
