package dev.rebel.chatmate.gui.proxy;


import com.google.common.base.Predicate;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// To make our life easier, here is an annotated proxy to the net.minecraft.client.gui.GuiPageButtonList class.
// note: this is not intended to be extended. protected methods are not supposed to be called (as they are used only internally by the chain of inheritance).
// For a usage example, refer to GuiCustomizeWorldScreen.java

@SideOnly(Side.CLIENT)
public class GuiPageButtonList
{
  private final net.minecraft.client.gui.GuiPageButtonList underlying;

  public GuiPageButtonList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, GuiPageButtonList.GuiResponder p_i45536_7_, GuiPageButtonList.GuiListEntry[]... p_i45536_8_)
  {
    // LOL at the following line
    net.minecraft.client.gui.GuiPageButtonList.GuiListEntry[][] entries = Arrays.stream(p_i45536_8_).map(x -> Arrays.stream(x).map(GuiListEntry::getUnderlying).toArray(net.minecraft.client.gui.GuiPageButtonList.GuiListEntry[]::new)).toArray(net.minecraft.client.gui.GuiPageButtonList.GuiListEntry[][]::new);
    this.underlying = new net.minecraft.client.gui.GuiPageButtonList(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn, new ProxyGuiResponderToMc(p_i45536_7_), entries);
  }

  ///////////////////////////////////
  //// GuiPageButtonList methods ////
  ///////////////////////////////////
  public void func_181156_c(int p_181156_1_) { this.underlying.func_181156_c(p_181156_1_); }

  public int func_178059_e() { return this.underlying.func_178059_e(); }

  public int func_178057_f() { return this.underlying.func_178057_f(); }

  public Gui func_178056_g() { return this.underlying.func_178056_g(); }

  public void func_178071_h() { this.underlying.func_178071_h(); }

  public void func_178064_i() { this.underlying.func_178064_i(); }

  public Gui func_178061_c(int p_178061_1_) { return this.underlying.func_178061_c(p_178061_1_); }

  public void func_181155_a(boolean p_181155_1_) { this.underlying.func_181155_a(p_181155_1_); }

  public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) { return this.underlying.mouseClicked(mouseX, mouseY, mouseEvent); }

  public void func_178062_a(char p_178062_1_, int p_178062_2_) { this.underlying.func_178062_a(p_178062_1_, p_178062_2_); }

  /** Gets the IGuiListEntry object for the given index. */
  public GuiEntry getListEntry(int index) { return new GuiEntry(this.underlying.getListEntry(index)); }

  public int getSize() { return this.underlying.getSize(); }

  /** Gets the width of the list. */
  public int getListWidth() { return this.underlying.getListWidth(); }

  /////////////////////////////////
  //// GuiListExtended methods ////
  /////////////////////////////////
  public boolean mouseReleased(int p_148181_1_, int p_148181_2_, int p_148181_3_) { return this.underlying.mouseReleased(p_148181_1_, p_148181_2_, p_148181_3_); }

  /////////////////////////
  //// GuiSlot methods ////
  /////////////////////////
  public void setDimensions(int width, int height, int top, int bottom) { this.underlying.setDimensions(width, height, top, bottom); }

  public void setShowSelectionBox(boolean showSelectionBox) { this.underlying.setShowSelectionBox(showSelectionBox); }

  public int getSlotIndexFromScreenCoords(int x, int y) { return this.underlying.getSlotIndexFromScreenCoords(x, y); }

  /** Registers the IDs that can be used for the scrollbar's up/down buttons. */
  public void registerScrollButtons(int scrollUpButtonID, int scrollDownButtonID) { this.underlying.registerScrollButtons(scrollUpButtonID, scrollDownButtonID); }

  public int func_148135_f() { return this.underlying.func_148135_f(); }

  /** Returns the amountScrolled field as an integer. */
  public int getAmountScrolled() { return this.underlying.getAmountScrolled(); }

  public boolean isMouseYWithinSlotBounds(int p_148141_1_) { return this.underlying.isMouseYWithinSlotBounds(p_148141_1_); }

  /** Scrolls the slot by the given amount. A positive value scrolls down, and a negative value scrolls up. */
  public void scrollBy(int amount) { this.underlying.scrollBy(amount); }

  public void actionPerformed(GuiButton button) { this.underlying.actionPerformed(button); }

  public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_) { this.underlying.drawScreen(mouseXIn, mouseYIn, p_148128_3_); }

  public void handleMouseInput() { this.underlying.handleMouseInput(); }

  public void setEnabled(boolean enabled) { this.underlying.setEnabled(enabled); }

  public boolean getEnabled() { return this.underlying.getEnabled(); }

  /** Sets the left and right bounds of the slot. Param is the left bound, right is calculated as left + width. */
  public void setSlotXBoundsFromLeft(int left) { this.underlying.setSlotXBoundsFromLeft(left); }

  public int getSlotHeight() { return this.underlying.getSlotHeight(); }

  // GuiSlot properties:

  public void setWidth(int width) { this.underlying.width = width; }
  public int getWidth() { return this.underlying.width; }

  public void setHeight(int height) { this.underlying.height = height; }
  public int getHeight() { return this.underlying.height; }

  /** The top of the slot container. Affects the overlays and scrolling. */
  public void setTop(int top) { this.underlying.top = top; }
  /** The top of the slot container. Affects the overlays and scrolling. */
  public int getTop() { return this.underlying.top; }

  /** The bottom of the slot container. Affects the overlays and scrolling. */
  public void setBottom(int bottom) { this.underlying.bottom = bottom; }
  /** The bottom of the slot container. Affects the overlays and scrolling. */
  public int getBottom() { return this.underlying.bottom; }

  public void setRight(int right) { this.underlying.right = right; }
  public int getRight() { return this.underlying.right; }

  public void setLeft(int left) { this.underlying.left = left; }
  public int getLeft() { return this.underlying.left; }

  public void setHeaderPadding(int headerPadding) { this.underlying.headerPadding = headerPadding; }
  public int getHeaderPadding() { return this.underlying.headerPadding; }

  ///////////////////////////////////
  //// GuiPageButtonList classes ////
  ///////////////////////////////////
  @SideOnly(Side.CLIENT)
  public static class GuiEntry implements GuiListExtended.IGuiListEntry
  {
    private final net.minecraft.client.gui.GuiPageButtonList.GuiEntry underlying;

    public GuiEntry(Gui p_i45533_1_, Gui p_i45533_2_)
    {
      this.underlying = new net.minecraft.client.gui.GuiPageButtonList.GuiEntry(p_i45533_1_, p_i45533_2_);
    }

    public GuiEntry(net.minecraft.client.gui.GuiPageButtonList.GuiEntry underlying) {
      this.underlying = underlying;
    }

    public Gui func_178022_a()
    {
      return this.underlying.func_178022_a();
    }

    public Gui func_178021_b()
    {
      return this.underlying.func_178021_b();
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
      this.underlying.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
      this.underlying.setSelected(p_178011_1_, p_178011_2_, p_178011_3_);
    }

    /** Returns true if the mouse has been pressed on this control. */
    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
    {
      return this.underlying.mousePressed(slotIndex, p_148278_2_, p_148278_3_, p_148278_4_, p_148278_5_, p_148278_6_);
    }

    /** Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
      this.underlying.mouseReleased(slotIndex, x, y, mouseEvent, relativeX, relativeY);
    }
  }

  /** The GUI screen that spawns the list items should implement this directly. */
  @SideOnly(Side.CLIENT)
  public interface GuiResponder
  {
    void func_175321_a(int p_175321_1_, boolean p_175321_2_);

    void onTick(int id, float value);

    void func_175319_a(int p_175319_1_, String p_175319_2_);

  }

  private static class ProxyGuiResponderToMc implements net.minecraft.client.gui.GuiPageButtonList.GuiResponder {
    private final GuiResponder proxyResponder;

    public ProxyGuiResponderToMc(GuiResponder proxyResponder) {
      this.proxyResponder = proxyResponder;
    }

    @Override
    public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {
      this.proxyResponder.func_175321_a(p_175321_1_, p_175321_2_);
    }

    @Override
    public void onTick(int id, float value) {
      this.proxyResponder.onTick(id, value);
    }

    @Override
    public void func_175319_a(int p_175319_1_, String p_175319_2_) {
      this.proxyResponder.func_175319_a(p_175319_1_, p_175319_2_);
    }
  }

  @SideOnly(Side.CLIENT)
  public static class GuiListEntry
  {
    private final net.minecraft.client.gui.GuiPageButtonList.GuiListEntry underlying;

    public GuiListEntry(int p_i45531_1_, String p_i45531_2_, boolean p_i45531_3_) {
      this.underlying = new net.minecraft.client.gui.GuiPageButtonList.GuiListEntry(p_i45531_1_, p_i45531_2_, p_i45531_3_);
    }

    public int func_178935_b() { return this.underlying.func_178935_b(); }

    public String func_178936_c() { return this.underlying.func_178936_c(); }

    public boolean func_178934_d() { return this.underlying.func_178934_d(); }

    public net.minecraft.client.gui.GuiPageButtonList.GuiListEntry getUnderlying() { return this.underlying; }
  }

  @SideOnly(Side.CLIENT)
  public static class GuiLabelEntry extends GuiListEntry
  {
    private net.minecraft.client.gui.GuiPageButtonList.GuiLabelEntry underlying;

    public GuiLabelEntry(int p_i45532_1_, String p_i45532_2_, boolean p_i45532_3_)
    {
      super(p_i45532_1_, p_i45532_2_, p_i45532_3_);
    }

    @Override
    public net.minecraft.client.gui.GuiPageButtonList.GuiLabelEntry getUnderlying() { return this.underlying; }
  }

  @SideOnly(Side.CLIENT)
  public static class GuiButtonEntry extends GuiListEntry
  {
    private final net.minecraft.client.gui.GuiPageButtonList.GuiButtonEntry underlying;

    public GuiButtonEntry(int p_i45535_1_, String p_i45535_2_, boolean p_i45535_3_, boolean p_i45535_4_)
    {
      super(p_i45535_1_, p_i45535_2_, p_i45535_3_);
      this.underlying = new net.minecraft.client.gui.GuiPageButtonList.GuiButtonEntry(p_i45535_1_, p_i45535_2_, p_i45535_3_, p_i45535_4_);
    }

    public boolean func_178940_a() { return this.underlying.func_178940_a(); }

    @Override
    public net.minecraft.client.gui.GuiPageButtonList.GuiButtonEntry getUnderlying() { return this.underlying; }
  }

  @SideOnly(Side.CLIENT)
  public static class GuiSlideEntry extends GuiListEntry
  {
    private final net.minecraft.client.gui.GuiPageButtonList.GuiSlideEntry underlying;

    public GuiSlideEntry(int p_i45530_1_, String p_i45530_2_, boolean p_i45530_3_, GuiSlider.FormatHelper p_i45530_4_, float p_i45530_5_, float p_i45530_6_, float p_i45530_7_)
    {
      super(p_i45530_1_, p_i45530_2_, p_i45530_3_);
      this.underlying = new net.minecraft.client.gui.GuiPageButtonList.GuiSlideEntry(p_i45530_1_, p_i45530_2_, p_i45530_3_, p_i45530_4_, p_i45530_5_, p_i45530_6_, p_i45530_7_);
    }

    public GuiSlider.FormatHelper func_178945_a() { return this.underlying.func_178945_a(); }

    public float func_178943_e() { return this.underlying.func_178943_e(); }

    public float func_178944_f() { return this.underlying.func_178944_f(); }

    public float func_178942_g() { return this.underlying.func_178942_g(); }

    @Override
    public net.minecraft.client.gui.GuiPageButtonList.GuiSlideEntry getUnderlying() { return this.underlying; }
  }

  @SideOnly(Side.CLIENT)
  public static class EditBoxEntry extends GuiListEntry
  {
    private final net.minecraft.client.gui.GuiPageButtonList.EditBoxEntry underlying;

    public EditBoxEntry(int p_i45534_1_, String p_i45534_2_, boolean p_i45534_3_, Predicate<String> p_i45534_4_)
    {
      super(p_i45534_1_, p_i45534_2_, p_i45534_3_);
      this.underlying = new net.minecraft.client.gui.GuiPageButtonList.EditBoxEntry(p_i45534_1_, p_i45534_2_, p_i45534_3_, p_i45534_4_);
    }

    public Predicate<String> func_178950_a() { return this.underlying.func_178950_a(); }

    @Override
    public net.minecraft.client.gui.GuiPageButtonList.EditBoxEntry getUnderlying() { return this.underlying; }
  }
}