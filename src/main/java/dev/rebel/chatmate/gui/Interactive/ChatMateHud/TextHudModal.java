package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.Asset.*;

public class TextHudModal extends ModalElement {
  private final TextHudStore textHudStore;

  private final TextButtonElement addButton;
  private final IElement noElementsLabel;
  private final IElement addButtonWrapper;
  private final BlockElement body;

  public TextHudModal(InteractiveContext context, InteractiveScreen parent, TextHudStore textHudStore) {
    super(context, parent);
    this.textHudStore = textHudStore;

    this.noElementsLabel = new LabelElement(context, this)
        .setText("You have not yet created any text elements.")
        .setOverflow(TextOverflow.SPLIT)
        .setMargin(RectExtension.fromBottom(gui(4)))
        .setVisible(this.textHudStore.getElements().size() == 0);
    this.addButton = new TextButtonElement(context, this)
        .setText("Add text element")
        .setOnClick(this::onAddElement)
        .setHorizontalAlignment(HorizontalAlignment.CENTRE)
        .setMargin(RectExtension.fromBottom(gui(4)))
        .cast();

    // wrap the button to make sure it's always centred in the modal, even when no text item exists in the list yet
    this.addButtonWrapper = new WrapperElement(context, this)
        .setContent(this.addButton)
        .setSizingMode(SizingMode.FILL);

    this.body = new BlockElement(context, this);
    for (TextHudElement element : this.textHudStore.getElements()) {
      this.addEditableTextHudElement(element, false);
    }

    this.body.addElement(this.noElementsLabel).addElement(this.addButtonWrapper);
  }

  private void addEditableTextHudElement(TextHudElement element, boolean isNew) {
    this.body.addElement(
        new EditableTextHudElement(super.context, this, element, isNew, this::onEditElement, this::onDeleteElement)
            .setPadding(RectExtension.fromBottom(gui(4)))
    );
  }

  private void onAddElement() {
    this.body.removeElement(this.addButtonWrapper);

    this.addEditableTextHudElement(new TextHudElement(super.context, this), true);

    this.addButton.setEnabled(this, false);
    this.body.addElement(this.addButtonWrapper); // make sure the button is on the bottom
    this.noElementsLabel.setVisible(false);
  }

  private void onDeleteElement(EditableTextHudElement element) {
    this.body.removeElement(element);
    this.textHudStore.removeElement(element.element);
    this.addButton.setEnabled(this, true);
    this.noElementsLabel.setVisible(this.textHudStore.getElements().size() == 0);
  }

  private void onEditElement(EditableTextHudElement element) {
    if (element.isNew) {
      // this is a bit awkward, but we have to create a new underlying element - updating just the parent in the element
      // factory doesn't seem to work (the element is invisible).
      // we need to make sure the element's reference in the editable element matches that of the hud store.
      TextHudElement newHudElement = this.textHudStore.addElement(TextHudElement::new);
      newHudElement.setText(element.element.getText());
      element.element = newHudElement;
    }

    this.addButton.setEnabled(this, true);
  }

  @Override
  public void onInitialise() {
    super.onInitialise();
    super.setBody(this.body);
    super.setTitle("Text Elements");
  }

  @Override
  protected @Nullable Boolean validate() {
    return null;
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    // nothing to submit
  }

  private static class EditableTextHudElement extends InlineElement {
    public TextHudElement element;
    public boolean isNew;
    private final Consumer<EditableTextHudElement> onConfirm;
    private final Consumer<EditableTextHudElement> onDelete;

    private final TextInputElement textInputElement;
    private final LabelElement labelElement;
    private final IconButtonElement confirmButton;
    private final IElement readonlyElement;
    private final IElement editingElement;

    public EditableTextHudElement(InteractiveContext context, IElement parent, TextHudElement element, boolean isNew, Consumer<EditableTextHudElement> onConfirm, Consumer<EditableTextHudElement> onDelete) {
      super(context, parent);

      this.element = element;
      this.isNew = isNew;
      this.onConfirm = onConfirm;
      this.onDelete = onDelete;

      this.labelElement = new LabelElement(context, this)
          .setOverflow(TextOverflow.TRUNCATE)
          .setText(this.element.getText())
          .setMinWidth(gui(50))
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .setSizingMode(SizingMode.FILL)
          .cast();
      this.textInputElement = new TextInputElement(context, this)
          .setPlaceholder("Add text")
          .setTextUnsafe(this.element.getText())
          .onTextChange(this::onEditedTextChanged)
          .useDefaultTextFormatter()
          .setMinWidth(gui(50))
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .cast();

      Dim iconWidth = context.fontEngine.FONT_HEIGHT_DIM;
      Dim buttonMargin = gui(2);
      IconButtonElement editButton = new IconButtonElement(context, this)
          .setImage(GUI_PEN_ICON)
          .setTooltip("Edit text element")
          .setOnClick(this::onEdit)
          .setMaxContentWidth(iconWidth)
          .setMargin(RectExtension.fromLeft(buttonMargin))
          .cast();
      IconButtonElement deleteButton = new IconButtonElement(context, this)
          .setImage(GUI_BIN_ICON)
          .setTooltip("Delete text element")
          .setOnClick(this::onDelete)
          .setMaxContentWidth(iconWidth)
          .setMargin(RectExtension.fromLeft(buttonMargin))
          .cast();
      IconButtonElement sectionIcon = new IconButtonElement(context, this)
          .setImage(GUI_SECTION_ICON)
          .setTooltip("Insert section character")
          .setOnClick(this::onInsertSectionIcon)
          .setMaxContentWidth(iconWidth)
          .setMargin(RectExtension.fromLeft(buttonMargin))
          .cast();
      this.confirmButton = new IconButtonElement(context, this)
          .setImage(GUI_TICK_ICON)
          .setEnabled(this, false)
          .setTooltip("Confirm changes")
          .setOnClick(this::onConfirmEdit)
          .setMaxContentWidth(iconWidth)
          .setMargin(RectExtension.fromLeft(buttonMargin))
          .cast();
      IconButtonElement cancelButton = new IconButtonElement(context, this)
          .setImage(GUI_CLEAR_ICON)
          .setTooltip("Cancel changes")
          .setOnClick(this::onCancelEdit)
          .setMaxContentWidth(iconWidth)
          .setMargin(RectExtension.fromLeft(buttonMargin))
          .cast();

      this.readonlyElement = new InlineElement(context, this)
          .addElement(this.labelElement)
          .addElement(editButton)
          .addElement(deleteButton)
          .setAllowShrink(true)
          .setSizingMode(SizingMode.FILL);
      this.editingElement = new InlineElement(context, this)
          .addElement(this.textInputElement)
          .addElement(sectionIcon)
          .addElement(confirmButton)
          .addElement(cancelButton)
          .setAllowShrink(true)
          .setSizingMode(SizingMode.FILL);

      super.addElement(this.readonlyElement);
      super.addElement(this.editingElement);

      this.setEditingMode(isNew);
    }

    private void onDelete() {
      this.onDelete.accept(this);
    }

    private void onInsertSectionIcon() {
      this.textInputElement.writeText("§");

      // bring back the focus to the input element
      super.context.focusedElement = this.textInputElement;
    }

    private void onEdit() {
      this.setEditingMode(true);
    }

    private void onEditedTextChanged(String text) {
      boolean textChanged = !text.equals(this.element.getText());
      this.confirmButton.setEnabled(this, textChanged);
    }

    private void onCancelEdit() {
      this.textInputElement.setText(this.element.getText());
      this.setEditingMode(false);

      if (this.isNew) {
        this.onDelete();
      }
    }

    private void onConfirmEdit() {
      this.element.setText(this.textInputElement.getText());
      this.labelElement.setText(this.textInputElement.getText());
      this.setEditingMode(false);
      this.onConfirm.accept(this);
      this.isNew = false;
    }

    private void setEditingMode(boolean editing) {
      this.readonlyElement.setVisible(!editing);
      this.editingElement.setVisible(editing);

      if (editing) {
        super.context.focusedElement = this.textInputElement;
      }
    }
  }
}
