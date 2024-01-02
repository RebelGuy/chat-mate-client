package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.EditableListElement.EditableListElement;
import dev.rebel.chatmate.gui.Interactive.EditableListElement.IEditableListAdapter;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;
import scala.Tuple2;

import java.util.List;
import java.util.function.BiFunction;

public class UserVariablesListElement extends BlockElement implements IEditableListAdapter<UserVariable, UserVariablesListElement.UserVariableElement> {
  private final CounterModalController controller;
  private final EditableListElement<UserVariable, UserVariableElement> variableListElement;

  public UserVariablesListElement(InteractiveContext context, IElement parent, CounterModalController controller) {
    super(context, parent);
    this.controller = controller;

    this.variableListElement = new EditableListElement<>(context, this, this);

    super.addElement(this.variableListElement);
  }

  public boolean validate() {
    return !Collections.any(this.variableListElement.getItems(), (var, i) -> !this.validateName(var, i) || !this.validateValue(var, i));
  }

  public List<UserVariable> getUserVariables() {
    return this.variableListElement.getItems();
  }

  @Override
  public UserVariable onCreateItem(int newIndex) {
    return new UserVariable(newIndex == 0 ? "x" : "", "");
  }

  @Override
  public UserVariableElement onCreateContents(UserVariable fromItem, int fromIndex) {
    return this.createUserVariableElement(fromItem, fromIndex);
  }

  @Override
  public void onItemAdded(UserVariable addedItem, int addedAtIndex) {
    this.variableListElement.getElements().forEach(el -> el.contents.revalidate());
  }

  @Override
  public void onItemRemoved(UserVariable removedItem, int removedAtIndex) {
    this.variableListElement.getElements().forEach(el -> el.contents.revalidate());
  }

  @Override
  public void onIndexUpdated(UserVariable item, UserVariableElement element, int previousIndex, int newIndex) {
    element.index = newIndex;
    element.revalidate();
  }

  private UserVariableElement createUserVariableElement(UserVariable variable, int index) {
    return new UserVariableElement(super.context, this, variable, index, this::validateName, this::validateValue, this::formatValue)
        .setMargin(new RectExtension(ZERO, gui(2)))
        .cast();
  }

  private boolean validateName(UserVariable variable, int index) {
    return variable.name.length() > 0
        && !variable.name.contains("{")
        && !variable.name.contains("}")
        // should not be the same name as any of the accessible variables
        && !this.getAccessibleVariables(variable, index).contains(variable.name);
  }

  private boolean validateValue(UserVariable variable, int index) {
    if (index == 0) {
      return true;
    }

    List<String> accessibleVariables = this.getAccessibleVariables(variable, index);
    return variable.value.length() > 0 && !this.controller.usesInaccessibleVariables(variable.value, accessibleVariables);
  }

  private List<String> getAccessibleVariables(UserVariable variable, int index) {
    return Collections.map(
        Collections.filter(this.getUserVariables(), (var, i) -> i < index),
        var -> var.name
    );
  }

  private List<Tuple2<String, Font>> formatValue(UserVariable variable, int index) {
    List<String> accessibleVariables = this.getAccessibleVariables(variable, index);
    return this.controller.formatText(variable.value, accessibleVariables);
  }

  protected static class UserVariableElement extends InlineElement {
    public int index;
    private final UserVariable userVariable;
    private final BiFunction<UserVariable, Integer, Boolean> onValidateName;
    private final BiFunction<UserVariable, Integer, Boolean> onValidateValue;

    private final TextInputElement nameInput;
    private final TextInputElement valueInput;

    public UserVariableElement(InteractiveContext context,
                               IElement parent,
                               UserVariable userVariable,
                               int index,
                               BiFunction<UserVariable, Integer, Boolean> onValidateName,
                               BiFunction<UserVariable, Integer, Boolean> onValidateValue,
                               BiFunction<UserVariable, Integer, List<Tuple2<String, Font>>> textFormatter) {
      super(context, parent);
      this.userVariable = userVariable;
      this.index = index;
      this.onValidateName = onValidateName;
      this.onValidateValue = onValidateValue;

      this.nameInput = new TextInputElement(context, this)
          .setTextUnsafe(userVariable.name)
          .onTextChange(this::onNameChange)
          .setEnabled(this, index > 0)
          .setTabIndex(2 * this.index + 1)
          .setMaxWidth(gui(30))
          .cast();

      this.valueInput = new TextInputElement(context, this)
          .setPlaceholder(index == 0 ? "Counter value" : null)
          .onTextChange(this::onValueChange)
          .setTextFormatter(text -> textFormatter.apply(userVariable, index))
          .setEnabled(this, index > 0)
          .setTabIndex(2 * this.index + 2)
          .setMinWidth(gui(50))
          .cast();

      super.addElement(this.nameInput);
      super.addElement(new LabelElement(context, this)
          .setText("=")
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .setMargin(new RectExtension(gui(1.5f), ZERO))
      );
      super.addElement(this.valueInput);
      super.setAllowShrink(true);
      super.setMinWidth(gui(60));

      if (index > 0) {
        context.onSetFocus(this.nameInput);
      }
    }

    private void revalidate() {
      this.onNameChange(this.userVariable.name);
      this.onValueChange(this.userVariable.value);
    }

    private void onNameChange(String newName) {
      this.userVariable.name = newName.trim();
    }

    private void onValueChange(String newValue) {
      this.userVariable.value = newValue.trim();
    }

    @Override
    protected void renderElement() {
      // update every frame because things might change outside this element that affect the validity
      this.nameInput.setWarning(!this.onValidateName.apply(this.userVariable, this.index));
      this.valueInput.setWarning(!this.onValidateValue.apply(this.userVariable, this.index));

      super.renderElement();
    }
  }
}
