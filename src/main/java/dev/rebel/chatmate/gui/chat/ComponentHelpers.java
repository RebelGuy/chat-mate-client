package dev.rebel.chatmate.gui.chat;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComponentHelpers {
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
      } else {
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

    // we could fit everything
    return new Tuple2<>(result, new ArrayList<>());
  }

  private static TrimmedComponent trimComponent(IChatComponent component, int maxWidth, boolean isLineStart, FontRenderer font) {
    if (component instanceof ContainerChatComponent) {
      return trimComponent((ContainerChatComponent)component, maxWidth, isLineStart, font);
    } else if (component instanceof ChatComponentText) {
      return trimComponent((ChatComponentText)component, maxWidth, isLineStart, font);
    } else {
      throw new RuntimeException("Unable to trim component in a container because it is of type " + component.getClass().getSimpleName());
    }
  }

  // note: this uses a slightly modified vanilla algorithm, and it is what does the actual work
  private static TrimmedComponent trimComponent(ChatComponentText component, int maxWidth, boolean isLineStart, FontRenderer font) {
    ChatStyle style = component.getChatStyle().createShallowCopy();

    String fullText = component.getUnformattedTextForChat();
    String text = fullText;
    String leftOver = "";

    int newLine = text.indexOf('\n');
    if (newLine >= 0) {
      text = fullText.substring(0, newLine);
      leftOver = fullText.substring(newLine + 1);
    }

    int textWidth = font.getStringWidth(text);
    if (textWidth > maxWidth) {
      String maybeText = font.trimStringToWidth(text, maxWidth);
      String maybeLeftover = maybeText.length() < text.length() ? text.substring(maybeText.length()) : "";

      // try to split between words
      if (maybeLeftover.length() > 0) {
        int lastSpaceIndex = maybeText.lastIndexOf(" ");

        if (lastSpaceIndex >= 0 && font.getStringWidth(text.substring(0, lastSpaceIndex)) > 0) {
          maybeText = text.substring(0, lastSpaceIndex);
          maybeLeftover = text.substring(lastSpaceIndex + 1);
        } else if (!text.contains(" ") && !isLineStart) {
          // word is too long to fit - move everything to the next component as long as we are not starting the line
          maybeText = "";
          maybeLeftover = text;
        }
      }

      text = maybeText;
      leftOver = maybeLeftover + leftOver;
    }

    int actualWidth = font.getStringWidth(text);
    ChatComponentText trimmedComponent = new ChatComponentText(text);
    trimmedComponent.setChatStyle(style);

    ChatComponentText leftoverComponent = null;
    if (leftOver.length() > 0) {
      leftoverComponent = new ChatComponentText(leftOver);
      leftoverComponent.setChatStyle(style);
    }

    return new TrimmedComponent(trimmedComponent, actualWidth, leftoverComponent);
  }

  private static TrimmedComponent trimComponent(ContainerChatComponent component, int maxWidth, boolean isLineStart, FontRenderer font) {
    // only take the direct child, so we re-wrap the container correctly later
    IChatComponent unpackedComponent = component.component;

    // even though we are possibly destroying the unpacked component reference, it's ok because modifying the properties in ContainerChatComponent
    // requires a chat refresh already, meaning this method will be called anyway
    TrimmedComponent trimmed = trimComponent(unpackedComponent, maxWidth, isLineStart, font);
    ContainerChatComponent trimmedContainer = new ContainerChatComponent(trimmed.component, component.data);
    ContainerChatComponent leftoverContainer = trimmed.leftover == null ? null : new ContainerChatComponent(trimmed.leftover, component.data);

    return new TrimmedComponent(trimmedContainer, trimmed.trimmedWidth, leftoverContainer);
  }

  /** Data class. */
  private static class TrimmedComponent {
    /** The component that was trimmed. */
    public final IChatComponent component;

    /** The width of the trimmed component. */
    public final int trimmedWidth;

    /** The component that is leftover, if any. */
    public final @Nullable IChatComponent leftover;

    public TrimmedComponent(IChatComponent trimmed, int trimmedWidth, @Nullable IChatComponent leftover) {
      this.component = trimmed;
      this.trimmedWidth = trimmedWidth;
      this.leftover = leftover;
    }
  }
}
