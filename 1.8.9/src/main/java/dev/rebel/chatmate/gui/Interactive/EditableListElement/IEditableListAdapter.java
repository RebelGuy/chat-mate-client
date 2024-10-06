package dev.rebel.chatmate.gui.Interactive.EditableListElement;

import dev.rebel.chatmate.gui.Interactive.IElement;

public interface IEditableListAdapter<TItem, TListElement extends IElement> {
    TItem onCreateItem(int newIndex);

    // note: you can set a minimum width on the contents to allow them to shrink nicely into the available box for the list item.
    TListElement onCreateContents(TItem fromItem, int forIndex);
    void onItemAdded(TItem addedItem, int addedAtIndex);
    void onItemRemoved(TItem removedItem, int removedAtIndex);
    void onIndexUpdated(TItem item, TListElement element, int previousIndex, int newIndex);
}
