package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Hud.DropdownSelectionElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InlineElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;
import scala.Tuple2;

import static dev.rebel.chatmate.util.TextHelpers.toSentenceCase;

public class AppearanceElement extends InlineElement {
  private final LabelElement label;
  private final DropdownSelectionElement<TextAlignment> textAlignmentDropdown;

  public AppearanceElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.label = new LabelElement(context, this)
        .setText("Select text alignment:")
        .setMargin(new RectExtension(ZERO, gui(4), gui(2), gui(2)))
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();
    this.textAlignmentDropdown = new DropdownSelectionElement<TextAlignment>(context, this)
        .setMaxWidth(gui(75))
        .setMargin(new RectExtension(gui(6), gui(2)))
        .cast();

    for (TextAlignment textAlignment : TextAlignment.values()) {
      LabelElement label = new LabelElement(context, this)
          .setText(toSentenceCase(textAlignment.toString()))
          .setPadding(new RectExtension(gui(3), gui(1)))
          .setSizingMode(SizingMode.FILL)
          .cast();
      this.textAlignmentDropdown.addOption(
          label,
          textAlignment,
          null,
          (el, selected) -> el.setColour(selected ? Colour.LIGHT_YELLOW : Colour.WHITE),
          alignment -> toSentenceCase(alignment.toString()));
    }
    this.textAlignmentDropdown.setSelection(TextAlignment.LEFT);

    super.addElement(this.label);
    super.addElement(this.textAlignmentDropdown);
  }

  public AppearanceModel getModel() {
    return new AppearanceModel(this.textAlignmentDropdown.getSelection());
  }

  public boolean validate() {
    return this.textAlignmentDropdown.getSelection() != null;
  }

  public static class AppearanceModel {
    public final TextAlignment textAlignment;

    public AppearanceModel(TextAlignment textAlignment) {
      this.textAlignment = textAlignment;
    }
  }

  public enum TextAlignment {
    LEFT, CENTRE, RIGHT, AUTO
  }
}
