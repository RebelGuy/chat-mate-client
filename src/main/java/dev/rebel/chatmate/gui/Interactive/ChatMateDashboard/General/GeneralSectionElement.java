package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.api.proxy.AccountEndpointProxy;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.GeneralRoute;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.RequireStreamerElement;
import dev.rebel.chatmate.gui.Interactive.RequireStreamerElement.RequireStreamerOptions;

import javax.annotation.Nullable;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;

public class GeneralSectionElement extends ContainerElement implements ISectionElement {
  private final IElement streamerElement;

  public GeneralSectionElement(InteractiveContext context, IElement parent, @Nullable GeneralRoute route, StreamerEndpointProxy streamerEndpointProxy, Config config, AccountEndpointProxy accountEndpointProxy) {
    super(context, parent, LayoutMode.BLOCK);

    super.addElement(new LoginElement(context, this, accountEndpointProxy)
        .setMargin(RectExtension.fromBottom(gui(4)))
    );

    this.streamerElement = new RequireStreamerElement(context, this, new StreamerElement(context, this, streamerEndpointProxy, config), RequireStreamerOptions.forBlockSection());
    IElement enableDebugModeCheckbox = CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable Debug Mode")
        .setChecked(config.getDebugModeEnabledEmitter().get())
        .onCheckedChanged(config.getDebugModeEnabledEmitter()::set)
        .setScale(0.75f)
        .setMargin(RectExtension.fromTop(gui(12)));

    super.addElement(this.streamerElement);
    super.addElement(enableDebugModeCheckbox);
  }

  @Override
  public void onShow() {
    this.streamerElement.setVisible(true);
  }

  @Override
  public void onHide() {
    this.streamerElement.setVisible(false);
  }
}
