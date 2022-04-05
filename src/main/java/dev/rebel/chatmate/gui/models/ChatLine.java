package dev.rebel.chatmate.gui.models;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class ChatLine
{
  /** GUI Update Counter value this Line was created at */
  private final int updateCounterCreated;
  private final IChatComponent component;
  /** int value to refer to existing Chat Lines, can be 0 which means unreferrable */
  private final int chatLineID;
  private final AbstractChatLine parent;

  public ChatLine(int updateCounterCreated, IChatComponent component, int chatLineID, @Nonnull AbstractChatLine parent) {
    this.component = component;
    this.updateCounterCreated = updateCounterCreated;
    this.chatLineID = chatLineID;
    this.parent = parent;
  }

  public IChatComponent getChatComponent() {
    return this.component;
  }

  public int getUpdatedCounter() {
    return this.updateCounterCreated;
  }

  public int getChatLineID() {
    return this.chatLineID;
  }

  public AbstractChatLine getParent() {
    return this.parent;
  }
}
