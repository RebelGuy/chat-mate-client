package dev.rebel.chatmate.services;

import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay.Options;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.util.function.Consumer;

public class RenderService {
  // todo: can add proper laying out of boxed components with text alignments, padding, automatic wrapping (Using fontRenderer), etc.
  // e.g. boxes will be anchored to preset points of the screen (corners, centres, etc), and reposition automatically
  // if more boxes are added or the screen size changes.
  // use fontRenderer.getStringWidth() for aligning text

  private final Minecraft minecraft;
  private final ForgeEventService forgeEventService;

  public RenderService(Minecraft minecraft, ForgeEventService forgeEventService) {
    this.minecraft = minecraft;
    this.forgeEventService = forgeEventService;

    this.registerHandlers();
  }

  public DrawnText drawText(int x, int y, String... lines) {
    DrawnText text = new DrawnText(this::updateText, lines);

    // we can use GL11 (OpenGL) directly if we want
    // e.g. https://relativity.net.au/gaming/java/Transparency.html
//    this.minecraft.ingameGUI.drawString();
    this.minecraft.fontRendererObj.drawString("test", 2, 2, 0xFFFFFFFF, false);
//    this.minecraft.fontRendererObj.drawStringWithShadow("Test! THIS IS A TEST!!!! LOLOLOLOLOLOL", 100, 100, 0xFF0000);
    return text;
  }

  private void registerHandlers() {
    this.forgeEventService.onRenderGameOverlay(this::onRenderGameOverlay, new Options(ElementType.ALL));
  }

  private RenderGameOverlay.Out onRenderGameOverlay(RenderGameOverlay.In eventIn) {
    this.minecraft.fontRendererObj.drawString("test", 2, 2, 0xFFFFFFFF, false);
    return null;
  }

  private void updateText(DrawnText text) {

  }

  public static class DrawnText {
    private final Consumer<DrawnText> onUpdateCallback;

    private String[] lines;
    private boolean isVisible;

    public DrawnText(Consumer<DrawnText> onUpdate, String... lines) {
      this.onUpdateCallback = onUpdate;

      this.lines = lines;
      this.isVisible = true;
    }

    public void updateLine(int lineNumber, String newText) {
      this.lines[lineNumber] = newText;
      this.onUpdate();
    }

    public void hide() {
      this.isVisible = false;
      this.onUpdate();
    }

    public void show() {
      this.isVisible = true;
      this.onUpdate();
    }

    // pure read-only properties are "not possible in Java"
    // https://stackoverflow.com/questions/20623184/can-i-create-read-only-properties-in-java/20623213
    // I quit
    public boolean isVisible() {
      return this.isVisible;
    }

    private void onUpdate() {
      this.onUpdateCallback.accept(this);
    }
  }
}
