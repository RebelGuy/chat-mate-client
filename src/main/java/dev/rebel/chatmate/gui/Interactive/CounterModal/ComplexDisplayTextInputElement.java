package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;
import scala.Tuple2;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ComplexDisplayTextInputElement extends BlockElement {
  private final CounterModalController controller;

  private final TextInputElement displayTextInput;
  private final UserVariablesListElement userVariablesListElement;

  public ComplexDisplayTextInputElement(InteractiveContext context, IElement parent, CounterModalController controller, Runnable onSwitchToSimpleMode) {
    super(context, parent);
    this.controller = controller;

    this.displayTextInput = new TextInputElement(context, this)
        .setTextUnsafe("Counter value: {{x}}")
        .setTextFormatter(this::formatText)
        .setTabIndex(0)
        .setAutoFocus(true)
        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        .setMinWidth(gui(10))
        .cast();

    IElement textElement = new InlineElement(context, this) // todo: add tab indexes
        .addElement(new LabelElement(context, this)
            .setText("Text:")
            .setOverflow(TextOverflow.TRUNCATE)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setMargin(RectExtension.fromRight(gui(4)))
        ).addElement(this.displayTextInput)
        .addElement(new IconButtonElement(context, this)
            .setImage(Asset.GUI_TEXT_ICON)
            .setOnClick(onSwitchToSimpleMode)
            .setBorderCornerRadius(gui(2))
            .setMaxWidth(gui(12))
            .setPadding(new RectExtension(gui(1)))
            .setMargin(RectExtension.fromLeft(gui(4)))
            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        ).setAllowShrink(true)
        .setMargin(RectExtension.fromBottom(gui(5)));

    this.userVariablesListElement = new UserVariablesListElement(context, this, this.controller)
        .setMinWidth(gui(50))
        .cast();

    IElement variablesElement = new BlockElement(context, this)
        .addElement(new LabelElement(context, this)
            .setText("Variables:")
        ).addElement(new ScrollingElement(context, this)
            .setElement(this.userVariablesListElement)
            .setMaxHeight(gui(100))
        ).setMargin(RectExtension.fromBottom(gui(5)));

    super.addElement(textElement);
    super.addElement(variablesElement);
  }

  public boolean validate() {
    List<String> accessibleVariables = Collections.map(this.userVariablesListElement.getUserVariables(), var -> var.name);
    return !this.controller.usesInaccessibleVariables(this.displayTextInput.getText(), accessibleVariables) && this.userVariablesListElement.validate();
  }

  public Function<Integer, String> constructDisplayFunction() {
    List<UserVariable> userVariables = Collections.filter(this.userVariablesListElement.getUserVariables(), var -> !Objects.equals(var.name, "x"));
    return Expression.createDisplayFunction(this.displayTextInput.getText(), Collections.map(userVariables, var -> new Tuple2<>(var.name, var.value)));
  }

  public void setFocus() {
    super.context.onSetFocus(this.displayTextInput);
  }

  private List<Tuple2<String, Font>> formatText(String text) {
    List<String> accessibleVariables = Collections.map(this.userVariablesListElement.getUserVariables(), var -> var.name);
    return this.controller.formatText(text, accessibleVariables);
  }
}
