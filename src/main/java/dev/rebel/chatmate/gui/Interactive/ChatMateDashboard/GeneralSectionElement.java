package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;

public class GeneralSectionElement extends ContainerElement implements ISectionElement {
  private final GeneralSectionLivestreamElement livestreamElement;

  public GeneralSectionElement(InteractiveContext context, IElement parent, ChatMateEndpointProxy chatMateEndpointProxy) {
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
