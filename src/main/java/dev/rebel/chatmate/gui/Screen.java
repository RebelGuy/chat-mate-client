package dev.rebel.chatmate.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

// does NOT fire the following events:
// - InitGuiEvent.Pre and InitGuiEvent.Post
// - ActionPerformedEvent.Pre and ActionPerformedEvent.Post

@SideOnly(Side.CLIENT)
public abstract class Screen extends GuiScreen
{
  private int eventButton;
  private long lastMouseEvent;

  public Screen() {
  }

  /** Called when the GUI is displayed and when the window resizes. */
  @Override
  public abstract void initGui();

  /** Called from the main game loop to update the screen EVERY TICK. */
  @Override
  public final void updateScreen() { }

  /** Called from forge to draw all components to the screen EVERY FRAME. */
  @Override
  public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);

  /** Called when the screen is unloaded (e.g. when menu closed, or replaced with other screen). */
  @Override
  public abstract void onGuiClosed();

  /** Called when the screen dimensions change. */
  protected void onScreenSizeUpdated() { }

  /** Call this when the screen should be closed. Note that this will still emit a Screen::onGuiClosed event. */
  protected final void closeScreen() {
    this.mc.displayGuiScreen(null);

    if (this.mc.currentScreen == null)
    {
      this.mc.setIngameFocus();
    }
  }

  /** Causes the screen to lay out its subcomponents again. */
  @Override
  public final void setWorldAndResolution(Minecraft mc, int width, int height)
  {
    this.mc = mc;
    this.itemRender = mc.getRenderItem();
    this.fontRendererObj = mc.fontRendererObj;
    this.width = width;
    this.height = height;
    this.initGui();
  }

  @Override
  public final void setGuiSize(int w, int h)
  {
    boolean hasUpdated = this.width != w || this.height != h;
    this.width = w;
    this.height = h;
    if (hasUpdated) {
      this.onScreenSizeUpdated();
    }
  }

  @Override
  public void onResize(Minecraft mc, int w, int h)
  {
    if (this.mc != mc) {
      this.setWorldAndResolution(mc, w, h);
    } else {
      this.setGuiSize(w, h);
    }
  }

  /** For now, use the super implementation.*/
  @Override
  public void handleInput() throws IOException {
    // *sigh* we need to manually fire off the input events, otherwise the underlying GUI screen will do some extra
    // behaviour that we don't want, and also because otherwise the minecraft game loop will behave oddly.
    if (Mouse.isCreated()) {
      while(Mouse.next()) {
        if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Pre(this))) {
          if (this.equals(this.mc.currentScreen)) {
            MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Post(this));
          }
        }
      }
    }

    if (Keyboard.isCreated()) {
      while(Keyboard.next()) {
        if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Pre(this))) {
          if (this.equals(this.mc.currentScreen)) {
            MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Post(this));
          }
        }
      }
    }

  }

  /**
   * Handles mouse input.
   */
  @Override
  public void handleMouseInput() throws IOException
  {
    int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
    int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
    int k = Mouse.getEventButton();

    if (Mouse.getEventButtonState())
    {
      this.eventButton = k;
      this.lastMouseEvent = Minecraft.getSystemTime();
      this.mouseClicked(i, j, this.eventButton);
    }
    else if (k != -1)
    {
      this.eventButton = -1;
      this.mouseReleased(i, j, k);
    }
    else if (this.eventButton != -1 && this.lastMouseEvent > 0L)
    {
      long l = Minecraft.getSystemTime() - this.lastMouseEvent;
      this.mouseClickMove(i, j, this.eventButton, l);
    }
  }

  /**
   * Handles keyboard input.
   */
  @Override
  public void handleKeyboardInput() throws IOException
  {
    if (Keyboard.getEventKeyState())
    {
      // todo: move to event service
      this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
    }

    this.mc.dispatchKeypresses();
  }

  @Override
  public void drawWorldBackground(int tint)
  {
    if (this.mc.theWorld != null)
    {
      this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
    }
    else
    {
      this.drawBackground(tint);
    }
  }

  /**
   * Draws the background (i is always 0 as of 1.2.2)
   */
  @Override
  public void drawBackground(int tint)
  {
    GlStateManager.disableLighting();
    GlStateManager.disableFog();
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    this.mc.getTextureManager().bindTexture(optionsBackground);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    float f = 32.0F;
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
    worldrenderer.pos(0.0D, (double)this.height, 0.0D).tex(0.0D, (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
    worldrenderer.pos((double)this.width, (double)this.height, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
    worldrenderer.pos((double)this.width, 0.0D, 0.0D).tex((double)((float)this.width / 32.0F), (double)tint).color(64, 64, 64, 255).endVertex();
    worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, (double)tint).color(64, 64, 64, 255).endVertex();
    tessellator.draw();
  }

  @Override
  public boolean doesGuiPauseGame()
  {
    return false;
  }
}
