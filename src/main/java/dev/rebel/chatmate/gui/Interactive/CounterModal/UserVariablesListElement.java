package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserVariablesListElement extends BlockElement {
  private final List<Tuple2<UserVariable, UserVariableElement>> userVariables;

  public UserVariablesListElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    UserVariable standardVariable = new UserVariable(0, "", "");
    UserVariableElement standardVariableElement = this.createUserVariableElement(standardVariable);

    this.userVariables = new ArrayList<>();
    this.userVariables.add(new Tuple2<>(standardVariable, standardVariableElement));

    super.addElement(standardVariableElement);
  }

  private UserVariableElement createUserVariableElement(UserVariable variable) {
    return new UserVariableElement(super.context, this, variable, this::onAddUserVariable, this::onRemoveUserVariable, this::onUpdate)
        .setMargin(new RectExtension(ZERO, gui(2)))
        .cast();
  }

  private void onAddUserVariable(UserVariable originator) {
    int newIndex = originator.index + 1;
    this.userVariables.subList(newIndex, this.userVariables.size()).forEach(var -> var._1.index++);

    UserVariable newVariable = new UserVariable(newIndex, "", "");
    UserVariableElement newVariableElement = this.createUserVariableElement(newVariable);
    this.userVariables.add(newIndex, new Tuple2<>(newVariable, newVariableElement));

    super.clear();
    this.userVariables.forEach(var -> super.addElement(var._2));
  }

  private void onRemoveUserVariable(UserVariable originator) {
    this.userVariables.remove(originator.index);
    this.userVariables.subList(originator.index, this.userVariables.size()).forEach(var -> var._1.index--);

    super.clear();
    this.userVariables.forEach(var -> super.addElement(var._2));
  }

  private void onUpdate(UserVariable updatedVariable) {

  }

  private static class UserVariableElement extends InlineElement {
    private final UserVariable userVariable;
    private final Consumer<UserVariable> onAddUserVariable;
    private final Consumer<UserVariable> onRemoveUserVariable;
    private final Consumer<UserVariable> onUpdate;

    private final TextInputElement nameInput;
    private final TextInputElement valueInput;

    public UserVariableElement(InteractiveContext context, IElement parent, UserVariable userVariable, Consumer<UserVariable> onAddUserVariable, Consumer<UserVariable> onRemoveUserVariable, Consumer<UserVariable> onUpdate) {
      super(context, parent);
      this.userVariable = userVariable;
      this.onAddUserVariable = onAddUserVariable;
      this.onRemoveUserVariable = onRemoveUserVariable;
      this.onUpdate = onUpdate;

      int index = userVariable.index;
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
          ).setOnClick(() -> onAddUserVariable.accept(this.userVariable))
          .setBorder(new RectExtension(gui(0)))
          .setPadding(new RectExtension(gui(0)))
          .cast();
      TextButtonElement createButton = new TextButtonElement(context, this)
          .setText("-")
          .withLabelUpdated(label -> label
              .setFontScale(0.75f)
              .setPadding(new RectExtension(ZERO))
          ).setOnClick(() -> onRemoveUserVariable.accept(this.userVariable))
          .setEnabled(this, index > 0)
          .setBorder(new RectExtension(gui(0)))
          .setPadding(new RectExtension(gui(0)))
          .setMargin(RectExtension.fromTop(gui(1)))
          .cast();

      super.addElement(this.nameInput);
      super.addElement(new LabelElement(context, this)
          .setText("=")
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .setMargin(new RectExtension(gui(1.5f), ZERO))
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
