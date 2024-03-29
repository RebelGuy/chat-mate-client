package dev.rebel.chatmate.gui.chat;

import com.google.common.collect.Iterators;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.util.EnumHelpers;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.rebel.chatmate.util.Objects.casted;
import static dev.rebel.chatmate.util.Objects.ifClass;
import static net.minecraft.util.ChatComponentStyle.createDeepCopyIterator;

/** A single-line chat component that is used to draw children components at precise x-values. When printing to chat,
 * please make sure you print this by itself, and not as part of a sibling of another component. Otherwise, don't come crying when you get an exception. */
public class PrecisionChatComponent implements IChatComponent {
  private final List<Tuple2<PrecisionLayout, IChatComponent>> components;

  public PrecisionChatComponent(List<Tuple2<PrecisionLayout, IChatComponent>> components) {
    this.components = components;
  }

  /** Gets the effective components for a chat line. Note that any action events may be lost - they are only retained in the original components. */
  public List<Tuple2<PrecisionLayout, IChatComponent>> getComponentsForLine(FontEngine fontEngine, Dim guiLineWidth) {
    List<Tuple2<PrecisionLayout, IChatComponent>> list = new ArrayList<>();

    for (Tuple2<PrecisionLayout, IChatComponent> pair : components) {
      Tuple2<PrecisionLayout, IChatComponent> effective = this.getEffectiveComponent(pair, guiLineWidth, fontEngine);
      list.add(effective);
    }

    return list;
  }

  /** If fullWidth is true, will use the whole layout box for the text component's hitbox, otherwise,
   * will use only the visible text region. Returns the original text component instance. */
  public @Nullable IChatComponent getComponentAtGuiPosition(Dim guiPosition, Dim guiLineWidth, boolean fullWidth, FontEngine fontEngine) {
    for (Tuple2<PrecisionLayout, IChatComponent> component : this.components) {
      Dim start, width;
      if (fullWidth) {
        // full layout (may include whitespace)
        start = component._1.position;
        width = component._1.width;
      } else {
        // truncated/positioned layout (text region only)
        Tuple2<PrecisionLayout, IChatComponent> effective = this.getEffectiveComponent(component, guiLineWidth, fontEngine);
        start = effective._1.position;
        width = effective._1.width;
      }

      Dim end = start.plus(width);
      if (guiPosition.gte(start) && guiPosition.lt(end)) {
        return component._2;
      }
    }

    return null;
  }

  /** Returns the left-aligned, truncated chat component that should be rendered to satisfy the layout of the provided component.
   * Retains all chat style properties of this component. The layout box is flush with the text. */
  private Tuple2<PrecisionLayout, IChatComponent> getEffectiveComponent(Tuple2<PrecisionLayout, IChatComponent> pair, Dim lineWidth, FontEngine fontEngine) {
    PrecisionLayout layout = pair._1;
    IChatComponent component = pair._2;
    Dim maxWidth = layout.width;

    String text = component.getFormattedText();
    Dim textWidth = fontEngine.getStringWidthDim(text);
    Dim componentWidth = textWidth;

    IChatComponent unwrappedComponent = component;
    if (component instanceof ContainerChatComponent) {
      unwrappedComponent = casted(ContainerChatComponent.class, component, ContainerChatComponent::getComponent);
    }

    if (unwrappedComponent instanceof UserNameChatComponent) {
      componentWidth = casted(UserNameChatComponent.class, unwrappedComponent, c -> c.getWidth(fontEngine.FONT_HEIGHT_DIM));
    }
    Dim nonTextWidth = componentWidth.minus(textWidth);

    if (textWidth.gt(maxWidth)) {
      String truncation = layout.customTruncation == null ? "…" : layout.customTruncation;
      Dim suffixWidth = fontEngine.getStringWidthDim(truncation);
      text = listFormattedStringToWidth(component.getFormattedText(), maxWidth.minus(suffixWidth).minus(nonTextWidth), fontEngine).get(0) + truncation;
      textWidth = fontEngine.getStringWidthDim(text);
    }

    // at this point, the total width is guaranteed to be <= maxWidth
    Dim totalWidth = textWidth.plus(nonTextWidth);
    Dim originalPosition = layout.position;
    Dim effectivePosition;
    if (layout.alignment == PrecisionAlignment.LEFT) {
      effectivePosition = originalPosition;
    } else if (layout.alignment == PrecisionAlignment.CENTRE) {
      effectivePosition = originalPosition.plus(maxWidth.minus(totalWidth).over(2));
    } else if (layout.alignment == PrecisionAlignment.RIGHT) {
      effectivePosition = originalPosition.plus(maxWidth.minus(totalWidth));
    } else {
      throw EnumHelpers.<PrecisionAlignment>assertUnreachable(layout.alignment);
    }

    IChatComponent effectiveComponent;
    if (ifClass(ContainerChatComponent.class, component, c -> ifClass(UserNameChatComponent.class, c.getComponent(), null))) {
      UserNameChatComponent userNameChatComponent = casted(ContainerChatComponent.class, component, c -> casted(UserNameChatComponent.class, c.getComponent()));
      userNameChatComponent.setDisplayName(text);
      effectiveComponent = userNameChatComponent;
    } else if (ifClass(ContainerChatComponent.class, component, c -> ifClass(ImageChatComponent.class, c.getComponent(), null))) {
      effectiveComponent = casted(ContainerChatComponent.class, component, c -> casted(ImageChatComponent.class, c.getComponent()));
    } else {
      effectiveComponent = new ChatComponentText(text);
    }
    PrecisionLayout effectiveLayout = new PrecisionLayout(effectivePosition, totalWidth, PrecisionAlignment.LEFT);
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

  private List<String> listFormattedStringToWidth(String text, Dim width, FontEngine fontEngine) {
    return Arrays.asList(this.wrapFormattedStringToWidth(text, width, fontEngine).split("\n"));
  }

  /** From the FontRenderer::wrapFormattedStringToWidth, except it treats spaces as just another character and ignores newlines. */
  private String wrapFormattedStringToWidth(String text, Dim width, FontEngine fontEngine) {
    int i = this.sizeStringToWidth(text, width, fontEngine);
    if (text.length() <= i) {
      return text;
    } else {
      return text.substring(0, i);
    }
  }

  /** Same as the FontRenderer implementation, except it treats spaces as just another character. */
  private int sizeStringToWidth(String text, Dim width, FontEngine fontEngine) {
    int i = text.length();
    Dim j = width.setGui(0);
    int k = 0;
    int l = -1;

    for(boolean flag = false; k < i; ++k) {
      char c0 = text.charAt(k);
      switch(c0) {
        case '\n':
          --k;
          break;

        default:
          j = j.plus(fontEngine.getStringWidthDim("" + c0));
          if (flag) {
            j = j.plus(j.setGui(1)); // equivalent to j++
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

      if (j.gt(width)) {
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
    public final Dim position;
    public final Dim width;
    public final PrecisionAlignment alignment;
    public final @Nullable String customTruncation;

    public PrecisionLayout(Dim position, Dim width, PrecisionAlignment alignment) {
      this.position = position;
      this.width = width;
      this.alignment = alignment;
      this.customTruncation = null;
    }

    public PrecisionLayout(Dim position, Dim width, PrecisionAlignment alignment, String customTruncation) {
      this.position = position;
      this.width = width;
      this.alignment = alignment;
      this.customTruncation = customTruncation;
    }
  }

  public enum PrecisionAlignment {
    LEFT, CENTRE, RIGHT
  }
}
