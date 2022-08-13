package dev.rebel.chatmate.services.util;

import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.util.TextHelpers.StringMask;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.*;
import java.util.Collections;
import java.util.Objects;

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

  public static String stringWithWidth(FontRenderer renderer, String msg, String truncationSuffix, char paddingChar, int maxWidth) {
    int currentWidth = renderer.getStringWidth(msg);
    if (currentWidth <= maxWidth) {
      int padWidth = renderer.getCharWidth(paddingChar);
      int paddingRequired = (maxWidth - currentWidth) / padWidth;
      String padding = String.join("", Collections.nCopies(paddingRequired, String.valueOf(paddingChar)));
      return msg + padding;

    } else {
      int truncationWidth = renderer.getStringWidth(truncationSuffix);
      return renderer.trimStringToWidth(msg, maxWidth - truncationWidth) + truncationSuffix;
    }
  }

  public static char getThinnestCharacter(FontRenderer renderer) {
    char hairSpace = '\u200a'; // ?
    char thinSpace = '\u2009'; // ?
    char punctuationSpace = '\u2008'; // 2?
    char brailleBlank = '\u2800'; // space for blind people
    char dot = 'ᐧ'; // 2
    char space = ' '; // 4 - worst-case scenario

    return dot;
  }

  public static class ClickEventWithCallback extends ClickEvent {
    private final static Action IDENTIFIER_ACTION = Action.RUN_COMMAND;
    private final static String IDENTIFIER_VALUE = "ChatMate Custom Click Event";

    private final LogService logService;
    private Runnable callback;
    private final boolean singleUse;

    public ClickEventWithCallback(LogService logService, Runnable callback, boolean singleUse) {
      super(IDENTIFIER_ACTION, IDENTIFIER_VALUE);
      this.logService = logService;
      this.callback = callback;
      this.singleUse = singleUse;
    }

    public ChatStyle bind(ChatStyle style) {
      return style.setChatClickEvent(this);
    }

    public static boolean isClickEventWithCallback(ClickEvent event) {
      return event.getAction() == IDENTIFIER_ACTION && Objects.equals(event.getValue(), IDENTIFIER_VALUE);
    }

    /** Returns true if the click was handled successfully. */
    public boolean handleClick() {
      if (this.callback == null) {
        return false;
      } else {
        boolean success;
        try {
          this.callback.run();
          success = true;
        } catch (Exception e) {
          this.logService.logError(this, e);
          success = false;
        }

        if (this.singleUse) {
          this.callback = null;
        }
        return success;
      }
    }

    public boolean isClickable() {
      return this.callback != null;
    }
  }
}
