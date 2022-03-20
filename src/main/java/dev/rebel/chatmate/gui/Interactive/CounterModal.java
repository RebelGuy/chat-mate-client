package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.TextHelpers.isNullOrEmpty;

public class CounterModal extends ModalElement {
  private final CounterHandler counterHandler;

  private final ButtonElement deleteButton;

  private @Nullable String text = null;
  private @Nullable Integer startValue = 0;
  private @Nullable Integer incrementValue = 1;

  public CounterModal(InteractiveScreen.InteractiveContext context, InteractiveScreen parent, CounterHandler counterHandler) {
    super(context, parent);
    this.counterHandler = counterHandler;

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
                .onTextChange(this::onTextChange)
                .setTabIndex(0)
                .setAutoFocus(true)
            )
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
    );

    IElement valueElements = new SideBySideElement(context, this)
        .setElementPadding(gui(40))
        .addElement(1,
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
                        .setText("0")
                        .setTabIndex(1)
                ).setElementPadding(gui(5))
            )
        .addElement(1,
            new SideBySideElement(context, this)
                .addElement(1,
                    new LabelElement(context, this)
                        .setText("Start:")
                        .setOverflow(TextOverflow.TRUNCATE)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                ).addElement(0.75f,
                    new TextInputElement(context, this)
                        .onTextChange(this::onIncrementChange)
                        .setValidator(this::onValidateIncrement)
                        .setText("1")
                        .setTabIndex(2)
                ).setElementPadding(gui(5))
            )
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5)));

    this.deleteButton = (ButtonElement)new ButtonElement(context, this)
        .setText("Delete existing counter")
        .setOnClick(this::onDeleteCounter)
        .setVisible(this.counterHandler.hasExistingCounter())
        .setMargin(new RectExtension(ZERO, gui(5)))
        .setHorizontalAlignment(Layout.HorizontalAlignment.CENTRE);

    super.setBody(new ListElement(context, this).addElement(titleElements).addElement(valueElements).addElement(deleteButton));
    super.setTitle("Set up Counter");
  }

  private void onTextChange(String newText) {
    this.text = newText;
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
  protected boolean validate() {
    return this.incrementValue != null && this.startValue != null;
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    try {
      this.counterHandler.createCounter(this.startValue, this.incrementValue, 1, this.text);
      onSuccess.run();
      super.onCloseScreen();

    } catch (Exception e) {
      onError.accept("Something went wrong: " + e.getMessage());
    }
  }
}
