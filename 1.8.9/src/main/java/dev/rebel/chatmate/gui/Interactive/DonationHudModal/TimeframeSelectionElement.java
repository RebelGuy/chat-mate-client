package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;

public class TimeframeSelectionElement extends BlockElement {
  private final CheckboxInputElement thisStreamOnlyCheckbox;
  private final CheckboxInputElement sinceDateCheckbox;
  private final DatePicker datePicker;

  public TimeframeSelectionElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    IElement headerLabel = new LabelElement(context, this)
        .setText("Select timeframe:");

    this.thisStreamOnlyCheckbox = new CheckboxInputElement(context, this)
        .setLabel("This stream only")
        .setChecked(true)
        .setCheckboxBorderColour(Colour.WHITE)
        .setMargin(new RectExtension(ZERO, gui(2)))
        .cast();
    this.sinceDateCheckbox = new CheckboxInputElement(context, this)
        .setLabel("Since date")
        .setCheckboxBorderColour(Colour.WHITE)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();
    this.datePicker = new DatePicker(context, this)
        .setEnabled(this, false)
        .setMargin(new RectExtension(gui(4), ZERO, ZERO, ZERO))
        .cast();
    this.sinceDateCheckbox.onCheckedChanged(checked -> this.datePicker.setEnabled(this, checked));

    // note: this element is not rendered so that we can customise the rendering of the checkbox elements
    IElement radioGroup = new RadioButtonGroup(context, this)
        .addOption(this.thisStreamOnlyCheckbox)
        .addOption(this.sinceDateCheckbox)
        .validate();

    super.addElement(headerLabel);
    super.addElement(thisStreamOnlyCheckbox);
    super.addElement(new InlineElement(context, this)
        .addElement(this.sinceDateCheckbox)
        .addElement(this.datePicker)
        .setMargin(new RectExtension(ZERO, gui(2)))
    );
  }

  public TimeframeModel getModel() {
    DatePicker.DatePickerModel datePickerModel = this.datePicker.getModel();
    return new TimeframeModel(this.thisStreamOnlyCheckbox.getChecked(), datePickerModel == null ? 0 : datePickerModel.timestamp);
  }

  public boolean validate() {
    return !this.sinceDateCheckbox.getChecked() || this.sinceDateCheckbox.getChecked() && this.datePicker.validate();
  }

  public static class TimeframeModel {
    public final boolean thisStreamOnly;
    public final Long since;

    public TimeframeModel(boolean thisStreamOnly, Long since) {
      this.thisStreamOnly = thisStreamOnly;
      this.since = since;
    }
  }
}
