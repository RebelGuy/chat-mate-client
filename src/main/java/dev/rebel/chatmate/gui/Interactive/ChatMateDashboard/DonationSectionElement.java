package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.DonationRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.GeneralRoute;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.proxy.DonationEndpointProxy;

import javax.annotation.Nullable;

public class DonationSectionElement extends ContainerElement implements ISectionElement {
  public DonationSectionElement(InteractiveContext context, IElement parent, @Nullable DonationRoute route, DonationEndpointProxy donationEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);
  }

  public void onShow() {

  }

  public void onHide() {

  }
}
