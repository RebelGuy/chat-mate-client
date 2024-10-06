package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;

import javax.annotation.Nullable;

import static dev.rebel.chatmate.util.Objects.firstOrNull;
import static dev.rebel.chatmate.util.TextHelpers.isNullOrEmpty;

public class ContentSelectionElement extends BlockElement {
  private final CheckboxInputElement showTotalCheckbox;
  private final CheckboxInputElement showLatestCheckbox;
  private final CheckboxInputElement showHighestCheckbox;
  private final TextInputElement latestCount;
  private final TextInputElement highestCount;

  public ContentSelectionElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    IElement headerLabel = new LabelElement(context, this)
        .setText("Select content:");

    this.showTotalCheckbox = new CheckboxInputElement(context, this)
        .setLabel("Show total amount")
        .setCheckboxBorderColour(Colour.WHITE)
        .setMargin(new RectExtension(ZERO, gui(2)))
        .cast();
    this.showLatestCheckbox = new CheckboxInputElement(context, this)
        .setLabel("Show latest donations")
        .setCheckboxBorderColour(Colour.WHITE);
    this.showHighestCheckbox = new CheckboxInputElement(context, this)
        .setLabel("Show highest donations")
        .setCheckboxBorderColour(Colour.WHITE);

    this.latestCount = new TextInputElement(context, this)
        .setTextUnsafe("3")
        .setValidator(this::validatePositiveInt)
        .setEnabled(this, false)
        .setMaxContentWidth(context.fontEngine.getStringWidthDim("00"))
        .setMargin(new RectExtension(gui(4), ZERO, ZERO, ZERO))
        .cast();
    this.highestCount = new TextInputElement(context, this)
        .setTextUnsafe("3")
        .setValidator(this::validatePositiveInt)
        .setEnabled(this, false)
        .setMaxContentWidth(context.fontEngine.getStringWidthDim("00"))
        .setMargin(new RectExtension(gui(4), ZERO, ZERO, ZERO))
        .cast();
    this.showLatestCheckbox.onCheckedChanged(checked -> this.latestCount.setEnabled(this, checked));
    this.showHighestCheckbox.onCheckedChanged(checked -> this.highestCount.setEnabled(this, checked));

    super.addElement(headerLabel);
    super.addElement(this.showTotalCheckbox);
    super.addElement(new InlineElement(context, this)
        .addElement(this.showLatestCheckbox.setVerticalAlignment(VerticalAlignment.MIDDLE))
        .addElement(this.latestCount)
        .setMargin(new RectExtension(ZERO, gui(2)))
    );
    super.addElement(new InlineElement(context, this)
        .addElement(this.showHighestCheckbox.setVerticalAlignment(VerticalAlignment.MIDDLE))
        .addElement(this.highestCount)
        .setMargin(new RectExtension(ZERO, gui(2)))
    );
  }

  public ContentModel getModel() {
    return new ContentModel(
        this.showTotalCheckbox.getChecked(),
        this.showLatestCheckbox.getChecked(),
        firstOrNull(this.tryParsePositiveInteger(this.latestCount.getText()), 0),
        this.showHighestCheckbox.getChecked(),
        firstOrNull(this.tryParsePositiveInteger(this.highestCount.getText()), 0)
    );
  }

  public boolean validate() {
    if (!this.showTotalCheckbox.getChecked() && !this.showLatestCheckbox.getChecked() && !this.showHighestCheckbox.getChecked()) {
      return false;
    } else if (this.showLatestCheckbox.getChecked() && this.tryParsePositiveInteger(this.latestCount.getText()) == null) {
      return false;
    } else if (this.showHighestCheckbox.getChecked() && this.tryParsePositiveInteger(this.highestCount.getText()) == null) {
      return false;
    } else {
      return true;
    }
  }

  private boolean validatePositiveInt(String text) {
    return isNullOrEmpty(text) || this.tryParsePositiveInteger(text) != null;
  }

  private @Nullable Integer tryParsePositiveInteger(String text) {
    if (isNullOrEmpty(text)) {
      return null;
    }

    try {
      int value = Integer.parseInt(text);
      return value > 0 ? value : null;
    } catch (Exception ignored) {
      return null;
    }
  }

  public static class ContentModel {
    public final boolean showTotal;
    public final boolean showLatest;
    public final int latestN;
    public final boolean showHighest;
    public final int highestN;

    public ContentModel(boolean showTotal, boolean showLatest, int latestN, boolean showHighest, int highestN) {
      this.showTotal = showTotal;
      this.showLatest = showLatest;
      this.latestN = latestN;
      this.showHighest = showHighest;
      this.highestN = highestN;
    }
  }
}
