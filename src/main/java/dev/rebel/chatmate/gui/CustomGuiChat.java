package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.models.Config;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class CustomGuiChat extends GuiChat {
  private final Config config;

  public CustomGuiChat(Config config, String defaultInput) {
    super(defaultInput);
    this.config = config;
  }

  @Override
  protected void mouseClicked(int x, int y, int button) throws IOException {
    if (button == 0) {
      IChatComponent ichatcomponent = this.getChatComponent(Mouse.getX(), Mouse.getY());
      if (this.handleComponentClick(ichatcomponent)) {
        return;
      }
    }

    this.inputField.mouseClicked(x, y, button);

    // stop the call sequence here. Stay far away from super, and don't need to call GuiScreen.mouseClicked because
    // it handles only the click events for buttons, of which we have none in this screen.
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawRect(2, this.height - 14, this.width - 2, this.height - 2, -2147483648);
    this.inputField.drawTextBox();
    IChatComponent ichatcomponent = this.getChatComponent(Mouse.getX(), Mouse.getY());
    if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) {
      this.handleComponentHover(ichatcomponent, mouseX, mouseY);
    }

    // again, stay away from super and don't call GuiScreen.drawScreen because it handles only drawing of buttons
    // and labels, of which we have none in this screen.
  }

  // Dirty hack, but not my fault.
  // When getting chat components at a position in the GuiNewChat Gui element (i.e. focussed chat), Minecraft assumes
  // that the chat window's bottom value is a fixed y-value. so we have to transform the mouseY value to the coordinate
  // it would be if chatOffset was zero.
  private IChatComponent getChatComponent(int mouseX, int mouseY) {
    int offset = this.config.getChatVerticalDisplacement().get();

    // the offset is in gui coordinate units, which may be scaled, but we need to convert it to screen coordinate units.
    ScaledResolution scaledResolution = new ScaledResolution(this.mc);
    int offsetScreen = offset * scaledResolution.getScaleFactor();

    // y-space is inverted, so we have to subtract to move the pointer down
    mouseY -= offsetScreen;

    return this.mc.ingameGUI.getChatGUI().getChatComponent(mouseX, mouseY);
  }
}
