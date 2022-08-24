package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.HudRoute;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;

import javax.annotation.Nullable;

public class HudSectionElement extends ContainerElement implements ISectionElement {
  public HudSectionElement(InteractiveContext context, IElement parent, @Nullable HudRoute route) {
    super(context, parent, LayoutMode.INLINE);
  }

  public void onShow() {

  }

  public void onHide() {

  }
}
