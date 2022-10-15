package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Objects;
import scala.Int;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static dev.rebel.chatmate.util.TextHelpers.isNullOrEmpty;

public class DatePicker extends InputElement {
  private final ContainerElement container;

  private final TextInputElement dayInput;
  private final TextInputElement monthInput;
  private final TextInputElement yearInput;

  public DatePicker(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);
    super.setCursor(null);
    super.setFocusable(false);

    this.dayInput = new TextInputElement(context, this)
        .setPlaceholder("dd")
        .setMaxStringLength(2)
        .setValidator(this::onValidateDay)
        .setTabIndex(0)
        .setMaxContentWidth(context.fontEngine.getStringWidthDim("00"))
        .cast();
    this.monthInput = new TextInputElement(context, this)
        .setPlaceholder("mm")
        .setMaxStringLength(2)
        .setValidator(this::onValidateMonth)
        .setTabIndex(1)
        .setMaxContentWidth(context.fontEngine.getStringWidthDim("00"))
        .cast();
    this.yearInput = new TextInputElement(context, this)
        .setPlaceholder("yyyy")
        .setMaxStringLength(4)
        .setValidator(this::onValidateYear)
        .setTabIndex(2)
        .setMaxContentWidth(context.fontEngine.getStringWidthDim("0000"))
        .cast();

    this.container = new InlineElement(context, this)
        .addElement(this.dayInput)
        .addElement(new LabelElement(context, this)
            .setText("/")
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(this.monthInput)
        .addElement(new LabelElement(context, this)
            .setText("/")
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(this.yearInput)
        .cast();
  }

  public DatePickerModel getModel() {
    int year = Objects.firstOrNull(this.tryGetValidInteger(this.yearInput.getText(), 0, 9999), 0);
    int month = Objects.firstOrNull(this.tryGetValidInteger(this.monthInput.getText(), 1, 12), 1);
    int day = Objects.firstOrNull(this.tryGetValidInteger(this.dayInput.getText(), 1, 31), 1);

    // wtf is this bs
    Calendar calendar = new GregorianCalendar();
    calendar.set(year, month - 1, day);
    Date date = calendar.getTime();

    return new DatePickerModel(date.getTime());
  }

  public boolean validate() {
    return tryGetValidInteger(this.dayInput.getText(), 1, 31) != null
        && tryGetValidInteger(this.monthInput.getText(), 1, 12) != null
        && tryGetValidInteger(this.yearInput.getText(), 1, 9999) != null;
  }

  private boolean onValidateDay(String day) {
    return isNullOrEmpty(day) || this.tryGetValidInteger(day, 1, 31) != null;
  }

  private boolean onValidateMonth(String month) {
    return isNullOrEmpty(month) || this.tryGetValidInteger(month, 1, 12) != null;
  }

  private boolean onValidateYear(String year) {
    return isNullOrEmpty(year) || this.tryGetValidInteger(year, 1, 9999) != null;
  }

  private @Nullable Integer tryGetValidInteger(String maybeTime, int minValue, int maxValue) {
    if (isNullOrEmpty(maybeTime)) {
      return null;
    }

    try {
      int time = Integer.parseInt(maybeTime);
      return time < minValue || time > maxValue ? null : time;
    } catch (Exception ignored) {
      return null;
    }
  }

  @Override
  public InputElement setEnabled(Object key, boolean enabled) {
    this.dayInput.setEnabled(key, enabled);
    this.monthInput.setEnabled(key, enabled);
    this.yearInput.setEnabled(key, enabled);
    return super.setEnabled(key, enabled);
  }

  @Override
  public List<IElement> getChildren() {
    return Collections.list(this.container);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.container.calculateSize(maxContentSize);
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);
    this.container.setBox(super.getContentBox());
  }

  @Override
  protected void renderElement() {
    this.container.render(null);
  }

  public static class DatePickerModel {
    public final @Nullable Long timestamp;

    public DatePickerModel(@Nullable Long timestamp) {
      this.timestamp = timestamp;
    }
  }
}
