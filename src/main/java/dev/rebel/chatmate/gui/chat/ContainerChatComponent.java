package dev.rebel.chatmate.gui.chat;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

/** Represents a container that contains another replaceable component and data. Used for modifying existing chat lines
 * and associating custom data with chat components.
 * Note that, if the contents have been replaced with a different component instance, you must refresh the chat. */
public class ContainerChatComponent implements IChatComponent {
  public @Nonnull IChatComponent component;
  public @Nullable Object data;

  /** Draws an empty line by default. */
  public ContainerChatComponent() { this.component = new ChatComponentText(""); }

  public ContainerChatComponent(@Nonnull IChatComponent component) {
    this.component = component;
  }

  public ContainerChatComponent(@Nonnull IChatComponent component, @Nullable Object data) {
    this.component = component;
    this.data = data;
  }

  //region Interface methods
  @Override
  public IChatComponent setChatStyle(ChatStyle var1) { return this.component.setChatStyle(var1); }

  @Override
  public ChatStyle getChatStyle() { return this.component.getChatStyle(); }

  @Override
  public IChatComponent appendText(String var1) { return this.component.appendText(var1); }

  @Override
  public IChatComponent appendSibling(IChatComponent component) { return this.component.appendSibling(component); }

  @Override
  public String getUnformattedTextForChat() { return this.component.getUnformattedTextForChat(); }

  @Override
  public String getUnformattedText() { return this.component.getUnformattedText(); }

  @Override
  public String getFormattedText() { return this.component.getFormattedText(); }

  @Override
  public List<IChatComponent> getSiblings() { return this.component.getSiblings(); }

  @Override
  public IChatComponent createCopy() { return new ContainerChatComponent(this.component.createCopy()); }

  @Override
  public Iterator<IChatComponent> iterator() { return this.component.iterator(); }
  //endregion
}
