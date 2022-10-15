package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Hud.DropdownSelectionElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InlineElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;

public class AppearanceElement extends InlineElement {
  private final DropdownSelectionElement textAlignmentDropdown;

  public AppearanceElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.textAlignmentDropdown = new DropdownSelectionElement(context, this)
        .setMargin(new RectExtension(gui(6), gui(2)))
        .cast();
  }

  public AppearanceModel getModel() {
    return new AppearanceModel(TextAlignment.AUTO);
  }

  public static class AppearanceModel {
    final TextAlignment textAlignment;

    public AppearanceModel(TextAlignment textAlignment) {
      this.textAlignment = textAlignment;
    }
  }

  public enum TextAlignment {
    AUTO, LEFT, CENTRE, RIGHT
  }
}
