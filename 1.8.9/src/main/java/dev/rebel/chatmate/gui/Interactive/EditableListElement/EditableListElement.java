package dev.rebel.chatmate.gui.Interactive.EditableListElement;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.util.Collections;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EditableListElement<TItem, TListElement extends IElement> extends BlockElement implements IEditableListAdapter<TItem, TListElement> {
  private List<Tuple2<TItem, ListItemElement<TItem, TListElement>>> itemElements;
  private final IEditableListAdapter<TItem, TListElement> adapter;
  private final @Nullable List<TItem> initialItems;

  public EditableListElement(InteractiveScreen.InteractiveContext context, IElement parent, @Nullable List<TItem> initialItems, IEditableListAdapter<TItem, TListElement> adapter) {
    super(context, parent);

    this.adapter = adapter;
    this.itemElements = new ArrayList<>();
    this.initialItems = initialItems;
  }

  // we can't add the initial items in the constructor, otherwise the adapter methods have no way yet of knowing about the current instance
  @Override
  public void onInitialise() {
    super.onInitialise();

    if (Collections.any(initialItems)) {
      Collections.forEach(initialItems, this::onItemAdded);
    }
  }

  public List<TItem> getItems() {
    return Collections.map(this.itemElements, ie -> ie._1);
  }

  public List<ListItemElement<TItem, TListElement>> getElements() {
    return Collections.map(this.itemElements, ie -> ie._2);
  }

  public void replaceItem(int index, TItem newItem) {
    this.itemElements = Collections.map(this.itemElements, (el, i) -> {
      if (i == index) {
        return new Tuple2<>(newItem, el._2);
      } else {
        return new Tuple2<>(el._1, el._2);
      }
    });
  }

  @Override
  public TItem onCreateItem(int newIndex) {
    return this.adapter.onCreateItem(newIndex);
  }

  @Override
  public TListElement onCreateContents(TItem fromItem, int forIndex) {
    return this.adapter.onCreateContents(fromItem, forIndex);
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
