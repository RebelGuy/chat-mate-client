package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;

public class HudSectionElement extends ContainerElement {
  public HudSectionElement(InteractiveContext context, IElement parent) {
    super(context, parent, LayoutMode.INLINE);
  }
}
