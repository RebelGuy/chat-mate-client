package dev.rebel.chatmate.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// most code is stolen from GuiKeyBindingList.class (for the ExtendedList, extends GuiSlot but allows per-item rendering)
// and GuiLanguage (for the GuiSlot)
public class GuiDashboardScreen extends GuiScreen {
  private final Minecraft minecraft;
  private final FontEngineProxy fontEngineProxy;
  private GuiDashboardScreen.NavigationList navigation;

  public GuiDashboardScreen(Minecraft minecraft, FontEngineProxy fontEngineProxy) {
    this.minecraft = minecraft;
    this.fontEngineProxy = fontEngineProxy;
  }

  public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
    this.drawDefaultBackground();
    this.drawCenteredString(this.fontEngineProxy, "ChatMate Dashboard", this.width / 2, 40, 16777215);
    super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
    this.navigation.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
  }

  @Override
  public void initGui() {
    super.initGui();

    // since we didn't explicitly define width and height, they default to the screen size.
    this.navigation = new NavigationList(minecraft, this.width, this.height, 20, 20, 20);
    this.navigation.registerScrollButtons(7, 8);
  }

  @Override
  protected void actionPerformed(GuiButton p_actionPerformed_1_) throws IOException {
    super.actionPerformed(p_actionPerformed_1_);
    this.navigation.actionPerformed(p_actionPerformed_1_);
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    this.navigation.handleMouseInput();
  }

  class NavigationList extends GuiListExtended { // todo: there are a couple of nice set* methods that could be useful
    private final List<IGuiListEntry> navigationEntries;

    public NavigationList(Minecraft minecraft, int width, int height, int headerSize, int footerSize, int slotHeight) {
      super(minecraft, width, height, headerSize, height - footerSize, slotHeight);

      this.navigationEntries = new ArrayList<>();
      this.navigationEntries.add(new NavigationItem("test"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
      // call mc.displayGuiScreen if required
      super.actionPerformed(button);
    }

    @Override
    protected int getSize() {
      return this.navigationEntries.size() * 10;
    }

    @Override
    protected void elementClicked(int i, boolean b, int i1, int i2) {

    }

    @Override
    protected boolean isSelected(int i) {
      return false;
    }

    protected int getContentHeight() {
      return this.getSize() * 50;
    }

    @Override
    public int getSlotIndexFromScreenCoords(int p_getSlotIndexFromScreenCoords_1_, int p_getSlotIndexFromScreenCoords_2_) {
      // will need to override if we are using custom heights
      return super.getSlotIndexFromScreenCoords(p_getSlotIndexFromScreenCoords_1_, p_getSlotIndexFromScreenCoords_2_);
    }

    @Override
    protected void drawBackground() {
      GuiDashboardScreen.this.drawDefaultBackground();
    }

    protected void drawSlot(int p_drawSlot_1_, int p_drawSlot_2_, int p_drawSlot_3_, int p_drawSlot_4_, int p_drawSlot_5_, int p_drawSlot_6_) {
      GuiDashboardScreen.this.drawCenteredString(GuiDashboardScreen.this.fontEngineProxy, "drawSlot", this.width / 2, p_drawSlot_3_ + 1, 16777215);

      // THIS is why the button wasn't rendering - we need to call this on super too, then it gets the entry, and renders the entry!!
      super.drawSlot(p_drawSlot_1_, p_drawSlot_2_, p_drawSlot_3_, p_drawSlot_4_, p_drawSlot_5_, p_drawSlot_6_);
    }

    @Override
    public IGuiListEntry getListEntry(int i) {
      return this.navigationEntries.get(0);
    }

    protected int getScrollBarX() {
      return super.getScrollBarX() + 15;
    }

    public int getListWidth() {
      return super.getListWidth() + 32;
    }
  }

  class NavigationItem implements IGuiListEntry {
    private GuiButton button;

    public NavigationItem(String text) {
      int width = 200;
      int height = 20;
      this.button = new GuiButton(0, 0, 0, width, height, text);
    }

    @Override
    public void setSelected(int i, int i1, int i2) { // unknown params, but doesn't seem to be used anyway?
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
      GuiDashboardScreen.this.fontEngineProxy.drawString("This is a rather long description to see what happens if there is overlow in the horizontal direction!", x + 90, y + slotHeight / 2 - GuiDashboardScreen.this.fontRendererObj.FONT_HEIGHT / 2, 16777215);
      this.button.xPosition = x;
      this.button.yPosition = y;
      this.button.drawButton(GuiDashboardScreen.this.minecraft, mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
      if (this.button.mousePressed(GuiDashboardScreen.this.minecraft, mouseX, mouseY)) {
        // button pressed - handle here
        return true;
      } else {
        return false;
      }
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
      // this should be done for every button
      this.button.mouseReleased(x, y);
    }
  }
}
