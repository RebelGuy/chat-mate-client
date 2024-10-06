package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.CheckboxInputElement.CheckboxAppearance;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Ensures that exactly one of the added options is selected. */
public class RadioButtonGroup extends BlockElement {
  private final List<CheckboxInputElement> options;

  public RadioButtonGroup(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.options = new ArrayList<>();
  }

  public RadioButtonGroup addOption(CheckboxInputElement checkboxInputElement) {
    checkboxInputElement.setCheckboxAppearance(CheckboxAppearance.RADIO);
    checkboxInputElement.onCheckedChanged(checked -> this.onCheckedChanged(checkboxInputElement, checked));
    checkboxInputElement.setValidator((isChecked, isUserInput) -> !isUserInput || isChecked); // allow only checking, not unchecking
    this.options.add(checkboxInputElement);
    super.addElement(checkboxInputElement);
    return this;
  }

  /** Once all options have been added, call this method to enforce that exactly one option is selected. */
  public RadioButtonGroup validate() {
    @Nullable CheckboxInputElement first = null;
    boolean anyChecked = false;

    for (CheckboxInputElement option : this.options) {
      if (first == null) {
        first = option;
      }

      if (anyChecked) {
        option.setChecked(false);
      } else {
        anyChecked = option.getChecked();
      }
    }

    if (!anyChecked && first != null) {
      first.setChecked(true);
    }

    return this;
  }

  private void onCheckedChanged(CheckboxInputElement checkboxInputElement, Boolean checked) {
    if (checked) {
      for (CheckboxInputElement sibling : this.options) {
        if (sibling == checkboxInputElement) {
          continue;
        }

        sibling.setChecked(false, true);
      }
    }
  }
}
