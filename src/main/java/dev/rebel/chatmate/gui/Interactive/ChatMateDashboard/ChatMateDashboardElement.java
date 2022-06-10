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
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The main element that should be rendered into the interactive screen. */
public class ChatMateDashboardElement extends ContainerElement {
  private final static List<Tuple2<SettingsPage, String>> pageNames = new ArrayList<Tuple2<SettingsPage, String>>() {{
    add(new Tuple2<>(SettingsPage.GENERAL, "General"));
    add(new Tuple2<>(SettingsPage.HUD, "HUD"));
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
    super.setMargin(new RectExtension(ZERO, ZERO, gui(4), ZERO)); // clear the HUD indicator
    super.setBorder(new RectExtension(gui(8)));
    super.setPadding(new RectExtension(gui(8)));

    this.store = new DashboardStore();
    this.chatMateEndpointProxy = chatMateEndpointProxy;

    this.sidebarMaxWidth = gui(80);

    this.generalSection = new GeneralSectionElement(context, this, this.chatMateEndpointProxy);
    this.hudSection = new HudSectionElement(context, this);

    this.sidebar = new SidebarElement(context, this, this.store, pageNames)
        .setMargin(new RectExtension(ZERO, gui(8), ZERO, ZERO))
        .setMaxWidth(this.sidebarMaxWidth)
        .cast();
    this.content = new ElementReference(context, this);
    this.contentWrapper = new WrapperElement(context, this, this.content)
        .cast();
    this.setContentWidth(this.context.dimFactory.getMinecraftSize());

    super.addElement(this.sidebar);
    super.addElement(this.contentWrapper);

    this.store.onSettingsPageChange(this::onSettingsPageChange);
    this.onSettingsPageChange(SettingsPage.GENERAL);
  }

  private void onSettingsPageChange(SettingsPage settingsPage) {
    ISectionElement newElement;
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

    if (!this.content.compareWithUnderlyingElement(newElement)) {
      ISectionElement existingElement = (ISectionElement)this.content.getUnderlyingElement();
      if (existingElement != null) {
        existingElement.onHide();
      }
      this.content.setUnderlyingElement(newElement);
      newElement.onShow();
    }
  }

  private void setContentWidth(DimPoint windowSize) {
    Dim dashboardContentWidth = super.getContentBoxWidth(windowSize.getX());
    this.contentWrapper.setMaxWidth(dashboardContentWidth.minus(this.sidebarMaxWidth));
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
    RectExtension margin = new RectExtension(screen(4));
    Colour colour = new Colour(0, 0, 20);
    Dim borderWidth = screen(16);
    Colour borderColour = new Colour(206, 212, 218);
    Dim cornerRadius = screen(16);
    RendererHelpers.drawRect(0, margin.applySubtractive(minecraftRect), colour, borderWidth, borderColour, cornerRadius);

    super.renderElement();
  }

  public interface ISectionElement extends IElement {
    void onShow();
    void onHide();
  }
}
