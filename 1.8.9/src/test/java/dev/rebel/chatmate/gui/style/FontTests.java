package dev.rebel.chatmate.gui.style;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FontTests {
  @Test
  public void withItalic_generatesNewFontWithItalicStyle() {
    Font baseFont = new Font();

    Font updatedFont = baseFont.withItalic(true);

    Assert.assertFalse(baseFont.getItalic());
    Assert.assertTrue(updatedFont.getItalic());
  }
}
