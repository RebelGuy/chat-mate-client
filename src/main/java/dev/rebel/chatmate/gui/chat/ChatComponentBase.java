package dev.rebel.chatmate.gui.chat;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import net.minecraft.util.*;

import java.util.Iterator;
import java.util.List;

import static net.minecraft.util.ChatComponentStyle.createDeepCopyIterator;

/** Represents a base implementation that can be used to delegate sibling-related methods. Stolen from `ChatComponentStyle`. */
public abstract class ChatComponentBase implements IChatComponent {
  private final List<IChatComponent> siblings = Lists.newArrayList();
  private ChatStyle style;

  public ChatComponentBase() { }

  public IChatComponent appendSibling(IChatComponent sibling) {
    sibling.getChatStyle().setParentStyle(this.getChatStyle());
    this.siblings.add(sibling);
    return this;
  }

  public List<IChatComponent> getSiblings() {
    return this.siblings;
  }

  public IChatComponent appendText(String text) {
    return this.appendSibling(new ChatComponentText(text));
  }

  public IChatComponent setChatStyle(ChatStyle chatStyle) {
    this.style = chatStyle;

    for (IChatComponent ichatcomponent : this.siblings) {
      ichatcomponent.getChatStyle().setParentStyle(this.getChatStyle());
    }

    return this;
  }

  public ChatStyle getChatStyle() {
    if (this.style == null) {
      this.style = new ChatStyle();

      for (IChatComponent ichatcomponent : this.siblings) {
        ichatcomponent.getChatStyle().setParentStyle(this.style);
      }
    }

    return this.style;
  }

  public Iterator<IChatComponent> iterator() {
    return Iterators.concat(Iterators.forArray(this), createDeepCopyIterator(this.siblings));
  }

  public final String getUnformattedText() {
    StringBuilder stringbuilder = new StringBuilder();

    for (IChatComponent ichatcomponent : this) {
      stringbuilder.append(ichatcomponent.getUnformattedTextForChat());
    }

    return stringbuilder.toString();
  }

  public final String getFormattedText() {
    StringBuilder stringBuilder = new StringBuilder();

    for (IChatComponent ichatcomponent : this) {
      stringBuilder.append(ichatcomponent.getChatStyle().getFormattingCode());
      stringBuilder.append(ichatcomponent.getUnformattedTextForChat());
      stringBuilder.append(EnumChatFormatting.RESET);
    }

    return stringBuilder.toString();
  }
}
