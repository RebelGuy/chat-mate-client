package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.TextHelpers.isNullOrEmpty;

public class CountdownModal extends ModalElement {
  private final CountdownHandler countdownHandler;

  private TextButtonElement deleteButton;

  private @Nullable Float hours = 0.0f;
  private @Nullable Float minutes = 0.0f;
  private @Nullable Float seconds = 0.0f;
  private @Nullable String title = null;

  public CountdownModal(InteractiveScreen.InteractiveContext context, InteractiveScreen parent, CountdownHandler countdownHandler) {
    super(context, parent);
    this.countdownHandler = countdownHandler;
  }

  @Override
  public void onInitialise() {
    super.onInitialise();

    IElement titleElements = new SideBySideElement(context, this)
        .setElementPadding(gui(10))
        .addElement(1,
            new LabelElement(context, this)
                .setText("Title:")
                .setOverflow(TextOverflow.TRUNCATE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(4,
            new TextInputElement(context, this)
                .onTextChange(this::onTitleChange)
                .setTabIndex(0)
                .setAutoFocus(true)
        ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
    );

    IElement timeElements = new SideBySideElement(context, this)
        .setElementPadding(gui(10))
        .addElement(1,
            new LabelElement(context, this)
                .setText("Duration:")
                .setOverflow(TextOverflow.TRUNCATE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(0.5f,
            new TextInputElement(context, this)
                .onTextChange(this::onHoursChange)
                .setValidator(this::onValidateInput)
                .setSuffix("h")
                .setTextUnsafe("0")
                .setTabIndex(1)
        ).addElement(0.5f,
            new TextInputElement(context, this)
                .onTextChange(this::onMinutesChange)
                .setValidator(this::onValidateInput)
                .setSuffix("m")
                .setTextUnsafe("0")
                .setTabIndex(2)
        ).addElement(0.5f,
            new TextInputElement(context, this)
                .onTextChange(this::onSecondsChange)
                .setValidator(this::onValidateInput)
                .setSuffix("s")
                .setTextUnsafe("0")
                .setTabIndex(3)
        ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
    );

    this.deleteButton = new TextButtonElement(context, this)
        .setText("Delete existing countdown")
        .setOnClick(this::onDeleteCountdown)
        .setVisible(this.countdownHandler.hasExistingCountdown())
        .setMargin(new RectExtension(ZERO, gui(5)))
        .setHorizontalAlignment(Layout.HorizontalAlignment.CENTRE)
        .cast();

    super.setBody(new BlockElement(context, this).addElement(titleElements).addElement(timeElements).addElement(this.deleteButton));
    super.setTitle("Set up Countdown");
  }

  private void onTitleChange(String newTitle) {
    this.title = newTitle;
  }

  private void onSecondsChange(String maybeSeconds) {
    this.seconds = this.tryGetValidTime(maybeSeconds);
  }

  private void onMinutesChange(String maybeMinutes) {
    this.minutes = this.tryGetValidTime(maybeMinutes);
  }

  private void onHoursChange(String maybeHours) {
    this.hours = this.tryGetValidTime(maybeHours);
  }

  private boolean onValidateInput(String maybeTime) {
    return this.tryGetValidTime(maybeTime) != null;
  }

  private @Nullable Float tryGetValidTime(String maybeTime) {
    if (isNullOrEmpty(maybeTime)) {
      return 0.0f;
    }

    try {
      float time = Float.parseFloat(maybeTime);
      return time < 0 ? null : time;
    } catch (Exception ignored) {
      return null;
    }
  }

  private void onDeleteCountdown() {
    this.countdownHandler.stop();
    this.deleteButton.setVisible(this.countdownHandler.hasExistingCountdown());
  }

  @Override
  protected @Nullable Boolean validate() {
    return this.seconds != null && this.minutes != null && this.hours != null && this.getTotalSeconds() > 0;
  }

  private int getTotalSeconds() {
    return (int)(this.hours * 3600 + this.minutes * 60 + this.seconds);
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    try {
      this.countdownHandler.start(this.getTotalSeconds(), this.title);
      onSuccess.run();
      super.close();

    } catch (Exception e) {
      onError.accept("Something went wrong: " + e.getMessage());
    }
  }
}
