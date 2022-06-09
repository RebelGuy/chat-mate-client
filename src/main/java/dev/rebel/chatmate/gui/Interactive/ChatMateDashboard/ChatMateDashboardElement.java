package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardStore.SettingsPage;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.Events.SizeData;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.util.EnumHelpers;

import java.util.HashMap;
import java.util.Map;

/** The main element that should be rendered into the interactive screen. */
public class ChatMateDashboardElement extends ContainerElement {
  private final static Map<SettingsPage, String> pageNames = new HashMap<SettingsPage, String>() {{
    put(SettingsPage.GENERAL, "General");
    put(SettingsPage.HUD, "HUD");
  }};

  private final DashboardStore store;
  private final ChatMateEndpointProxy chatMateEndpointProxy;

  private final Dim sidebarMaxWidth;

  private final GeneralSectionElement generalSection;
  private final HudSectionElement hudSection;

  private final SidebarElement sidebar;
  private final WrapperElement contentWrapper;
  private final ElementReference content;

  public ChatMateDashboardElement(InteractiveContext context, IElement parent, ChatMateEndpointProxy chatMateEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);
    super.setBorder(new RectExtension(gui(8)));
    super.setPadding(new RectExtension(gui(8)));

    this.store = new DashboardStore();
    this.chatMateEndpointProxy = chatMateEndpointProxy;

    this.sidebarMaxWidth = gui(50);

    this.generalSection = new GeneralSectionElement(context, this, this.chatMateEndpointProxy);
    this.hudSection = new HudSectionElement(context, this);

    this.sidebar = new SidebarElement(context, this, this.store, pageNames)
        .setMargin(new RectExtension(gui(8)))
        .setMaxWidth(this.sidebarMaxWidth)
        .cast();
    this.content = new ElementReference(context, this);
    this.contentWrapper = new WrapperElement(context, this, this.content)
        .setMargin(new RectExtension(gui(8)))
        .cast();
    this.setContentWidth(this.context.dimFactory.getMinecraftSize());

    super.addElement(this.sidebar);
    super.addElement(this.contentWrapper);

    this.store.onSettingsPageChange(this::onSettingsPageChange);
    this.onSettingsPageChange(SettingsPage.GENERAL);
  }

  private void onSettingsPageChange(SettingsPage settingsPage) {
    IElement newElement;
    switch (settingsPage) {
      case GENERAL:
        newElement = this.generalSection;
        break;
      case HUD:
        newElement = this.hudSection;
        break;
      default:
        throw EnumHelpers.<SettingsPage>assertUnreachable(settingsPage);
    }
    this.content.setUnderlyingElement(newElement);
  }

  private void setContentWidth(DimPoint windowSize) {
    this.contentWrapper.setMaxWidth(windowSize.getX().minus(this.sidebarMaxWidth));
  }

  @Override
  public void onWindowResize(IEvent<SizeData> e) {
    this.setContentWidth(e.getData().size);
  }

  @Override
  public DimPoint calculateThisSize(Dim maxWidth) {
    super.calculateThisSize(maxWidth);

    // this should take up the whole screen
    return super.getContentBoxSize(this.context.dimFactory.getMinecraftSize());
  }

  @Override
  public void renderElement() {
    // draw a background with a thick, curvy border ;)
    DimRect minecraftRect = this.context.dimFactory.getMinecraftRect();
    Colour colour = new Colour(0, 0, 20);
    Dim borderWidth = screen(8);
    Colour borderColour = new Colour(206, 212, 218);
    Dim cornerRadius = screen(8);
    RendererHelpers.drawRect(0, minecraftRect, colour, borderWidth, borderColour, cornerRadius);

    super.renderElement();
  }
}
