package dev.rebel.chatmate.gui.chat;

import com.google.common.collect.Iterators;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.util.ChatComponentStyle.createDeepCopyIterator;

/** Represents a container that contains another replaceable component and data. Used for modifying existing chat lines
 * and associating custom data with chat components.
 * Note that, if the contents have been replaced with a different component instance, you must refresh the chat. */
public class ContainerChatComponent implements IChatComponent {
  private @Nonnull IChatComponent component;
  private @Nullable Object data;

  /** Draws an empty line by default. */
  public ContainerChatComponent() { this.component = new ChatComponentText(""); }

  public ContainerChatComponent(@Nonnull IChatComponent component) {
    this.component = component;
  }

  public ContainerChatComponent(@Nonnull IChatComponent component, @Nullable Object data) {
    this.component = component;
    this.data = data;
  }

  public void setComponent(@Nonnull IChatComponent component) {
    this.component = component;
  }

  /** Recursively gets the underlying component until finding the one that is NOT a ContainerChatComponent. */
  public IChatComponent getComponent() {
    if (this.component == this) {
      // infinite recursion? we don't do that here
      throw new RuntimeException("ContainerChatComponent cannot hold a component reference to itself.");
    }

    if (this.component instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)this.component;
      return container.getComponent();
    } else {
      return this.component;
    }
  }

  public void setData(@Nullable Object data) {
    this.data = data;
  }

  public @Nullable Object getData() {
    return this.data;
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
  public IChatComponent createCopy() { return new ContainerChatComponent(this.component.createCopy(), this.data); }

  @Override
  public Iterator<IChatComponent> iterator() {
    return Iterators.concat(Iterators.forArray(this), createDeepCopyIterator(this.getSiblings()));
  }
  //endregion
}
