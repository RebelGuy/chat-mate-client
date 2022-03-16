package dev.rebel.chatmate.services.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.rebel.chatmate.gui.chat.ComponentHelpers;
import dev.rebel.chatmate.services.util.TextHelpers.ExtractedFormatting.Format;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class TextHelpers {
  // custom implementation of String.indexOf that allows for a custom matching filter
  public static int indexOf(String text, WordFilter word, int startAt) {
    if (text.length() - startAt < word.length) {
      return -1;
    }

    char[] wordChars = word.word.toCharArray();
    char[] textChars = text.toCharArray();

    int charIndex = 0;
    for (int i = startAt; i < text.length(); i++) {
      int initialCharIndex = charIndex;

      if ((textChars[i] == wordChars[charIndex] || wordChars[charIndex] == '*')) {
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

      if (initialCharIndex > 0 && charIndex == 0) {
        // if we stopped searching for a word, we essentially disregarded any of the last `charIndex` characters
        // as possible candidates for the beginning of the actual match, so we have to rewind the index.
        // e.g. this will come up when finding the word 'abaa' in the text 'ababaa'
        i -= initialCharIndex; // resume search from the very character after the previous word snippet started
      }
    }

    return -1;
  }

  // if findOverlaps is true, searching 'aa' in 'aaa' returns two matches (0, 1), otherwise it returns one match (0).
  public static ArrayList<Integer> getAllOccurrences(String text, WordFilter word, boolean findOverlaps) {
    int startAt = 0;
    ArrayList<Integer> occurrences = new ArrayList<>();

    while (true) {
      int nextIndex = indexOf(text, word, startAt);
      if (nextIndex == -1) {
        break;
      } else {
        occurrences.add(nextIndex);
        if (findOverlaps) {
          startAt = nextIndex + 1;
        } else {
          startAt = nextIndex + word.length;
        }
      }
    }

    return occurrences;
  }

  public static WordFilter[] makeWordFilters(String... words) {
    return Arrays.stream(words).map(WordFilter::new).toArray(WordFilter[]::new);
  }

  public static ExtractedFormatting extractFormatting(String text) {
    WordFilter filter = new WordFilter("§*");
    Format[] formats = getAllOccurrences(text, filter, false)
      .stream()
      .map(i -> new Format(i, text.charAt(i + 1)))
      .toArray(Format[]::new);

    // append the sections between formats
    StringBuilder unformattedString = new StringBuilder();
    int from = 0;
    for (int i = 0; i < formats.length; i++) {
      int to = formats[i].index;
      unformattedString.append(text.substring(from, to));
      from = to + 2;
    }

    // append the rest
    if (from < text.length()) {
      unformattedString.append(text.substring(from));
    }

    return new ExtractedFormatting(unformattedString.toString(), formats);
  }

  public static List<String> splitText(String text, int maxWidth, FontRenderer font) {
    // simply use the component splitText method, using a ChatComponentText as a wrapper around the string that is to be split
    List<IChatComponent> components = ComponentHelpers.splitText(new ChatComponentText(text), maxWidth, font);
    return components.stream().map(IChatComponent::getFormattedText).collect(Collectors.toList());
  }

  public static String float2Str(float num) {
    String str = String.format("%.2f", num);
    return trimRight(trimRight(str, '0'), '.');
  }

  public static String trimRight(String text, char toTrim) {
    while (text.length() > 0 && text.charAt(text.length() - 1) == toTrim) {
      text = text.substring(0, text.length() - 1);
    }
    return text;
  }

  private static boolean isEndOfWord(char[] text, int i) {
    return i == text.length - 1 || !isWordSeparator(text[i]) && isWordSeparator(text[i + 1]);
  }

  private static boolean isStartOfWord(char[] text, int startAt, int i) {
    return i == startAt || isWordSeparator(text[i - 1]) && !isWordSeparator(text[i]);
  }

  /** Whether the given character is commonly used to separate one word from another, or mark the beginning/end of a word.
   * This includes spaces, punctuations, and some special characters. */
  private static boolean isWordSeparator(char c) {
    return c == ' ' || c == '.' || c == ',' || c == '!' || c == '?' || c == '’' || c == '\''
        || c == '-' || c == '/' || c == ':' || c == ';'
        || c == '§' // § only comes up when there is a single § at the end of the message
        || c == '@'; // special case - in legitimate cases, it is either used to prefix a word or separate multiple words
  }

  // note: a static nested class does NOT mean that the nested class itself is static (lol), it just
  // means that it does not require an instance of the outer class in order to be instantiated.
  public static class WordFilter {
    public final int length;
    public final boolean startOnly;
    public final boolean endOnly;
    public final String word;

    public WordFilter (String word) {
      word = word.toLowerCase();

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

  public static class StringMask {
    public final boolean[] mask;
    public final int length;

    public StringMask(int length) {
      this.mask = new boolean[length];
      Arrays.fill(this.mask, false);
      this.length = length;
    }

    public StringMask(boolean[] mask) {
      this.mask = mask;
      this.length = mask.length;
    }

    public StringMask copy() {
      return new StringMask(this.mask.clone());
    }

    public boolean any() {
      return findIndex(true) != -1;
    }

    public boolean all() {
      return findIndex(false) == -1;
    }

    public void invert() {
      this.map(v -> !v);
    }

    public StringMask subtract(StringMask mask) {
      return this.map((v, i) -> mask.mask[i] ? false : v);
    }

    public StringMask add(StringMask mask) {
      return this.map((v, i) -> mask.mask[i] ? true : v);
    }

    public StringMask insert(int from, int length, boolean value) {
      int N = this.length + length;
      int to = from + length;
      boolean[] mask = new boolean[N];

      for (int i = 0; i < N; i++) {
        if (i < from) {
          mask[i] = this.mask[i];
        } else if (i >= from && i < to) {
          mask[i] = value;
        } else {
          mask[i] = this.mask[i - length];
        }
      }

      return new StringMask(mask);
    }

    public void updateRange(int from, int length, boolean value) {
      int to = from + length - 1;
      this.update((v, i) -> (i < from || i > to) ? v : value);
    }

    public void forEach(BiConsumer<Boolean, Integer> fn) {
      for (int i = 0; i < this.length; i++) {
        fn.accept(this.mask[i], i);
      }
    }

    private void update(Function<Boolean, Boolean> updateFn) {
      this.update((v, i) -> updateFn.apply(v));
    }
    private void update(BiFunction<Boolean, Integer, Boolean> updateFn) {
      for (int i = 0; i < this.length; i++) {
        this.mask[i] = updateFn.apply(this.mask[i], i);
      }
    }

    private StringMask map(Function<Boolean, Boolean> mapFn) {
      return this.map((v, i) -> mapFn.apply(v));
    }
    private StringMask map(BiFunction<Boolean, Integer, Boolean> mapFn) {
      boolean[] mask = new boolean[this.length];

      for (int i = 0; i < this.length; i++) {
        mask[i] = mapFn.apply(this.mask[i], i);
      }

      return new StringMask(mask);
    }

    private int findIndex(boolean value) {
      for (int i = 0; i < this.length; i++) {
        if (this.mask[i] == value) {
          return i;
        }
      }
      return -1;
    }
  }

  public static class ExtractedFormatting {
    public final String unformattedText;
    public final Format[] extracted;

    public ExtractedFormatting (String unformattedText, Format[] extracted) {
      this.unformattedText = unformattedText;
      this.extracted = extracted;
    }

    public static class Format {
      public final int index;
      public final char formatChar;

      public Format(int index, char formatChar) {
        this.index = index;
        this.formatChar = formatChar;
      }
    }
  }
}
