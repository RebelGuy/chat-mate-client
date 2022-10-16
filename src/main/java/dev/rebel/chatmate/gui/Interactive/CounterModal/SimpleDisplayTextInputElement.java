package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;

import javax.annotation.Nullable;

public class SimpleDisplayTextInputElement extends BlockElement {
  private final TextInputElement textInput;

  public SimpleDisplayTextInputElement(InteractiveContext context, IElement parent, Runnable onSwitchToComplexMode) {
    super(context, parent);

    this.textInput = new TextInputElement(context, this)
        .setTabIndex(0)
        .setAutoFocus(true)
        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        .setMinWidth(gui(10))
        .cast();

    IElement titleElements = new InlineElement(context, this)
        .addElement(new LabelElement(context, this)
            .setText("Text:")
            .setOverflow(TextOverflow.TRUNCATE)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setMargin(RectExtension.fromRight(gui(10)))
        ).addElement(this.textInput)
        .addElement(new IconButtonElement(context, this)
            .setImage(Asset.GUI_FX_ICON)
            .setOnClick(onSwitchToComplexMode)
            .setBorderCornerRadius(gui(2))
            .setMaxWidth(gui(12))
            .setPadding(new RectExtension(gui(1)))
            .setMargin(new RectExtension(gui(4), ZERO, ZERO, ZERO))
            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
        ).setAllowShrink(true)
        .setMargin(RectExtension.fromBottom(gui(5)));

    super.addElement(titleElements);
  }

  public String getText() {
    return this.textInput.getText();
  }
}
