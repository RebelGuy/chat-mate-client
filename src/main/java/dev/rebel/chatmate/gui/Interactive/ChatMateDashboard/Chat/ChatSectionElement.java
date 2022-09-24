package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Chat;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.ChatRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.GeneralRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General.GeneralSectionLivestreamElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;

import javax.annotation.Nullable;

public class ChatSectionElement extends ContainerElement implements ISectionElement {
  public ChatSectionElement(InteractiveContext context, IElement parent, @Nullable ChatRoute route, Config config) {
    super(context, parent, LayoutMode.BLOCK);

    LabelElement labelElement = new LabelElement(context, this)
        .setFontScale(0.75f)
        .setText("Chat Height Offset")
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setMargin(new Layout.RectExtension(ZERO, gui(3), ZERO, ZERO))
        .cast();
    ValueSliderElement chatDisplacementSlider = new ValueSliderElement(context, this) // todo: when scrolling, increment by the smallest possible unit
        .setDecimals(0)
        .setMinValue(0)
        .setMaxValue(100)
        .setSuffix("px")
        .setValue(config.getChatVerticalDisplacementEmitter().get())
        .onChange(x -> config.getChatVerticalDisplacementEmitter().set((int)x.floatValue())) // lol
        .setSizingMode(SizingMode.FILL)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast()
        .cast();
    super.addElement(new InlineElement(context, this)
        .addElement(labelElement)
        .addElement(chatDisplacementSlider)
    );
  }

  public void onShow() {

  }

  public void onHide() {

  }
}
