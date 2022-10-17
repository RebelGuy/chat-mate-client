package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserVariablesListElement extends BlockElement {
  private final CounterModalController controller;
  private final List<Tuple2<UserVariable, UserVariableElement>> userVariables;

  public UserVariablesListElement(InteractiveContext context, IElement parent, CounterModalController controller) {
    super(context, parent);
    this.controller = controller;

    UserVariable standardVariable = new UserVariable(0, "x", "");
    UserVariableElement standardVariableElement = this.createUserVariableElement(standardVariable);

    this.userVariables = new ArrayList<>();
    this.userVariables.add(new Tuple2<>(standardVariable, standardVariableElement));

    super.addElement(standardVariableElement);
  }

  public boolean validate() {
    return !Collections.any(this.userVariables, var -> !this.validateName(var._1) || !this.validateValue(var._1));
  }

  public List<UserVariable> getUserVariables() {
    return Collections.map(this.userVariables, var -> var._1);
  }

  private UserVariableElement createUserVariableElement(UserVariable variable) {
    return new UserVariableElement(super.context, this, variable, this::onAddUserVariable, this::onRemoveUserVariable, this::validateName, this::validateValue, this::formatValue)
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
    this.userVariables.forEach(var -> {
      super.addElement(var._2);
      var._2.revalidate();
    });
  }

  private void onRemoveUserVariable(UserVariable originator) {
    this.userVariables.remove(originator.index);
    this.userVariables.subList(originator.index, this.userVariables.size()).forEach(var -> var._1.index--);

    super.clear();
    this.userVariables.forEach(var -> {
      super.addElement(var._2);
      var._2.revalidate();
    });
  }

  private boolean validateName(UserVariable variable) {
    return variable.name.length() > 0
        && !variable.name.contains("{")
        && !variable.name.contains("}")
        // should not be the same name as any of the accessible variables
        && !this.getAccessibleVariables(variable).contains(variable.name);
  }

  private boolean validateValue(UserVariable variable) {
    if (variable.index == 0) {
      return true;
    }

    // should only contain known variables that are also accessible to this variable
    List<String> variablesUsed = Collections.map(this.controller.extractUserVariables(variable.value), var -> var._2);
    List<String> accessibleVariables = this.getAccessibleVariables(variable);
    boolean usesInaccessibleVariables = Collections.any(variablesUsed, var -> !accessibleVariables.contains(var));

    return variable.value.length() > 0 && !usesInaccessibleVariables;
  }

  private List<String> getAccessibleVariables(UserVariable variable) {
    return Collections.map(
        Collections.filter(this.userVariables, var -> var._1.index < variable.index),
        var -> var._1.name
    );
  }

  private List<Tuple2<String, Font>> formatValue(UserVariable variable) {
    List<String> accessibleVariables = this.getAccessibleVariables(variable);
    return this.controller.formatText(variable.value, accessibleVariables);
  }

  private static class UserVariableElement extends InlineElement {
    private final UserVariable userVariable;
    private final Consumer<UserVariable> onAddUserVariable;
    private final Consumer<UserVariable> onRemoveUserVariable;
    private final Function<UserVariable, Boolean> onValidateName;
    private final Function<UserVariable, Boolean> onValidateValue;

    private final TextInputElement nameInput;
    private final TextInputElement valueInput;

    public UserVariableElement(InteractiveContext context,
                               IElement parent,
                               UserVariable userVariable,
                               Consumer<UserVariable> onAddUserVariable,
                               Consumer<UserVariable> onRemoveUserVariable,
                               Function<UserVariable, Boolean> onValidateName,
                               Function<UserVariable, Boolean> onValidateValue,
                               Function<UserVariable, List<Tuple2<String, Font>>> textFormatter) {
      super(context, parent);
      this.userVariable = userVariable;
      this.onAddUserVariable = onAddUserVariable;
      this.onRemoveUserVariable = onRemoveUserVariable;
      this.onValidateName = onValidateName;
      this.onValidateValue = onValidateValue;

      int index = userVariable.index;
      this.nameInput = new TextInputElement(context, this)
          .setTextUnsafe(userVariable.name)
          .onTextChange(this::onNameChange)
          .setEnabled(this, index > 0)
          .setMaxWidth(gui(30))
          .cast();

      this.valueInput = new TextInputElement(context, this)
          .setPlaceholder(index == 0 ? "Counter value" : null)
          .onTextChange(this::onValueChange)
          .setTextFormatter(text -> textFormatter.apply(userVariable))
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

    private void revalidate() {
      this.onNameChange(this.userVariable.name);
      this.onValueChange(this.userVariable.value);
    }

    private void onNameChange(String newName) {
      this.userVariable.name = newName.trim();
      this.nameInput.setWarning(!this.onValidateName.apply(this.userVariable));
    }

    private void onValueChange(String newValue) {
      this.userVariable.value = newValue.trim();
      this.valueInput.setWarning(!this.onValidateValue.apply(this.userVariable));
    }
  }
}
