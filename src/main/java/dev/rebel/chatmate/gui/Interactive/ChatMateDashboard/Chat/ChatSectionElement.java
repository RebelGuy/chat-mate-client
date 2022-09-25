package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Chat;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement.ISectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.ChatRoute;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.models.Config;

import javax.annotation.Nullable;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.CHECKBOX_LIGHT;

public class ChatSectionElement extends ContainerElement implements ISectionElement {
  public ChatSectionElement(InteractiveContext context, IElement parent, @Nullable ChatRoute route, Config config) {
    super(context, parent, LayoutMode.BLOCK);

    LabelElement labelElement = new LabelElement(context, this)
        .setFontScale(0.75f)
        .setText("Chat Height Offset")
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setMargin(new RectExtension(ZERO, gui(3), ZERO, ZERO))
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
        .setMargin(new RectExtension(ZERO, gui(2)))
    );

    super.addElement(CHECKBOX_LIGHT.create(context, this)
        .setLabel("Show Platform Icon")
        .setChecked(config.getShowChatPlatformIconEmitter().get())
        .onCheckedChanged(config.getShowChatPlatformIconEmitter()::set)
        .setScale(0.75f)
        .setMargin(new RectExtension(ZERO, gui(2)))
    );
  }

  public void onShow() {

  }

  public void onHide() {

  }
}
