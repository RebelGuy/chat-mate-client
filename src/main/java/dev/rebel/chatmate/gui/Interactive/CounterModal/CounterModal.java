package dev.rebel.chatmate.gui.Interactive.CounterModal;

import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.TextHelpers.isNullOrEmpty;

public class CounterModal extends ModalElement {
  private final CounterHandler counterHandler;

  private final SimpleDisplayTextInputElement simpleDisplayTextInputElement;
  private final ComplexDisplayTextInputElement complexDisplayTextInputElement;
  private final IElement valueElements;
  private final TextButtonElement deleteButton;

  private @Nullable Integer startValue = 0;
  private @Nullable Integer incrementValue = 1;

  public CounterModal(InteractiveScreen.InteractiveContext context, InteractiveScreen parent, CounterHandler counterHandler) {
    super(context, parent);
    this.name = "CounterModal";
    this.counterHandler = counterHandler;

    this.simpleDisplayTextInputElement = new SimpleDisplayTextInputElement(context, this, () -> this.onSetInputMode(false));
    this.complexDisplayTextInputElement = new ComplexDisplayTextInputElement(context, this, () -> this.onSetInputMode(true))
        .setVisible(false)
        .cast();

    this.valueElements = new SideBySideElement(context, this)
        .setElementPadding(gui(40))
        .addElement(0.6f,
            new SideBySideElement(context, this)
                .addElement(1,
                    new LabelElement(context, this)
                        .setText("Start:")
                        .setOverflow(TextOverflow.TRUNCATE)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                ).addElement(0.75f,
                    new TextInputElement(context, this)
                        .onTextChange(this::onStartValueChange)
                        .setValidator(this::onValidateStartValue)
                        .setTextUnsafe("0")
                        .setTabIndex(1)
                ).setElementPadding(gui(5))
        )
        .addElement(1,
            new SideBySideElement(context, this)
                .addElement(1,
                    new LabelElement(context, this)
                        .setText("Increment:")
                        .setOverflow(TextOverflow.TRUNCATE)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                ).addElement(0.75f,
                    new TextInputElement(context, this)
                        .onTextChange(this::onIncrementChange)
                        .setValidator(this::onValidateIncrement)
                        .setTextUnsafe("1")
                        .setTabIndex(2)
                ).setElementPadding(gui(5))
        )
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
    );

    this.deleteButton = new TextButtonElement(context, this)
        .setText("Delete existing counter")
        .setOnClick(this::onDeleteCounter)
        .setVisible(this.counterHandler.hasExistingCounter())
        .setMargin(new RectExtension(ZERO, gui(5)))
        .setHorizontalAlignment(Layout.HorizontalAlignment.CENTRE)
        .cast();

    super.setTitle("Set up Counter");
    super.setBody(new BlockElement(context, this)
        .addElement(this.simpleDisplayTextInputElement)
        .addElement(this.complexDisplayTextInputElement)
        .addElement(this.valueElements)
        .addElement(this.deleteButton)
    );
  }

  private void onSetInputMode(boolean isSimple) {
    this.simpleDisplayTextInputElement.setVisible(isSimple);
    this.complexDisplayTextInputElement.setVisible(!isSimple);
  }

  private void onStartValueChange(String maybeStartValue) {
    this.startValue = this.tryGetValidStartValue(maybeStartValue);
  }

  private void onIncrementChange(String maybeIncrement) {
    this.incrementValue = this.tryGetValidIncrement(maybeIncrement);
  }

  private boolean onValidateStartValue(String maybeTime) {
    return this.tryGetValidIncrement(maybeTime) != null;
  }

  private boolean onValidateIncrement(String maybeIncrement) {
    return this.tryGetValidIncrement(maybeIncrement) != null;
  }

  private @Nullable Integer tryGetValidStartValue(String maybeStartValue) {
    if (isNullOrEmpty(maybeStartValue)) {
      return 0;
    }

    try {
      int startValue = Integer.parseInt(maybeStartValue);
      return startValue < 0 ? null : startValue;
    } catch (Exception ignored) {
      return null;
    }
  }

  private @Nullable Integer tryGetValidIncrement(String maybeStartValue) {
    if (isNullOrEmpty(maybeStartValue)) {
      return null;
    }

    try {
      int increment = Integer.parseInt(maybeStartValue);
      return increment <= 0 ? null : increment;
    } catch (Exception ignored) {
      return null;
    }
  }

  private void onDeleteCounter() {
    this.counterHandler.deleteCounter();
    this.deleteButton.setVisible(this.counterHandler.hasExistingCounter());
  }

  @Override
  protected @Nullable Boolean validate() {
    return this.incrementValue != null && this.startValue != null;
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    try {
      this.counterHandler.createCounter(this.startValue, this.incrementValue, 1, this.simpleDisplayTextInputElement.getText());
      onSuccess.run();
      super.onCloseScreen();

    } catch (Exception e) {
      onError.accept("Something went wrong: " + e.getMessage());
    }
  }
}
