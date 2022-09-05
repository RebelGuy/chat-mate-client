package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.GeneralRoute;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;

import javax.annotation.Nullable;

public class GeneralSectionElement extends ContainerElement implements ISectionElement {
  private final GeneralSectionLivestreamElement livestreamElement;

  public GeneralSectionElement(InteractiveContext context, IElement parent, @Nullable GeneralRoute route, ChatMateEndpointProxy chatMateEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);

    this.livestreamElement = new GeneralSectionLivestreamElement(context, this, chatMateEndpointProxy);

    super.addElement(this.livestreamElement);
  }

  public void onShow() {
    this.livestreamElement.onShow();
  }

  public void onHide() {
    this.livestreamElement.onHide();
  }
}