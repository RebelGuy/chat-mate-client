package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.IHudComponent;
import dev.rebel.chatmate.gui.hud.LiveViewersComponent;
import dev.rebel.chatmate.gui.hud.StatusIndicatorComponent;
import dev.rebel.chatmate.gui.hud.TitleComponent;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.events.ForgeEventService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.List;

/** This is the game overlay class - for the focussed menu screen, go to GuiChatMateHudScreen */
public class GuiChatMateHud {
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final ForgeEventService forgeEventService;
  private final StatusService statusService;

  private final StatusIndicatorComponent statusIndicatorComponent;
  private final LiveViewersComponent liveViewersComponent;
  private final TitleComponent titleComponent;

  public final List<IHudComponent> hudComponents;

  public GuiChatMateHud(Minecraft minecraft, DimFactory dimFactory, ForgeEventService forgeEventService, StatusService statusService, Config config) {
    super();
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;
    this.forgeEventService = forgeEventService;
    this.statusService = statusService;

    this.statusIndicatorComponent = new StatusIndicatorComponent(dimFactory, 0.5f, statusService, config);
    this.liveViewersComponent = new LiveViewersComponent(dimFactory, 1, statusService, config, minecraft);

    this.titleComponent = new TitleComponent(dimFactory, minecraft, true, true);

    this.hudComponents = new ArrayList<>();
    this.hudComponents.add(this.statusIndicatorComponent);
    this.hudComponents.add(this.liveViewersComponent);
    this.hudComponents.add(this.titleComponent);
  }

  public TitleComponent getTitleComponent() {
    return this.titleComponent;
  }

  // render indicators here, etc.
  // will need to check config to see if an indicator should be rendered or not, as well as its transform and content
  public void renderGameOverlay() {
    RenderContext context = new RenderContext(this.minecraft.renderEngine);

    for (IHudComponent component : this.hudComponents) {
      component.render(context);
    }
  }
}
