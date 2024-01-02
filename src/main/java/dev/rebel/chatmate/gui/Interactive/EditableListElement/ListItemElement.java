package dev.rebel.chatmate.gui.Interactive.EditableListElement;

import dev.rebel.chatmate.gui.Interactive.*;

// renders the given contents with a small add/remove button next to it
public class ListItemElement<TItem, TListElement extends IElement> extends InlineElement {
  public final TItem item;
  public final TListElement contents;
  private int index;
  private final IEditableListAdapter<TItem, TListElement> adapter;

  public ListItemElement(InteractiveScreen.InteractiveContext context, IElement parent, TItem item, int index, TListElement contents, IEditableListAdapter<TItem, TListElement> adapter) {
    super(context, parent);
    this.item = item;
    this.contents = contents;
    this.index = index;
    this.adapter = adapter;

    ButtonElement.TextButtonElement deleteButton = new ButtonElement.TextButtonElement(context, this)
        .setText("+")
        .withLabelUpdated(label -> label
            .setFontScale(0.75f)
            .setPadding(new Layout.RectExtension(ZERO))
        ).setOnClick(this::onAddItem)
        .setBorder(new Layout.RectExtension(gui(0)))
        .setPadding(new Layout.RectExtension(gui(0)))
        .cast();
    ButtonElement.TextButtonElement createButton = new ButtonElement.TextButtonElement(context, this)
        .setText("-")
        .withLabelUpdated(label -> label
            .setFontScale(0.75f)
            .setPadding(new Layout.RectExtension(ZERO))
        ).setEnabled(this, this.index > 0)
        .setOnClick(this::onRemoveItem)
        .setBorder(new Layout.RectExtension(gui(0)))
        .setPadding(new Layout.RectExtension(gui(0)))
        .setMargin(Layout.RectExtension.fromTop(gui(1)))
        .cast();

    super.addElement(contents);
    super.addElement(new BlockElement(context, this)
        .addElement(createButton)
        .addElement(deleteButton)
        .setVerticalAlignment(Layout.VerticalAlignment.MIDDLE)
        .setMargin(Layout.RectExtension.fromLeft(gui(2)))
    );
    super.setAllowShrink(true);
  }

  public int getIndex() {
    return this.index;
  }

  public void setIndexDelta(int indexDelta) {
    int previousIndex = this.index;
    this.index += indexDelta;
    this.adapter.onIndexUpdated(this.item, this.contents, previousIndex, this.index);
  }

  private void onAddItem() {
    TItem newItem = this.adapter.onCreateItem(this.index + 1);
    this.adapter.onItemAdded(newItem, this.index + 1);
  }

  private void onRemoveItem() {
    this.adapter.onItemRemoved(this.item, this.index);
  }
}
