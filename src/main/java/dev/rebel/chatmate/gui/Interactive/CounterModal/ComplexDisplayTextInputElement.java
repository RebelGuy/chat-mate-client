package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;

public class ComplexDisplayTextInputElement extends BlockElement {

  public ComplexDisplayTextInputElement(InteractiveContext context, IElement parent, Runnable onSwitchToSimpleMode) {
    super(context, parent);

    IElement titleElements = new SideBySideElement(context, this)
        .setElementPadding(gui(10))
        .addElement(1,
            new LabelElement(context, this)
                .setText("Text:")
                .setOverflow(TextOverflow.TRUNCATE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        )
        .addElement(3,
            new TextInputElement(context, this)
//                .onTextChange(this::onTextChange)
                .setTabIndex(0)
                .setAutoFocus(true)
        )
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
        );
  }
}
