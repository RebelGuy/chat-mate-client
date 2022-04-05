package dev.rebel.chatmate.gui.models;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/** Represents a ChatLine that exists only in memory but is not drawn as-is. */
@SideOnly(Side.CLIENT)
public class AbstractChatLine
{
  /** GUI Update Counter value this Line was created at */
  private final int updateCounterCreated;
  private final IChatComponent component;
  /** int value to refer to existing Chat Lines, can be 0 which means unreferrable */
  private final int chatLineID;

  public AbstractChatLine(int updateCounterCreated, IChatComponent component, int chatLineID) {
    this.component = component;
    this.updateCounterCreated = updateCounterCreated;
    this.chatLineID = chatLineID;
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
}
