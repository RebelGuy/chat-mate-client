package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.chat.ContainerChatComponent;
import dev.rebel.chatmate.gui.chat.ImageChatComponent;
import dev.rebel.chatmate.gui.chat.UserNameChatComponent;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import static dev.rebel.chatmate.gui.chat.ComponentHelpers.getFormattedText;

public class ChatComponentRenderer extends Gui {
  private final DimFactory dimFactory;
  private final FontEngine fontEngine;
  private final Minecraft minecraft;

  public ChatComponentRenderer(DimFactory dimFactory, FontEngine fontEngine, Minecraft minecraft) {
    this.dimFactory = dimFactory;
    this.fontEngine = fontEngine;
    this.minecraft = minecraft;
  }

  /** Returns the width of the drawn component. */
  public Dim drawChatComponent(IChatComponent component, Dim x, Dim y, int opacity) {
    if (component instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)component;
      return drawChatComponent(container.getComponent(), x, y, opacity);

    } else if (component instanceof ChatComponentText) {
      String formattedText = getFormattedText((ChatComponentText) component);
      Font font = new Font().withColour(Colour.WHITE.withAlpha(opacity)).withShadow(new Shadow(this.dimFactory));
      this.fontEngine.drawString(formattedText, x, y, font);
      return this.fontEngine.getStringWidthDim(formattedText);

    } else if (component instanceof ImageChatComponent) {
      ImageChatComponent imageComponent = (ImageChatComponent)component;
      Texture texture = imageComponent.getTexture();
      if (texture == null) {
        return x.setGui(0);
      }

      Dim requiredWidth = imageComponent.getRequiredWidth(this.fontEngine.FONT_HEIGHT_DIM);
      Dim targetHeight = this.dimFactory.fromGui(this.fontEngine.FONT_HEIGHT);
      Dim currentHeight = this.dimFactory.fromScreen(texture.height);
      Dim imageX = x.plus(imageComponent.paddingGuiLeft);
      Dim imageY = y;

      float scaleToReachTarget = targetHeight.getGui() / currentHeight.getGui();
      float scaleY = currentHeight.getGui() / 256 * scaleToReachTarget;
      float aspectRatio = imageComponent.getImageWidth(this.fontEngine.FONT_HEIGHT_DIM).over(targetHeight);
      float scaleX = aspectRatio * scaleY;

      GlStateManager.pushMatrix();
      GlStateManager.scale(scaleX, scaleY, 1);
      GlStateManager.enableBlend();

      // The following are required to prevent the rendered context menu from interfering with the status indicator colour..
      GlStateManager.color(1.0f, 1.0f, 1.0f, opacity < 4 ? 0 : opacity / 255.0f);
      GlStateManager.disableLighting();

      // undo the scaling
      float scaledX = imageX.over(scaleX).getGui();
      float scaledY = imageY.over(scaleY).getGui();
      int u = 0, v = 0;
      this.minecraft.getTextureManager().bindTexture(texture.resourceLocation);
      this.drawTexturedModalRect(scaledX, scaledY, u, v, 256, 256);

      GlStateManager.popMatrix();

      return requiredWidth;

    } else if (component instanceof UserNameChatComponent) {
      UserNameChatComponent userNameChatComponent = (UserNameChatComponent)component;
      return userNameChatComponent.renderComponent(x, y, opacity);

    } else {
      throw new RuntimeException("Cannot draw chat component of type " + component.getClass().getSimpleName());
    }
  }
}
