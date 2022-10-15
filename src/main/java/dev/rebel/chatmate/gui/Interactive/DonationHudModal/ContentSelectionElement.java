package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.BlockElement;
import dev.rebel.chatmate.gui.Interactive.CheckboxInputElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;

public class ContentSelectionElement extends BlockElement {
  private final CheckboxInputElement showTotalCheckbox;
  private final CheckboxInputElement showLatestCheckbox;
  private final CheckboxInputElement showHighestCheckbox;

  public ContentSelectionElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.showTotalCheckbox = new CheckboxInputElement(context, this)
        .setLabel("Show total amount");
    this.showLatestCheckbox = new CheckboxInputElement(context, this)
        .setLabel("Show latest donations");
    this.showHighestCheckbox = new CheckboxInputElement(context, this)
        .setLabel("Show highest donations");

    super.addElement(this.showTotalCheckbox);
    super.addElement(this.showLatestCheckbox);
    super.addElement(this.showHighestCheckbox);
  }

  public ContentModel getModel() {
    return new ContentModel(this.showTotalCheckbox.getChecked(), this.showLatestCheckbox.getChecked(), 1, this.showHighestCheckbox.getChecked(), 1);
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
