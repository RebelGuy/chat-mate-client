package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.DonationRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.GeneralRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.HudRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardStore.SettingsPage;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.Events.ScreenSizeData;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.services.util.EnumHelpers;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static dev.rebel.chatmate.services.util.Objects.castOrNull;

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
  private final DonationSectionElement donationSection;

  private final SidebarElement sidebar;
  private final WrapperElement contentWrapper;
  private final ElementReference content;

  public ChatMateDashboardElement(InteractiveContext context, IElement parent, @Nullable DashboardRoute route, ChatMateEndpointProxy chatMateEndpointProxy, DonationEndpointProxy donationEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);
    super.setMargin(new RectExtension(ZERO, ZERO, gui(4), ZERO)); // stay clear of the HUD indicator
    super.setBorder(new RectExtension(gui(8)));
    super.setPadding(new RectExtension(gui(8)));

    this.store = new DashboardStore();
    this.chatMateEndpointProxy = chatMateEndpointProxy;

    this.sidebarMaxWidth = gui(80);

    this.generalSection = new GeneralSectionElement(context, this, castOrNull(GeneralRoute.class, route), this.chatMateEndpointProxy);
    this.hudSection = new HudSectionElement(context, this, castOrNull(HudRoute.class, route));
    this.donationSection = new DonationSectionElement(context, this, castOrNull(DonationRoute.class, route), donationEndpointProxy);

    this.sidebar = new SidebarElement(context, this, this.store, pageNames)
        .setMargin(new RectExtension(ZERO, gui(8), ZERO, ZERO))
        .setMaxWidth(this.sidebarMaxWidth)
        .cast();
    this.content = new ElementReference(context, this);
    this.contentWrapper = new WrapperElement(context, this, this.content)
        .cast();
    this.setContentSizes(this.context.dimFactory.getMinecraftSize());

    super.addElement(this.sidebar);
    super.addElement(this.contentWrapper);

    this.store.onSettingsPageChange(this::onSettingsPageChange);
    this.onSettingsPageChange(route ==  null ? SettingsPage.GENERAL : route.page);
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
      case DONATION:
        newElement = this.donationSection;
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

  private void setContentSizes(DimPoint windowSize) {
    Dim dashboardContentWidth = super.getContentBoxWidth(windowSize.getX());
    this.contentWrapper.setMaxWidth(dashboardContentWidth.minus(this.sidebarMaxWidth));

    Dim dashboardSidebarHeight = super.getContentBoxHeight(windowSize.getY());
    this.sidebar.setTargetHeight(dashboardSidebarHeight);
  }

  @Override
  public void onWindowResize(IEvent<ScreenSizeData> e) {
    this.setContentSizes(e.getData().newSize);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxWidth) {
    super.calculateThisSize(maxWidth);

    // this should take up the whole screen
    return super.getContentBoxSize(this.context.dimFactory.getMinecraftSize());
  }

  @Override
  protected void renderElement() {
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
