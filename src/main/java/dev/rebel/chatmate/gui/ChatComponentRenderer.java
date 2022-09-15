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

  public int drawChatComponent(IChatComponent component, int x, int y, int opacity) {
    if (component instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)component;
      return drawChatComponent(container.getComponent(), x, y, opacity);

    } else if (component instanceof ChatComponentText) {
      String formattedText = getFormattedText((ChatComponentText) component);
      Font font = new Font().withColour(Colour.WHITE.withAlpha(opacity)).withShadow(new Shadow(this.dimFactory));
      this.fontEngine.drawString(formattedText, x, y, font);
      return this.fontEngine.getStringWidth(formattedText);

    } else if (component instanceof ImageChatComponent) {
      ImageChatComponent imageComponent = (ImageChatComponent)component;
      Texture texture = imageComponent.getTexture();
      if (texture == null) {
        return 0;
      }

      // if the image is not square, it's possible that it will not be positioned properly in the dedicated space in the chat line
      // due to rounding error. the following adjustment will fix that
      float requiredWidth = imageComponent.getRequiredWidth(this.fontEngine.FONT_HEIGHT);
      int requiredWidthInt = (int)Math.ceil(requiredWidth);
      float xAdjustment = (requiredWidthInt - requiredWidth) / 2;

      Dim targetHeight = this.dimFactory.fromGui(this.fontEngine.FONT_HEIGHT);
      Dim currentHeight = this.dimFactory.fromScreen(texture.height);
      float imageX = x + imageComponent.paddingGuiLeft + xAdjustment;
      float imageY = y;

      float scaleToReachTarget = targetHeight.getGui() / currentHeight.getGui();
      float scaleY = currentHeight.getGui() / 256 * scaleToReachTarget;
      float aspectRatio = imageComponent.getImageWidth(this.fontEngine.FONT_HEIGHT) / targetHeight.getGui();
      float scaleX = aspectRatio * scaleY;

      GlStateManager.pushMatrix();
      GlStateManager.scale(scaleX, scaleY, 1);
      GlStateManager.enableBlend();

      // The following are required to prevent the rendered context menu from interfering with the status indicator colour..
      GlStateManager.color(1.0f, 1.0f, 1.0f, opacity < 4 ? 0 : opacity / 255.0f);
      GlStateManager.disableLighting();

      // undo the scaling
      float scaledX = imageX / scaleX;
      float scaledY = imageY / scaleY;
      int u = 0, v = 0;
      this.minecraft.getTextureManager().bindTexture(texture.resourceLocation);
      this.drawTexturedModalRect(scaledX, scaledY, u, v, 256, 256);

      GlStateManager.popMatrix();

      return requiredWidthInt;

    } else if (component instanceof UserNameChatComponent) {
      UserNameChatComponent userNameChatComponent = (UserNameChatComponent)component;
      return userNameChatComponent.renderComponent(this.dimFactory.fromGui(x), this.dimFactory.fromGui(y), opacity);

    } else {
      throw new RuntimeException("Cannot draw chat component of type " + component.getClass().getSimpleName());
    }
  }
}
