package dev.rebel.chatmate.services.util;

import org.junit.Assert;
import org.junit.Test;

import dev.rebel.chatmate.services.util.TextHelpers.ExtractedFormatting;
import dev.rebel.chatmate.services.util.TextHelpers.ExtractedFormatting.Format;

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
}
