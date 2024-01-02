package dev.rebel.chatmate.gui.Interactive.EditableListElement;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.util.Collections;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class EditableListElement<TItem, TListElement extends IElement> extends BlockElement implements IEditableListAdapter<TItem, TListElement> {
  private final List<Tuple2<TItem, ListItemElement<TItem, TListElement>>> itemElements;
  private final IEditableListAdapter<TItem, TListElement> adapter;

  public EditableListElement(InteractiveScreen.InteractiveContext context, IElement parent, IEditableListAdapter<TItem, TListElement> adapter) {
    super(context, parent);

    this.adapter = adapter;
    this.itemElements = new ArrayList<>();
  }

  // we can't add the first item in the constructor, otherwise the adapter methods won't be able to know about the current instance yet
  @Override
  public void onInitialise() {
    super.onInitialise();
    this.onItemAdded(this.adapter.onCreateItem(0), 0);
  }

  public List<TItem> getItems() {
    return Collections.map(this.itemElements, ie -> ie._1);
  }

  public List<ListItemElement<TItem, TListElement>> getElements() {
    return Collections.map(this.itemElements, ie -> ie._2);
  }

  @Override
  public TItem onCreateItem(int newIndex) {
    return this.adapter.onCreateItem(newIndex);
  }

  @Override
  public TListElement onCreateContents(TItem fromItem, int fromIndex) {
    return this.adapter.onCreateContents(fromItem, fromIndex);
  }

  @Override
  public void onItemAdded(TItem addedItem, int addedAtIndex) {
    if (this.itemElements.size() > addedAtIndex + 1) {
      this.itemElements.subList(addedAtIndex, this.itemElements.size()).forEach(item -> item._2.setIndexDelta(1));
    }

    ListItemElement<TItem, TListElement> newElement = this.createListItemElement(addedItem, addedAtIndex);
    this.itemElements.add(addedAtIndex, new Tuple2<>(addedItem, newElement));

    super.clear();
    this.itemElements.forEach(item -> super.addElement(item._2));

    this.adapter.onItemAdded(addedItem, addedAtIndex);
  }

  @Override
  public void onItemRemoved(TItem removedItem, int removedAtIndex) {
    this.itemElements.remove(removedAtIndex);
    this.itemElements.subList(removedAtIndex, this.itemElements.size()).forEach(item -> item._2.setIndexDelta(-1));

    super.clear();
    this.itemElements.forEach(item -> super.addElement(item._2));

    this.adapter.onItemRemoved(removedItem, removedAtIndex);
  }

  @Override
  public void onIndexUpdated(TItem item, TListElement element, int previousIndex, int newIndex) {
    this.adapter.onIndexUpdated(item, element, previousIndex, newIndex);
  }

  private ListItemElement<TItem, TListElement> createListItemElement(TItem item, int index) {
    TListElement contents = adapter.onCreateContents(item, index);
    return new ListItemElement<>(super.context, this, item, index, contents, this);
  }
}
