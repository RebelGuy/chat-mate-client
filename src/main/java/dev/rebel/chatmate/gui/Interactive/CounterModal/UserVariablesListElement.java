package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserVariablesListElement extends BlockElement {
  private final List<UserVariableElement> userVariableElements;

  public UserVariablesListElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    UserVariableElement standardVariable = this.createUserVariableElement(0);

    this.userVariableElements = new ArrayList<>();
    this.userVariableElements.add(standardVariable);

    super.addElement(standardVariable);
  }

  private UserVariableElement createUserVariableElement(int index) {
    return new UserVariableElement(super.context, this, index, this::onAddUserVariable, this::onRemoveUserVariable, this::onUpdate)
        .setMargin(new RectExtension(ZERO, gui(2)))
        .cast();
  }

  private void onAddUserVariable(int originatorIndex) {
    int newIndex = originatorIndex + 1;
    UserVariableElement newVariable = this.createUserVariableElement(newIndex);
    this.userVariableElements.add(newIndex, newVariable);

    super.clear();
    this.userVariableElements.forEach(super::addElement);
  }

  private void onRemoveUserVariable(int originatorIndex) {
    this.userVariableElements.remove(originatorIndex);

    super.clear();
    this.userVariableElements.forEach(super::addElement);
  }

  private void onUpdate(UserVariable updatedVariable) {

  }

  private static class UserVariableElement extends InlineElement {
    private final int index;
    private final Consumer<Integer> onAddUserVariable;
    private final Consumer<Integer> onRemoveUserVariable;
    private final Consumer<UserVariable> onUpdate;

    private final TextInputElement nameInput;
    private final TextInputElement valueInput;

    private UserVariable userVariable;

    public UserVariableElement(InteractiveContext context, IElement parent, int index, Consumer<Integer> onAddUserVariable, Consumer<Integer> onRemoveUserVariable, Consumer<UserVariable> onUpdate) {
      super(context, parent);
      this.index = index;
      this.onAddUserVariable = onAddUserVariable;
      this.onRemoveUserVariable = onRemoveUserVariable;
      this.onUpdate = onUpdate;

      this.userVariable = new UserVariable(index, "Var " + index, "0");

      this.nameInput = new TextInputElement(context, this)
          .setPlaceholder(index == 0 ? "x" : null)
          .setEnabled(this, index > 0)
          .setMaxWidth(gui(30))
          .cast();

      this.valueInput = new TextInputElement(context, this)
          .setPlaceholder(index == 0 ? "Counter value" : null)
          .setEnabled(this, index > 0)
          .setMinWidth(gui(50))
          .cast();

      TextButtonElement deleteButton = new TextButtonElement(context, this)
          .setText("+")
          .withLabelUpdated(label -> label
              .setFontScale(0.75f)
              .setPadding(new RectExtension(ZERO))
          ).setOnClick(() -> onAddUserVariable.accept(index))
          .setBorder(new RectExtension(gui(0)))
          .setPadding(new RectExtension(gui(0)))
          .cast();
      TextButtonElement createButton = new TextButtonElement(context, this)
          .setText("-")
          .withLabelUpdated(label -> label
              .setFontScale(0.75f)
              .setPadding(new RectExtension(ZERO))
          ).setOnClick(() -> onRemoveUserVariable.accept(index))
          .setEnabled(this, index > 0)
          .setBorder(new RectExtension(gui(0)))
          .setPadding(new RectExtension(gui(0)))
          .setMargin(RectExtension.fromTop(gui(1)))
          .cast();

      super.addElement(this.nameInput);
      super.addElement(new LabelElement(context, this)
          .setText("=")
          .setMargin(new RectExtension(gui(1.5f), gui(3)))
      );
      super.addElement(this.valueInput);
      super.addElement(new BlockElement(context, this)
          .addElement(createButton)
          .addElement(deleteButton)
          .setMargin(RectExtension.fromLeft(gui(2)))
      );
      super.setAllowShrink(true);
    }
  }
}
