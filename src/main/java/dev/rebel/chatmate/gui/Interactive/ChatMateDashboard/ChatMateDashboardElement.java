package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.api.proxy.AccountEndpointProxy;
import dev.rebel.chatmate.api.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Chat.ChatSectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardStore.SettingsPage;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Debug.DebugSectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations.DonationsSectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.General.GeneralSectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Hud.HudSectionElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SidebarElement.PageOptions;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudService;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Events.ScreenSizeData;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.api.proxy.UserEndpointProxy;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.util.EnumHelpers;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static dev.rebel.chatmate.util.Objects.castOrNull;

/** The main element that should be rendered into the interactive screen. */
public class ChatMateDashboardElement extends ContainerElement {
  private final static List<Tuple2<SettingsPage, PageOptions>> pageNames = new ArrayList<Tuple2<SettingsPage, PageOptions>>() {{
    add(new Tuple2<>(SettingsPage.GENERAL, new PageOptions("General", false)));
    add(new Tuple2<>(SettingsPage.HUD, new PageOptions("HUD", true)));
    add(new Tuple2<>(SettingsPage.CHAT, new PageOptions("Chat", true)));
    add(new Tuple2<>(SettingsPage.DONATION, new PageOptions("Donations", true)));
    add(new Tuple2<>(SettingsPage.DEBUG, new PageOptions("Debug", false)));
  }};

  private final DashboardStore store;
  private final StreamerEndpointProxy streamerEndpointProxy;
  private final ChatMateHudService chatMateHudService;

  private final Dim sidebarMaxWidth;
  private final AnimatedBool backgroundFadeIn;

  private final GeneralSectionElement generalSection;
  private final HudSectionElement hudSection;
  private final ChatSectionElement chatSection;
  private final DonationsSectionElement donationSection;
  private final DebugSectionElement debugSection;

  private final SidebarElement sidebar;
  private final ScrollingElement contentWrapper;
  private final ElementReference content;

  public ChatMateDashboardElement(InteractiveContext context,
                                  IElement parent,
                                  @Nullable DashboardRoute route,
                                  StreamerEndpointProxy streamerEndpointProxy,
                                  StatusService statusService,
                                  ApiRequestService apiRequestService,
                                  UserEndpointProxy userEndpointProxy,
                                  MessageService messageService,
                                  Config config,
                                  ChatMateHudService chatMateHudService,
                                  AccountEndpointProxy accountEndpointProxy,
                                  String dataFolder,
                                  DonationEndpointProxy donationEndpointProxy) {
    super(context, parent, LayoutMode.INLINE);
    super.setMargin(new RectExtension(ZERO, ZERO, gui(4), ZERO)); // stay clear of the HUD indicator
    super.setBorder(new RectExtension(gui(8)));
    super.setPadding(new RectExtension(gui(8)));

    this.store = new DashboardStore();
    this.streamerEndpointProxy = streamerEndpointProxy;
    this.chatMateHudService = chatMateHudService;

    this.sidebarMaxWidth = gui(80);
    this.backgroundFadeIn = new AnimatedBool(500L, false);
    this.backgroundFadeIn.set(true);

    this.generalSection = new GeneralSectionElement(context, this, castOrNull(GeneralRoute.class, route), this.streamerEndpointProxy, config, accountEndpointProxy);
    this.hudSection = new HudSectionElement(context, this, castOrNull(HudRoute.class, route), config, this.chatMateHudService);
    this.chatSection = new ChatSectionElement(context, this, castOrNull(ChatRoute.class, route), config);
    this.donationSection = new DonationsSectionElement(context, this, castOrNull(DonationRoute.class, route), statusService, apiRequestService, userEndpointProxy, messageService, donationEndpointProxy);
    this.debugSection = new DebugSectionElement(context, this, castOrNull(DebugRoute.class, route), config, context.urlService, dataFolder);

    this.sidebar = new SidebarElement(context, this, this.store, pageNames)
        .setMargin(new RectExtension(ZERO, gui(8), ZERO, ZERO))
        .setMaxWidth(this.sidebarMaxWidth)
        .cast();
    this.content = new ElementReference(context, this);
    this.contentWrapper = new ScrollingElement(context, this)
        .setElement(this.content);
    this.setContentSizes(this.context.dimFactory.getMinecraftSize());

    super.addElement(this.sidebar);
    super.addElement(this.contentWrapper);

    this.store.onSettingsPageChange(this::onSettingsPageChange);
    this.store.setSettingsPage(route ==  null ? SettingsPage.GENERAL : route.page);
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
      case CHAT:
        newElement = this.chatSection;
        break;
      case DONATION:
        newElement = this.donationSection;
        break;
      case DEBUG:
        newElement = this.debugSection;
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
    Dim dashboardContentHeight = super.getContentBoxWidth(windowSize.getY());
    this.contentWrapper.setMaxWidth(dashboardContentWidth.minus(this.sidebarMaxWidth));
    this.contentWrapper.setMaxHeight(dashboardContentHeight);

    Dim dashboardSidebarHeight = super.getContentBoxHeight(windowSize.getY());
    this.sidebar.setTargetHeight(dashboardSidebarHeight);
  }

  @Override
  public void onWindowResize(InteractiveEvent<ScreenSizeData> e) {
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
    // currently the dashboard is only accessible from within the minecraft menu, so fade between the background colours for a smooth experience
    Colour mcMenuBackgroundColour = new Colour(16, 16, 16, 200);
    Colour dashboardBackgroundColour = new Colour(0, 0, 20, 200);
    Colour colour = Colour.lerp(mcMenuBackgroundColour, dashboardBackgroundColour, this.backgroundFadeIn.getFrac());
    Colour borderColour = Colour.lerp(mcMenuBackgroundColour, new Colour(206, 212, 218), this.backgroundFadeIn.getFrac());

    // draw a background with a thick, curvy border ;)
    DimRect minecraftRect = this.context.dimFactory.getMinecraftRect();
    RectExtension margin = new RectExtension(screen(4));
    Dim borderWidth = screen(16);
    Dim cornerRadius = screen(16);
    RendererHelpers.drawRect(0, margin.applySubtractive(minecraftRect), colour, borderWidth, borderColour, cornerRadius);

    super.renderElement();
  }

  public interface ISectionElement extends IElement {
    void onShow();
    void onHide();
  }
}
