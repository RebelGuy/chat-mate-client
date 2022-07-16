package dev.rebel.chatmate.gui.chat;

import com.google.common.collect.Lists;
import dev.rebel.chatmate.services.util.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ComponentHelpers {
  public static String getFormattedText(ChatComponentText component) {
    // note: we can't use getFormattedText because it would also append the text of the siblings.
    // the unformattedText, however, only gets the text of this particular component, so we have to apply styling ourselves.
    String unformattedText = component.getUnformattedTextForChat();
    return component.getChatStyle().getFormattingCode() + unformattedText + EnumChatFormatting.RESET;
  }

  /** Stolen from GuiUtilRenderComponents::splitText, but fixes custom components not being retained after the split. Supports splitting of custom components.
   * Returns one empty ChatComponentText per line, with the actual components added as siblings of each line component. Does not handle PrecisionChatComponentText in any way. */
  public static List<IChatComponent> splitText(IChatComponent componentToSplit, int maxWidth, FontRenderer font) {
    List<IChatComponent> result = new ArrayList<>();

    // note: this uses the `component.iterator` to create the list of items... very sneaky
    List<IChatComponent> components =  Lists.newArrayList(componentToSplit);

    do {
      Tuple2<IChatComponent, List<IChatComponent>> line = splitNextLine(components, maxWidth, font);
      result.add(line._1);
      components = line._2;
    } while (components.size() > 0);

    return result;
  }

  /** Appends the components until reaching the given width. The first item is the resulting component with siblings, and the second is the flattened remaining components. */
  private static Tuple2<IChatComponent, List<IChatComponent>> splitNextLine(List<IChatComponent> flattenedComponents, int lineWidth, FontRenderer font) {
    IChatComponent result = null;

    // we only need to explicitly process the first line, and any subsequent lines can be split recursively
    int currentWidth = 0;
    for (int i = 0; i < flattenedComponents.size(); i++) {
      boolean isLineStart = currentWidth == 0;
      TrimmedComponent trimmed = trimComponent(flattenedComponents.get(i), lineWidth - currentWidth, isLineStart, font);
      currentWidth += trimmed.trimmedWidth;

      if (result == null) {
        result = trimmed.component;
      } else if (trimmed.component != null) {
        result.appendSibling(trimmed.component);
      }

      if (trimmed.leftover == null) {
        // the component fit in the specified width - keep going
        continue;
      } else {
        // stop here, as we can't fit any more
        List<IChatComponent> remaining = Lists.newArrayList(trimmed.leftover);
        remaining.addAll(flattenedComponents.subList(i + 1, flattenedComponents.size()));
        return new Tuple2<>(result, remaining);
      }
    }

    // we could fit everything. make sure we always return *something*
    if (result == null) {
      result = new ChatComponentText("");
    }
    return new Tuple2<>(result, new ArrayList<>());
  }

  private static TrimmedComponent trimComponent(IChatComponent component, int maxWidth, boolean isLineStart, FontRenderer font) {
    if (component instanceof ContainerChatComponent) {
      return trimComponent((ContainerChatComponent)component, maxWidth, isLineStart, font);
    } else if (component instanceof ChatComponentText) {
      return trimComponent((ChatComponentText) component, maxWidth, isLineStart, font);
    } else if (component instanceof ImageChatComponent) {
      return trimComponent((ImageChatComponent)component, maxWidth, isLineStart, font);
    } else if (component instanceof PrecisionChatComponentText && isLineStart) {
      return new TrimmedComponent(component, maxWidth, null);
    } else {
      throw new RuntimeException(String.format("Unable to trim component (isLineStart=%s) because it is of type %s", String.valueOf(isLineStart), component.getClass().getSimpleName()));
    }
  }

  // note: this uses a slightly modified vanilla algorithm, and it is what does the actual work
  private static TrimmedComponent trimComponent(ChatComponentText component, int maxWidth, boolean isLineStart, FontRenderer font) {
    ChatStyle style = component.getChatStyle().createShallowCopy();
    Function<String, String> styled = unstyled -> style.getFormattingCode() + unstyled;
    Function<String, String> unstyled = styledText -> styledText.startsWith(style.getFormattingCode()) ? styledText.substring(style.getFormattingCode().length()) : styledText;

    String fullText = TextHelpers.nonNull(component.getUnformattedTextForChat());
    String text = fullText;
    String leftOver = "";

    int newLine = text.indexOf('\n');
    if (newLine >= 0) {
      text = fullText.substring(0, newLine);
      leftOver = fullText.substring(newLine + 1);
    }

    int textWidth = font.getStringWidth(styled.apply(text));
    if (textWidth > maxWidth) {
      String maybeTextStyled = font.trimStringToWidth(styled.apply(text), maxWidth);
      String maybeText = unstyled.apply(maybeTextStyled);
      String maybeLeftover = maybeText.length() < text.length() ? text.substring(maybeText.length()) : "";

      // try to split between words
      if (maybeLeftover.length() > 0) {
        int lastSpaceIndex = maybeText.lastIndexOf(" ");

        // trim away spaces where appropriate. we can do this only because we know this is the transition of one line
        // to the next
        if (lastSpaceIndex >= 0 && font.getStringWidth(styled.apply(text.substring(0, lastSpaceIndex))) > 0) {
          maybeText = trimEnd(text.substring(0, lastSpaceIndex));
          maybeLeftover = trimStart(text.substring(lastSpaceIndex + 1));
        } else if (!text.trim().contains(" ") && !isLineStart) {
          // word is too long to fit - move everything to the next component as long as we are not starting the line
          maybeText = "";
          maybeLeftover = trimStart(text);
        }
      }

      text = maybeText;
      leftOver = maybeLeftover + leftOver;
    }

    int actualWidth = font.getStringWidth(styled.apply(text));
    ChatComponentText trimmedComponent = null;
    if (actualWidth > 0) {
      trimmedComponent = new ChatComponentText(text);
      trimmedComponent.setChatStyle(style);
    }
    
    ChatComponentText leftoverComponent = null;
    if (leftOver.length() > 0) {
      leftoverComponent = new ChatComponentText(leftOver);
      leftoverComponent.setChatStyle(style);
    }

    return new TrimmedComponent(trimmedComponent, actualWidth, leftoverComponent);
  }

  private static String trimStart(String string) {
    StringBuilder sb = new StringBuilder();
    for (char c : string.toCharArray()) {
      if (c == ' ' && sb.length() == 0) {
        continue;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String trimEnd(String string) {
    String reversedTrimmed = trimStart(new StringBuilder(string).reverse().toString());
    return new StringBuilder(reversedTrimmed).reverse().toString();
  }

  private static TrimmedComponent trimComponent(ContainerChatComponent component, int maxWidth, boolean isLineStart, FontRenderer font) {
    // only take the direct child, so we re-wrap the container correctly later
    IChatComponent unpackedComponent = component.component;

    // even though we are possibly destroying the unpacked component reference, it's ok because modifying the properties in ContainerChatComponent
    // requires a chat refresh already, meaning this method will be called anyway
    TrimmedComponent trimmed = trimComponent(unpackedComponent, maxWidth, isLineStart, font);
    IChatComponent innerComponent = trimmed.component == null ? new ChatComponentText("") : trimmed.component;
    ContainerChatComponent trimmedContainer = new ContainerChatComponent(innerComponent, component.data);
    ContainerChatComponent leftoverContainer = trimmed.leftover == null ? null : new ContainerChatComponent(trimmed.leftover, component.data);

    return new TrimmedComponent(trimmedContainer, trimmed.trimmedWidth, leftoverContainer);
  }

  private static TrimmedComponent trimComponent(ImageChatComponent component, int maxWidth, boolean isLineStart, FontRenderer font) {
    int requiredWidth = (int)Math.ceil(component.getRequiredWidth(font.FONT_HEIGHT));
    if (isLineStart || maxWidth >= requiredWidth) {
      return new TrimmedComponent(component, requiredWidth, null);
    } else {
      // add to next line
      return new TrimmedComponent(new ChatComponentText(""), requiredWidth, component);
    }
  }

  /** Data class. */
  private static class TrimmedComponent {
    /** The component that was trimmed, if any. */
    public final @Nullable IChatComponent component;

    /** The width of the trimmed component. */
    public final int trimmedWidth;

    /** The component that is leftover, if any. */
    public final @Nullable IChatComponent leftover;

    public TrimmedComponent(@Nullable IChatComponent trimmed, int trimmedWidth, @Nullable IChatComponent leftover) {
      this.component = trimmed;
      this.trimmedWidth = trimmedWidth;
      this.leftover = leftover;
    }
  }
}
