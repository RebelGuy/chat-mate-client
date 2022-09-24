package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.GeneralRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.CheckboxInputElement;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;

import javax.annotation.Nullable;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;

public class GeneralSectionElement extends ContainerElement implements ISectionElement {
  private final GeneralSectionLivestreamElement livestreamElement;

  public GeneralSectionElement(InteractiveContext context, IElement parent, @Nullable GeneralRoute route, ChatMateEndpointProxy chatMateEndpointProxy, Config config) {
    super(context, parent, LayoutMode.BLOCK);

    super.addElement(CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable ChatMate")
        .setChecked(config.getChatMateEnabledEmitter().get())
        .onCheckedChanged(config.getChatMateEnabledEmitter()::set)
        .setScale(0.75f)
        .setSizingMode(SizingMode.FILL)
    );
    super.addElement(CHECKBOX_LIGHT.create(context, this)
        .setLabel("Enable Sound")
        .setChecked(config.getSoundEnabledEmitter().get())
        .onCheckedChanged(config.getSoundEnabledEmitter()::set)
        .setScale(0.75f)
    );

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
