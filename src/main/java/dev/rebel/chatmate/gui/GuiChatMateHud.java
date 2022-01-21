package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.IHudComponent;
import dev.rebel.chatmate.gui.hud.StatusIndicatorComponent;
import dev.rebel.chatmate.services.events.ForgeEventService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

/** This is the game overlay class - for the focussed menu screen, go to GuiChatMateHudScreen */
public class GuiChatMateHud extends Gui {
  private final Minecraft minecraft;
  private final ForgeEventService forgeEventService;

  private final StatusIndicatorComponent statusIndicatorComponent;

  public final List<IHudComponent> hudComponents;

  public GuiChatMateHud(Minecraft minecraft, ForgeEventService forgeEventService) {
    super();
    this.minecraft = minecraft;
    this.forgeEventService = forgeEventService;

    this.statusIndicatorComponent = new StatusIndicatorComponent();

    this.hudComponents = new ArrayList<>();
    this.hudComponents.add(this.statusIndicatorComponent);
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
