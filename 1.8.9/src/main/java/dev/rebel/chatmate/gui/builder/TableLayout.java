package dev.rebel.chatmate.gui.builder;

import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction;
import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction.ButtonActionClickData;
import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction.ButtonActionType;
import dev.rebel.chatmate.gui.builder.CheckBoxLayout.CheckBoxAction;
import dev.rebel.chatmate.gui.builder.CheckBoxLayout.CheckBoxAction.CheckBoxActionClickData;
import dev.rebel.chatmate.gui.builder.CheckBoxLayout.CheckBoxAction.CheckBoxActionType;
import dev.rebel.chatmate.gui.builder.LayoutEngine.Layout;
import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction;
import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction.SliderActionMouseEventData;
import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction.SliderActionMouseEventData.MouseEventState;
import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction.SliderActionType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TableLayout extends ManagedLayout {
  private final int rowHeight;
  private final List<List<ContentLayout<?, ?>>> rows;

  public final int rowPadding;
  public final int cellPadding;
  public final int columns;
  public final int width;
  public final int left;
  public final int top;

  public TableLayout(List<GuiButton> buttonList, List<GuiLabel> labelList, int left, int top, int width, int columns, int rowHeight, int rowPadding, int cellPadding) {
    super(buttonList, labelList);
    this.rowHeight = rowHeight;
    this.rows = new ArrayList<>();

    this.rowPadding = rowPadding;
    this.cellPadding = cellPadding;
    this.columns = columns;
    this.width = width;
    this.left= left;
    this.top = top;
  }

  public int getTotalHeight() {
    return this.rows.size() * this.rowHeight + (this.rows.size() - 1) * this.rowPadding;
  }

  public int getTopForRowIndex(int i) {
    return this.top + i * (this.rowHeight + this.rowPadding);
  }

  public <TGui, TActionData, L extends ContentLayout<TGui, TActionData>> List<L> filterCells(Class<L> layoutClass, Function<L, Boolean> predicate) {
    List<L> items = new ArrayList<>();

    // nested loop because mapping/filtering wouldn't play nicely (also did you know that 60 seconds pass in africa every minute?)
    for (List<ContentLayout<?, ?>> row : this.rows) {
      for (ContentLayout<?, ?> cell : row) {
        if (layoutClass.isInstance(cell) && predicate.apply((L)cell)) {
          items.add((L)cell);
        }
      }
    }
    return items;
  }

  public <TGui, TActionData, L extends ContentLayout<TGui, TActionData>> L getLayoutForObject(Class<L> layoutClass, TGui object) {
    return this.filterCells(layoutClass, c -> c.tryGetGui() == object)
        .stream()
        .findFirst()
        .orElse(null);
  }

  public <TGui, TActionData, L extends ContentLayout<TGui, TActionData>> List<L> getAllLayoutsOfType(Class<L> layoutClass) {
    return this.filterCells(layoutClass, c -> true);
  }

  public TableLayout withRow(ContentLayout<?, ?> ...content) {
    if (content.length != this.columns) {
      throw new RuntimeException("Number of items in this row must be " + this.columns);
    }

    List<ContentLayout<?, ?>> contentList = Arrays.asList(content);
    this.rows.add(contentList);
    return this;

    // todo: this should modify the minSize and maxSize of all existing rows!
  }

  public TableLayout instantiate() {
    for (int i = 0; i < this.rows.size(); i++) {
      List<ContentLayout<?, ?>> row = this.rows.get(i);
      int top = this.getTopForRowIndex(i);

      // what the fuck
      List<Layout> horizontalLayouts = LayoutEngine.calculateLayouts(this.width, this.cellPadding, row.stream().map(ContentLayout::getWidth).toArray(String[][]::new));

      for (int j = 0; j < this.columns; j++) {
        Layout horizontalLayout = horizontalLayouts.get(j);
        ContentLayout<?, ?> layout = row.get(j);
        layout.instantiateGui(this.left + horizontalLayout.position, top, horizontalLayout.size, this.rowHeight);
      }
    }

    // add all items to the GUI item lists that were passed at instantiation, so things actually get rendered
    this.rows.forEach(r -> r.forEach(c -> {
      if (c instanceof ButtonLayout || c instanceof CheckBoxLayout || c instanceof SliderLayout) {
        GuiButton button = (GuiButton)c.tryGetGui();
        if (!this.buttonList.contains(button)) {
          this.buttonList.add(button);
        }
      } else if (c instanceof LabelLayout) {
        GuiLabel label = (GuiLabel)c.tryGetGui();
        if (!this.labelList.contains(label)) {
          this.labelList.add(label);
        }
      } else {
        throw new RuntimeException("Cannot add row because content of type " + c.getClass().getName() + " is not supported by TableLayout");
      }
    }));

    return this;
  }

  /** Returns true if the event was handled by a button */
  public boolean onActionPerformed(GuiButton button) {
    ButtonLayout buttonLayout = this.getLayoutForObject(ButtonLayout.class, button);
    if (buttonLayout != null) {
      return buttonLayout.dispatchAction(new ButtonAction(ButtonActionType.CLICK, new ButtonActionClickData()));
    }

    CheckBoxLayout checkBoxLayout = this.getLayoutForObject(CheckBoxLayout.class, button);
    if (checkBoxLayout != null) {
      return checkBoxLayout.dispatchAction(new CheckBoxAction(CheckBoxActionType.CLICK, new CheckBoxActionClickData()));
    }

    return false;
  }


  public boolean onPostMousePressed(int posX, int posY) {
    SliderActionMouseEventData data = new SliderActionMouseEventData(MouseEventState.DOWN, posX, posY);
    SliderAction action = new SliderAction(SliderActionType.MOUSE_EVENT, data);
    for (SliderLayout sliderLayout : this.getAllLayoutsOfType(SliderLayout.class)) {
      if (sliderLayout.dispatchAction(action)) {
        return true;
      }
    }

    return false;
  }

  public boolean onPostMouseDragged(int posX, int posY) {
    SliderActionMouseEventData data = new SliderActionMouseEventData(MouseEventState.MOVE, posX, posY);
    SliderAction action = new SliderAction(SliderActionType.MOUSE_EVENT, data);
    for (SliderLayout sliderLayout : this.getAllLayoutsOfType(SliderLayout.class)) {
      if (sliderLayout.dispatchAction(action)) {
        return true;
      }
    }

    return false;
  }

  public void onPostMouseReleased(int posX, int posY) {
    SliderActionMouseEventData data = new SliderActionMouseEventData(MouseEventState.UP, posX, posY);
    SliderAction action = new SliderAction(SliderActionType.MOUSE_EVENT, data);
    for (SliderLayout sliderLayout : this.getAllLayoutsOfType(SliderLayout.class)) {
      sliderLayout.dispatchAction(action);
    }
  }


  public void refreshContents() {
    this.rows.forEach(r -> r.forEach(ContentLayout::refreshContents));
  }
}
