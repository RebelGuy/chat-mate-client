package dev.rebel.chatmate.services.util;

import dev.rebel.chatmate.services.util.TextHelpers.StringMask;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatHelpers {

  /** Overwrites the style in one or more parts of the text. */
  public static Collection<IChatComponent> styledTextWithMask(String text, ChatStyle baseStyle, StringMask mask, ChatStyle maskStyle) {
    Collection<IChatComponent> collection = new ArrayList<>();
    char[] chars = text.toCharArray();

    boolean isSnippetMasked = false;
    StringBuilder snippet = new StringBuilder();

    // keep collecting chars until the mask value flips
    for (int i = 0; i <= text.length(); i++) {
      if (i == text.length() || mask.mask[i] != isSnippetMasked) {
        // flush previous
        if (snippet.length() > 0) {
          ChatStyle style = isSnippetMasked ? maskStyle : baseStyle;
          collection.add(new ChatComponentText(snippet.toString()).setChatStyle(style));

          if (i == text.length()) {
            continue;
          }
        }

        snippet = new StringBuilder();
        isSnippetMasked = !isSnippetMasked;
      }

      snippet.append(chars[i]);
    }

    return collection;
  }

  public static IChatComponent joinComponents(String joinText, List<IChatComponent> components) {
    IChatComponent result = new ChatComponentText("");

    boolean isFirst = true;
    for (IChatComponent comp: components) {
      if (!isFirst) {
        if (joinText.length() > 0) {
          result = result.appendText(joinText);
        }
      } else {
        isFirst = false;
      }

      result = result.appendSibling(comp);
    }

    return result;
  }
}
