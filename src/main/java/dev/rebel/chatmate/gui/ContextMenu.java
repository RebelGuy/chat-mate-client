package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.util.Memoiser;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.Color;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraftforge.fml.client.config.GuiUtils.drawGradientRect;

public class ContextMenu {
  private final static int LINE_HEIGHT = 10;

  private final Memoiser memoiser;

  public final Dim x;
  public final Dim y;
  public final ContextMenuOption[] options;
  public List<OptionBox> boxes;

  public ContextMenu(Dim x, Dim y, ContextMenuOption[] options) {
    this.memoiser = new Memoiser();

    this.x = x;
    this.y = y;
    this.options = options;
  }

  public boolean handleClick(Dim mouseX, Dim mouseY) {
    if (this.boxes != null) {
      for (OptionBox box : this.boxes) {
        if (box.testPosition(mouseX, mouseY)) {
          System.out.println("Clicked on " + box.textLines.get(0));
          box.option.onSelect.run();
          return true;
        }
      }
    }

    return false;
  }

  // Adapted from GuiUtils::drawHoveringText.
  // todo: use float values for positions/distances instead
  public void drawMenu(Dim mouseX, Dim mouseY, final int screenWidth, final int screenHeight, final int maxTextWidth, FontEngine font)
  {
    int x = (int)this.x.getGui();
    int y = (int)this.y.getGui();

    int minTooltipWidth = 50;
    int tooltipX = x + 12;
    int tooltipY = y - 12;
    int minTooltipHeight = 8;

    List<OptionBox> boxes = this.constructBoxes(screenWidth, screenHeight, maxTextWidth, font);
    Tuple2<Integer, Integer> dimensions = OptionBox.calculateBoxSize(boxes);
    int tooltipWidth = Math.max(dimensions._1, minTooltipWidth);
    int tooltipHeight = Math.max(dimensions._2, minTooltipHeight);

    GlStateManager.pushMatrix();
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();

    this.drawContextBox(tooltipX, tooltipY, tooltipWidth, tooltipHeight);
    this.drawOptions(boxes, tooltipX, tooltipY, mouseX, mouseY, font);

    GlStateManager.enableDepth();
    GlStateManager.popMatrix();
  }

  private void drawContextBox(int tooltipX, int tooltipY, int tooltipWidth, int tooltipHeight) {
    final int zLevel = 300;
    final int backgroundColor = 0xF0100010;
    drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
    drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
    drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
    drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
    drawGradientRect(zLevel, tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
    final int borderColorStart = 0x505000FF;
    final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
    drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
    drawGradientRect(zLevel, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
    drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
    drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
  }

  /** Draws the options, starting at the given x-y position. */
  private void drawOptions(List<OptionBox> boxes, int x, int y, Dim mouseX, Dim mouseY, FontEngine font) {
    for (OptionBox box : boxes) {
      boolean hoveringOverBox = box.testPosition(mouseX, mouseY);
      int color = hoveringOverBox ? -1 : new Colour(Color.LTGREY).toInt();

      for (String line : box.textLines) {
        font.drawStringWithShadow(line, (float)x, (float)y, color);
        y += LINE_HEIGHT;
      }
    }
  }

  private List<OptionBox> constructBoxes(int screenWidth, int screenHeight, int maxTextWidth, FontEngine font) {
    return this.memoiser.memoise("Context menu hitboxes", () -> {

      List<OptionBox> optionBoxes = new ArrayList<>();
      if (this.options.length == 0) {
        return optionBoxes;
      }

      int x = (int)this.x.getGui();
      int y = (int)this.y.getGui();

      int tooltipWidth = 50;
      int tooltipY = y - 12;
      int tooltipHeight = 8;

      int tooltipX = x + 12;
      int effectiveMaxWidth = maxTextWidth;
      if (tooltipX + maxTextWidth + 4 > screenWidth) {
        effectiveMaxWidth = screenWidth - 4 - tooltipX;
      }

      if (effectiveMaxWidth < 12) {
        effectiveMaxWidth = 12;
      }

      // first pass: get flush dimensions of the context menu
      String[][] optionLines = new String[this.options.length][];
      for (int i = 0; i < this.options.length; i++) {
        List<String> lines = font.listFormattedStringToWidth(this.options[i].text, effectiveMaxWidth);
        optionLines[i] = lines.toArray(new String[0]);

        for (String line : lines) {
          int textLineWidth = font.getStringWidth(line);
          if (textLineWidth > tooltipWidth) {
            tooltipWidth = textLineWidth;
          }
        }
      }

      tooltipHeight += Collections.sum(Arrays.asList(optionLines), l -> l.length);
      if (tooltipY + tooltipHeight + 6 > screenHeight) {
        tooltipY = screenHeight - tooltipHeight - 6;
      }

      // second pass: now that we know the dimensions of the menu, construct the option boxes
      int relY = 0;
      for (int i = 0; i < this.options.length; i++) {
        String[] lines = optionLines[i];
        int optionHeight = lines.length * LINE_HEIGHT;
        optionBoxes.add(new OptionBox(tooltipX, tooltipY + relY, tooltipWidth, optionHeight, this.options[i], Arrays.asList(lines)));
        relY += optionHeight;
      }

      this.boxes = optionBoxes;
      return optionBoxes;
    }, this.options, screenWidth, screenHeight);
  }

  public static class ContextMenuOption {
    public final String text;
    public final Runnable onSelect;

    public ContextMenuOption(String text, Runnable onSelect) {
      this.text = text;
      this.onSelect = onSelect;
    }
  }

  public static class OptionBox {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public final ContextMenuOption option;
    public final List<String> textLines;
    // todo: add optional title text - will not be clickable, but required to calculate final box size

    public OptionBox(int x, int y, int width, int height, ContextMenuOption option, List<String> renderedTextLines) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;

      this.option = option;
      this.textLines = renderedTextLines;
    }

    /** Returns true if the position is within the hitbox area or exactly on its boundaries (except the bottom boundary). */
    public boolean testPosition(Dim x, Dim y) {
      return x.getGui() >= this.x && x.getGui() <= this.x + this.width && y.getGui() >= this.y && y.getGui() < this.y + this.height;
    }

    public static Tuple2<Integer, Integer> calculateBoxSize(List<OptionBox> boxes) {
      int height = Collections.sum(boxes, b -> b.height);
      OptionBox widest = Collections.eliminate(boxes, (a, b) -> a.width > b.width ? a : b);
      int width = widest.width;

      return new Tuple2<>(width, height);
    }
  }
}
