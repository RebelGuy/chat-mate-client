package dev.rebel.chatmate.gui;

import com.google.common.collect.Iterators;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.FontRenderer.getFormatFromString;
import static net.minecraft.util.ChatComponentStyle.createDeepCopyIterator;

/** A single-line chat component that is used to draw children components at precise x-values. */
public class PrecisionChatComponentText implements IChatComponent {
  private final List<Tuple2<PrecisionLayout, ChatComponentText>> components;

  public PrecisionChatComponentText(List<Tuple2<PrecisionLayout, ChatComponentText>> components) {
    this.components = components;
  }

  /** Gets the effective components for a chat line. Note that any action events may be lost - they are only retained in the original components. */
  public List<Tuple2<PrecisionLayout, ChatComponentText>> getComponentsForLine(FontRenderer fontRenderer, int guiLineWidth) {
    List<Tuple2<PrecisionLayout, ChatComponentText>> list = new ArrayList<>();

    for (Tuple2<PrecisionLayout, ChatComponentText> pair : components) {
      Tuple2<PrecisionLayout, ChatComponentText> effective = this.getEffectiveComponent(pair, guiLineWidth, fontRenderer);
      list.add(effective);
    }

    return list;
  }

  /** If fullWidth is true, will use the whole layout box for the text component's hitbox, otherwise,
   * will use only the visible text region. Returns the original text component instance. */
  public @Nullable ChatComponentText getComponentAtGuiPosition(int guiPosition, int guiLineWidth, boolean fullWidth, FontRenderer fontRenderer) {
    for (Tuple2<PrecisionLayout, ChatComponentText> component : this.components) {
      int start, width;
      if (fullWidth) {
        // full layout (may include whitespace)
        start = component._1.position.getGuiValue(guiLineWidth);
        width = component._1.width.getGuiValue(guiLineWidth);
      } else {
        // truncated/positioned layout (text region only)
        Tuple2<PrecisionLayout, ChatComponentText> effective = this.getEffectiveComponent(component, guiLineWidth, fontRenderer);
        start = effective._1.position.getGuiValue(guiLineWidth);
        width = effective._1.width.getGuiValue(guiLineWidth);
      }

      int end = start + width;
      if (guiPosition >= start && guiPosition < end) {
        return component._2;
      }
    }

    return null;
  }

  /** Returns the left-aligned, truncated chat component that should be rendered to satisfy the layout of the provided component.
   * Retains all chat style properties of this component. The layout box is flush with the text. */
  private Tuple2<PrecisionLayout, ChatComponentText> getEffectiveComponent(Tuple2<PrecisionLayout, ChatComponentText> pair, int lineWidth, FontRenderer fontRenderer) {
    PrecisionLayout layout = pair._1;
    ChatComponentText component = pair._2;
    int maxWidth = layout.width.getGuiValue(lineWidth);

    String text = component.getFormattedText();
    int textWidth = fontRenderer.getStringWidth(text);

    if (textWidth > maxWidth) {
      String truncation = layout.customTruncation == null ? "…" : layout.customTruncation;
      int suffixWidth = fontRenderer.getStringWidth(truncation);
      text = listFormattedStringToWidth(component.getFormattedText(), maxWidth - suffixWidth, fontRenderer).get(0) + truncation;
      textWidth = fontRenderer.getStringWidth(text);
    }

    // at this point, the text is guaranteed to be <= maxWidth
    int originalPosition = layout.position.getGuiValue(lineWidth);
    int effectivePosition;
    if (layout.alignment == PrecisionAlignment.LEFT) {
      effectivePosition = originalPosition;
    } else if (layout.alignment == PrecisionAlignment.CENTRE) {
      effectivePosition = originalPosition + Math.round((maxWidth - textWidth) / 2.0f);
    } else if (layout.alignment == PrecisionAlignment.RIGHT) {
      effectivePosition = originalPosition + (maxWidth - textWidth);
    } else {
      throw new RuntimeException("Did not expect layout alignment " + layout.alignment);
    }

    // make sure we don't lose any style information from the parent (e.g. mouse actions)
    ChatComponentText effectiveComponent = new ChatComponentText(text);
    PrecisionLayout effectiveLayout = new PrecisionLayout(new PrecisionValue(effectivePosition), new PrecisionValue(textWidth), PrecisionAlignment.LEFT);
    return new Tuple2<>(effectiveLayout, effectiveComponent);
  }

  //region Interface methods
  @Override
  public IChatComponent setChatStyle(ChatStyle var1) { return this; } // meaningless

  @Override
  public ChatStyle getChatStyle() { return new ChatStyle(); } // meaningless

  @Override
  public IChatComponent appendText(String var1) { return this; } // meaningless

  @Override
  public IChatComponent appendSibling(IChatComponent component) { return this; } // meaningless

  @Override
  public String getUnformattedTextForChat() { return this.components.stream().map(c -> c._2.getUnformattedTextForChat()).collect(Collectors.joining()); }

  @Override
  public String getUnformattedText() { return this.components.stream().map(c -> c._2.getUnformattedText()).collect(Collectors.joining()); }

  @Override
  public String getFormattedText() { return this.components.stream().map(c -> c._2.getFormattedText()).collect(Collectors.joining()); }

  @Override
//  public List<IChatComponent> getSiblings() { return this.components.stream().map(c -> c._2).collect(Collectors.toList()); }
  public List<IChatComponent> getSiblings() { return new ArrayList<>(); } // not sure if this should be empty or not, seems to work fine this way.

  @Override
  public IChatComponent createCopy() { return this; }

  @Override
  public Iterator<IChatComponent> iterator() {
    return Iterators.concat(Iterators.forArray(this), createDeepCopyIterator(this.getSiblings()));
  }
  //endregion

  //region FontRenderer overrides
  // Note: the following overrides are required when trimming formatted strings because,
  // by default, the FontRenderer tries to split in-between words only.

  private List<String> listFormattedStringToWidth(String text, int width, FontRenderer fontRenderer) {
    return Arrays.asList(this.wrapFormattedStringToWidth(text, width, fontRenderer).split("\n"));
  }

  /** From the FontRenderer::wrapFormattedStringToWidth, except it treats spaces as just another character and ignores newlines. */
  private String wrapFormattedStringToWidth(String text, int width, FontRenderer fontRenderer) {
    int i = this.sizeStringToWidth(text, width, fontRenderer);
    if (text.length() <= i) {
      return text;
    } else {
      return text.substring(0, i);
    }
  }

  /** Same as the FontRenderer implementation, except it treats spaces as just another character. */
  private int sizeStringToWidth(String text, int width, FontRenderer fontRenderer) {
    int i = text.length();
    int j = 0;
    int k = 0;
    int l = -1;

    for(boolean flag = false; k < i; ++k) {
      char c0 = text.charAt(k);
      switch(c0) {
        case '\n':
          --k;
          break;

        default:
          j += fontRenderer.getCharWidth(c0);
          if (flag) {
            ++j;
          }
          break;
        case '§':
          if (k < i - 1) {
            ++k;
            char c1 = text.charAt(k);
            if (c1 != 'l' && c1 != 'L') {
              if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                flag = false;
              }
            } else {
              flag = true;
            }
          }
      }

      if (c0 == '\n') {
        ++k;
        l = k;
        break;
      }

      if (j > width) {
        break;
      }
    }

    return k != i && l != -1 && l < k ? l : k;
  }

  private static boolean isFormatColor(char p_isFormatColor_0_) {
    return p_isFormatColor_0_ >= '0' && p_isFormatColor_0_ <= '9' || p_isFormatColor_0_ >= 'a' && p_isFormatColor_0_ <= 'f' || p_isFormatColor_0_ >= 'A' && p_isFormatColor_0_ <= 'F';
  }
  //endregion

  public static class PrecisionLayout {
    public final PrecisionValue position;
    public final PrecisionValue width;
    public final PrecisionAlignment alignment;
    public final @Nullable String customTruncation;

    public PrecisionLayout(PrecisionValue position, PrecisionValue width, PrecisionAlignment alignment) {
      this.position = position;
      this.width = width;
      this.alignment = alignment;
      this.customTruncation = null;
    }

    public PrecisionLayout(PrecisionValue position, PrecisionValue width, PrecisionAlignment alignment, String customTruncation) {
      this.position = position;
      this.width = width;
      this.alignment = alignment;
      this.customTruncation = customTruncation;
    }
  }

  public enum PrecisionAlignment {
    LEFT, CENTRE, RIGHT
  }

  public static class PrecisionValue {
    public final @Nullable Integer guiValue;
    public final @Nullable Float relativeValue;

    public PrecisionValue(@Nonnull Integer guiValue) {
      this.guiValue = guiValue;
      this.relativeValue = null;
    }
    public PrecisionValue(@Nonnull Float relativeValue) {
      this.guiValue = null;
      this.relativeValue = relativeValue;
    }

    public int getGuiValue(int fullGuiSize) {
      if (this.guiValue != null) {
        return this.guiValue;
      } else {
        return (int)(this.relativeValue * fullGuiSize);
      }
    }

    public float getRelativeValue(int fullGuiSize) {
      if (this.relativeValue != null) {
        return this.relativeValue;
      } else {
        return (float)this.guiValue / fullGuiSize;
      }
    }
  }
}
