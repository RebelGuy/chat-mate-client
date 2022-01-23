package dev.rebel.chatmate.gui.screen;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.IntHashMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPageButtonList extends GuiListExtended
{
  /** The entries (rows) on the current page. */
  private final List<GuiPageButtonList.GuiEntry> pageRows = Lists.newArrayList();
  /** All elements on the current page. */
  private final IntHashMap<Gui> elements = new IntHashMap();
  /** The text fields on the current page. */
  private final List<GuiTextField> textFields = Lists.newArrayList();
  private final GuiPageButtonList.GuiListEntry[][] allPageContents;
  private int pageIndex;
  private final GuiPageButtonList.GuiResponder responder;
  private Gui focusedElement;

  /** Draws each array of entries on a page as a list with scrollbars. Each page is divided into two columns.
   * Null is allowed if an element should be by itself in a row. If a label is by itself, it will automatically be centred.
   */
  public GuiPageButtonList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight, GuiPageButtonList.GuiResponder responder, GuiPageButtonList.GuiListEntry[]... allPageContents)
  {
    super(mc, width, height, top, bottom, slotHeight);
    this.responder = responder;
    this.allPageContents = allPageContents;
    this.field_148163_i = false;
    this.registerAllContent();
    this.registerPageRows();
  }

  /** Registers all rows, Gui elements, and text fields. */
  private void registerAllContent()
  {
    for (GuiPageButtonList.GuiListEntry[] pageContents : this.allPageContents)
    {
      for (int i = 0; i < pageContents.length; i += 2)
      {
        GuiPageButtonList.GuiListEntry leftEntry = pageContents[i];
        GuiPageButtonList.GuiListEntry rightEntry = i < pageContents.length - 1 ? pageContents[i + 1] : null;
        Gui leftElement = this.instantiateElement(leftEntry, 0, rightEntry == null);
        Gui rightElement = this.instantiateElement(rightEntry, 160, leftEntry == null);
        GuiPageButtonList.GuiEntry row = new GuiPageButtonList.GuiEntry(leftElement, rightElement);
        this.pageRows.add(row);

        if (leftEntry != null && leftElement != null)
        {
          this.elements.addKey(leftEntry.getId(), leftElement);

          if (leftElement instanceof GuiTextField)
          {
            this.textFields.add((GuiTextField)leftElement);
          }
        }

        if (rightEntry != null && rightElement != null)
        {
          this.elements.addKey(rightEntry.getId(), rightElement);

          if (rightElement instanceof GuiTextField)
          {
            this.textFields.add((GuiTextField)rightElement);
          }
        }
      }
    }
  }

  /** Registers rows for the current page. */
  private void registerPageRows()
  {
    this.pageRows.clear();

    for (int i = 0; i < this.allPageContents[this.pageIndex].length; i += 2)
    {
      GuiPageButtonList.GuiListEntry leftEntry = this.allPageContents[this.pageIndex][i];
      GuiPageButtonList.GuiListEntry rightEntry = i < this.allPageContents[this.pageIndex].length - 1 ? this.allPageContents[this.pageIndex][i + 1] : null;
      Gui gui = this.elements.lookup(leftEntry.getId());
      Gui gui1 = rightEntry != null ? this.elements.lookup(rightEntry.getId()) : null;
      GuiPageButtonList.GuiEntry row = new GuiPageButtonList.GuiEntry(gui, gui1);
      this.pageRows.add(row);
    }
  }

  public void setPageIndex(int pageIndex)
  {
    if (pageIndex != this.pageIndex)
    {
      int i = this.pageIndex;
      this.pageIndex = pageIndex;
      this.registerPageRows();
      this.onSwitchPage(i, pageIndex);
      this.amountScrolled = 0.0F;
    }
  }

  public int getCurrentPageIndex()
  {
    return this.pageIndex;
  }

  public int getPageCount()
  {
    return this.allPageContents.length;
  }

  public Gui getFocusedElement()
  {
    return this.focusedElement;
  }

  public void trySetPreviousPage()
  {
    if (this.pageIndex > 0)
    {
      this.setPageIndex(this.pageIndex - 1);
    }
  }

  public void trySetNextPage()
  {
    if (this.pageIndex < this.allPageContents.length - 1)
    {
      this.setPageIndex(this.pageIndex + 1);
    }
  }

  public Gui getElementById(int id)
  {
    return this.elements.lookup(id);
  }

  private void onSwitchPage(int oldPageIndex, int newPageIndex)
  {
    for (GuiPageButtonList.GuiListEntry entry : this.allPageContents[oldPageIndex])
    {
      if (entry != null)
      {
        this.setVisibility(this.elements.lookup(entry.getId()), false);
      }
    }

    for (GuiPageButtonList.GuiListEntry entry : this.allPageContents[newPageIndex])
    {
      if (entry != null)
      {
        this.setVisibility(this.elements.lookup(entry.getId()), true);
      }
    }
  }

  private void setVisibility(Gui element, boolean isVisible)
  {
    if (element instanceof GuiButton)
    {
      ((GuiButton)element).visible = isVisible;
    }
    else if (element instanceof GuiTextField)
    {
      ((GuiTextField)element).setVisible(isVisible);
    }
    else if (element instanceof GuiLabel)
    {
      ((GuiLabel)element).visible = isVisible;
    }
  }

  private Gui instantiateElement(GuiPageButtonList.GuiListEntry entry, int xOffset, boolean onlyElement)
  {
    if (entry instanceof GuiPageButtonList.GuiSlideEntry) {
      return this.instantiateSlider(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.GuiSlideEntry)entry);
    } else if (entry instanceof GuiPageButtonList.GuiButtonEntry) {
      return this.instantiateButton(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.GuiButtonEntry)entry);
    } else if (entry instanceof GuiPageButtonList.EditBoxEntry) {
      return this.instantiateTextBox(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.EditBoxEntry)entry);
    } else if (entry instanceof GuiPageButtonList.GuiLabelEntry) {
      return this.instantiateLabel(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.GuiLabelEntry)entry, onlyElement);
    } else {
      return null;
    }
  }

  private GuiSlider instantiateSlider(int xPosition, int yPosition, GuiPageButtonList.GuiSlideEntry entry)
  {
    GuiSlider guislider = new GuiSlider(new ProxyGuiResponderToMc(this.responder), entry.getId(), xPosition, yPosition, entry.getLabel(), entry.getMin(), entry.getMax(), entry.getDefaultValue(), entry.getFormatter());
    guislider.visible = entry.isVisible();
    return guislider;
  }

  private GuiListButton instantiateButton(int xPosition, int yPosition, GuiPageButtonList.GuiButtonEntry entry)
  {
    GuiListButton guilistbutton = new GuiListButton(this.responder, entry.getId(), xPosition, yPosition, entry.getLabel(), entry.isChecked());
    guilistbutton.visible = entry.isVisible();
    return guilistbutton;
  }

  private GuiTextField instantiateTextBox(int xPosition, int yPosition, GuiPageButtonList.EditBoxEntry entry)
  {
    GuiTextField guitextfield = new GuiTextField(entry.getId(), this.mc.fontRendererObj, xPosition, yPosition, 150, 20);
    guitextfield.setText(entry.getLabel());
    // register the GUI responder
    guitextfield.func_175207_a(new ProxyGuiResponderToMc(this.responder));
    guitextfield.setVisible(entry.isVisible());
    guitextfield.setValidator(entry.getValidator());
    return guitextfield;
  }

  private GuiLabel instantiateLabel(int xPosition, int yPosition, GuiPageButtonList.GuiLabelEntry entry, boolean centreOnRow)
  {
    GuiLabel guilabel;

    if (centreOnRow)
    {
      guilabel = new GuiLabel(this.mc.fontRendererObj, entry.getId(), xPosition, yPosition, this.width - xPosition * 2, 20, -1);
    }
    else
    {
      guilabel = new GuiLabel(this.mc.fontRendererObj, entry.getId(), xPosition, yPosition, 150, 20, -1);
    }

    guilabel.visible = entry.isVisible();
    // add the line of text to the label
    guilabel.func_175202_a(entry.getLabel());
    guilabel.setCentered();
    return guilabel;
  }

  public void setAllEnabled(boolean enabled)
  {
    for (GuiPageButtonList.GuiEntry row : this.pageRows)
    {
      if (row.first instanceof GuiButton)
      {
        ((GuiButton)row.first).enabled = enabled;
      }

      if (row.second instanceof GuiButton)
      {
        ((GuiButton)row.second).enabled = enabled;
      }
    }
  }

  public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent)
  {
    boolean clickHandled = super.mouseClicked(mouseX, mouseY, mouseEvent);
    int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);

    if (i >= 0)
    {
      GuiPageButtonList.GuiEntry row = this.getListEntry(i);

      if (this.focusedElement != row.focusedElement && this.focusedElement != null && this.focusedElement instanceof GuiTextField)
      {
        ((GuiTextField)this.focusedElement).setFocused(false);
      }

      this.focusedElement = row.focusedElement;
    }

    return clickHandled;
  }

  /** This even is available in GuiScreens. */
  public void keyTyped(char typedChar, int keyCode)
  {
    if (this.focusedElement instanceof GuiTextField)
    {
      GuiTextField guitextfield = (GuiTextField)this.focusedElement;

      if (!GuiScreen.isKeyComboCtrlV(keyCode))
      {
        if (keyCode == 15) // tab key
        {
          guitextfield.setFocused(false);
          int k = this.textFields.indexOf(this.focusedElement);

          if (GuiScreen.isShiftKeyDown())
          {
            if (k == 0)
            {
              k = this.textFields.size() - 1;
            }
            else
            {
              --k;
            }
          }
          else if (k == this.textFields.size() - 1)
          {
            k = 0;
          }
          else
          {
            ++k;
          }

          this.focusedElement = (Gui)this.textFields.get(k);
          guitextfield = (GuiTextField)this.focusedElement;
          guitextfield.setFocused(true);
          int l = guitextfield.yPosition + this.slotHeight;
          int i1 = guitextfield.yPosition;

          if (l > this.bottom)
          {
            this.amountScrolled += (float)(l - this.bottom);
          }
          else if (i1 < this.top)
          {
            this.amountScrolled = (float)i1;
          }
        }
        else
        {
          guitextfield.textboxKeyTyped(typedChar, keyCode);
        }
      }
      else
      {
        String s = GuiScreen.getClipboardString();
        String[] astring = s.split(";");
        int i = this.textFields.indexOf(this.focusedElement);
        int j = i;

        for (String s1 : astring)
        {
          ((GuiTextField)this.textFields.get(j)).setText(s1);

          if (j == this.textFields.size() - 1)
          {
            j = 0;
          }
          else
          {
            ++j;
          }

          if (j == i)
          {
            break;
          }
        }
      }
    }
  }

  /**
   * Gets the IGuiListEntry object for the given index
   */
  public GuiPageButtonList.GuiEntry getListEntry(int index)
  {
    return (GuiPageButtonList.GuiEntry)this.pageRows.get(index);
  }

  public int getSize()
  {
    return this.pageRows.size();
  }

  /**
   * Gets the width of the list
   */
  public int getListWidth()
  {
    return 400;
  }

  protected int getScrollBarX()
  {
    return super.getScrollBarX() + 32;
  }

  /** Represents a row of instantiated GUI elements. It is itself a GUI element that can be rendered. */
  @SideOnly(Side.CLIENT)
  public static class GuiEntry implements GuiListExtended.IGuiListEntry
  {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Gui first;
    private final Gui second;
    private Gui focusedElement;

    public GuiEntry(Gui first, Gui second)
    {
      this.first = first;
      this.second = second;
    }

    public Gui getFirst()
    {
      return this.first;
    }

    public Gui getSecond()
    {
      return this.second;
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
      this.drawElement(this.first, y, mouseX, mouseY, false);
      this.drawElement(this.second, y, mouseX, mouseY, false);
    }

    private void drawElement(Gui element, int yPosition, int mouseX, int mouseY, boolean selected)
    {
      if (element != null)
      {
        if (element instanceof GuiButton)
        {
          this.drawButton((GuiButton)element, yPosition, mouseX, mouseY, selected);
        }
        else if (element instanceof GuiTextField)
        {
          this.drawTextField((GuiTextField)element, yPosition, selected);
        }
        else if (element instanceof GuiLabel)
        {
          this.drawLabel((GuiLabel)element, yPosition, mouseX, mouseY, selected);
        }
      }
    }

    private void drawButton(GuiButton button, int yPosition, int mouseX, int mouseY, boolean selected)
    {
      button.yPosition = yPosition;

      if (!selected)
      {
        button.drawButton(this.mc, mouseX, mouseY);
      }
    }

    private void drawTextField(GuiTextField textField, int yPosition, boolean selected)
    {
      textField.yPosition = yPosition;

      if (!selected)
      {
        textField.drawTextBox();
      }
    }

    private void drawLabel(GuiLabel label, int yPosition, int mouseX, int mouseY, boolean selected)
    {
      label.field_146174_h = yPosition;

      if (!selected)
      {
        label.drawLabel(this.mc, mouseX, mouseY);
      }
    }

    public void setSelected(int unknown1, int unknown2, int yPosition)
    {
      // unknown1 is probably slotIndex, and unknown2 is probably xPosition
      this.drawElement(this.first, yPosition, 0, 0, true);
      this.drawElement(this.second, yPosition, 0, 0, true);
    }

    /**
     * Returns true if the mouse has been pressed on this control.
     */
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int unknown1, int unknown2)
    {
      boolean pressedFirst = this.onElementMousePressed(this.first, x, y, mouseEvent);
      boolean pressedSecond = this.onElementMousePressed(this.second, x, y, mouseEvent);
      return pressedFirst || pressedSecond;
    }

    private boolean onElementMousePressed(Gui element, int x, int y, int mouseEvent)
    {
      if (element == null)
      {
        return false;
      }
      else if (element instanceof GuiButton)
      {
        return this.onGuiButtonMousePressed((GuiButton)element, x, y, mouseEvent);
      }
      else
      {
        if (element instanceof GuiTextField)
        {
          this.onTextFieldMouseClicked((GuiTextField)element, x, y, mouseEvent);
        }

        return false;
      }
    }

    private boolean onGuiButtonMousePressed(GuiButton button, int x, int y, int mouseEvent)
    {
      boolean wasPressed = button.mousePressed(this.mc, x, y);

      if (wasPressed)
      {
        this.focusedElement = button;
      }

      return wasPressed;
    }

    private void onTextFieldMouseClicked(GuiTextField textField, int x, int y, int mouseEvent)
    {
      textField.mouseClicked(x, y, mouseEvent);

      if (textField.isFocused())
      {
        this.focusedElement = textField;
      }
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
      this.onElementMouseReleased(this.first, x, y, mouseEvent);
      this.onElementMouseReleased(this.second, x, y, mouseEvent);
    }

    private void onElementMouseReleased(Gui element, int x, int y, int mouseEvent)
    {
      if (element != null)
      {
        if (element instanceof GuiButton)
        {
          this.onGuiButtonMouseReleased((GuiButton)element, x, y, mouseEvent);
        }
      }
    }

    private void onGuiButtonMouseReleased(GuiButton guiButton, int x, int y, int mouseEvent)
    {
      guiButton.mouseReleased(x, y);
    }
  }

  /** These are merely used as data containers to later instantiate the actual GUI elements. */
  @SideOnly(Side.CLIENT)
  public static class GuiListEntry
  {
    private final int id;
    private final String label;
    private final boolean isVisible;

    public GuiListEntry(int id, String label, boolean isVisible)
    {
      this.id = id;
      this.label = label;
      this.isVisible = isVisible;
    }

    public int getId()
    {
      return this.id;
    }

    public String getLabel()
    {
      return this.label;
    }

    public boolean isVisible()
    {
      return this.isVisible;
    }
  }

  @SideOnly(Side.CLIENT)
  public static class GuiLabelEntry extends GuiPageButtonList.GuiListEntry
  {
    public GuiLabelEntry(int id, String label, boolean isVisible)
    {
      super(id, label, isVisible);
    }
  }

  @SideOnly(Side.CLIENT)
  public static class EditBoxEntry extends GuiPageButtonList.GuiListEntry
  {
    private final Predicate<String> validator;

    public EditBoxEntry(int id, String label, boolean isVisible, Predicate<String> validator)
    {
      super(id, label, isVisible);
      this.validator = (Predicate)Objects.firstNonNull(validator, Predicates.alwaysTrue());
    }

    public Predicate<String> getValidator()
    {
      return this.validator;
    }
  }

  @SideOnly(Side.CLIENT)
  public static class GuiButtonEntry extends GuiPageButtonList.GuiListEntry
  {
    private final boolean isChecked;

    public GuiButtonEntry(int id, String label, boolean isVisible, boolean isChecked)
    {
      super(id, label, isVisible);
      this.isChecked = isChecked;
    }

    public boolean isChecked()
    {
      return this.isChecked;
    }
  }

  @SideOnly(Side.CLIENT)
  public static class GuiSlideEntry extends GuiPageButtonList.GuiListEntry
  {
    private final GuiSlider.FormatHelper formatter;
    private final float min;
    private final float max;
    private final float defaultValue;

    public GuiSlideEntry(int id, String label, boolean isVisible, GuiSlider.FormatHelper formatter, float min, float max, float defaultValue)
    {
      super(id, label, isVisible);
      this.formatter = formatter;
      this.min = min;
      this.max = max;
      this.defaultValue = defaultValue;
    }

    public GuiSlider.FormatHelper getFormatter()
    {
      return this.formatter;
    }

    public float getMin()
    {
      return this.min;
    }

    public float getMax()
    {
      return this.max;
    }

    public float getDefaultValue()
    {
      return this.defaultValue;
    }
  }

  /** The GUI screen that spawns the list items should implement this directly. */
  @SideOnly(Side.CLIENT)
  public interface GuiResponder
  {
    void onButtonClick(int buttonId, boolean checked);

    void onSliderChange(int sliderId, float value);

    void onInputChange(int inputId, String text);
  }

  private static class ProxyGuiResponderToMc implements net.minecraft.client.gui.GuiPageButtonList.GuiResponder {
    private final GuiPageButtonList.GuiResponder proxyResponder;

    public ProxyGuiResponderToMc(GuiPageButtonList.GuiResponder proxyResponder) {
      this.proxyResponder = proxyResponder;
    }

    @Override
    public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {
      this.proxyResponder.onButtonClick(p_175321_1_, p_175321_2_);
    }

    @Override
    public void onTick(int id, float value) {
      this.proxyResponder.onSliderChange(id, value);
    }

    @Override
    public void func_175319_a(int p_175319_1_, String p_175319_2_) {
      this.proxyResponder.onInputChange(p_175319_1_, p_175319_2_);
    }
  }
}