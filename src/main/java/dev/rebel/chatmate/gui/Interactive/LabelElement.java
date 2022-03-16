package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.util.Color;

import java.util.ArrayList;
import java.util.List;

public class LabelElement extends SingleElement {
  private String text;
  private TextAlignment alignment;
  private TextOverflow overflow;
  private LayoutMode layoutMode;
  private Dim linePadding;
  private Colour colour;

  private List<String> lines;

  public LabelElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);
    this.text = "";
    this.alignment = TextAlignment.LEFT;
    this.overflow = TextOverflow.TRUNCATE;
    this.layoutMode = LayoutMode.FIT;
    this.linePadding = context.dimFactory.fromGui(1);
    this.colour = new Colour(Color.WHITE);
  }

  public LabelElement setText(String text) {
    this.text = text;
    this.onInvalidateSize();
    return this;
  }

  public String getText() {
    return this.text;
  }

  public LabelElement setAlignment(TextAlignment alignment) {
    this.alignment = alignment;
    this.onInvalidateSize();
    return this;
  }

  public LabelElement setOverflow(TextOverflow overflow) {
    this.overflow = overflow;
    this.onInvalidateSize();
    return this;
  }

  public LabelElement setLinePadding(Dim linePadding) {
    this.linePadding = linePadding;
    this.onInvalidateSize();
    return this;
  }

  public LabelElement setLayoutMode(LayoutMode layoutMode) {
    this.layoutMode = layoutMode;
    return this;
  }

  public LabelElement setColour(Colour colour) {
    this.colour = colour;
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    FontRenderer font = this.context.fontRenderer;
    DimFactory factory = this.context.dimFactory;

    Dim fontHeight = factory.fromGui(font.FONT_HEIGHT);
    maxWidth = this.getContentBoxWidth(maxWidth);

    Dim contentWidth;
    Dim contentHeight;
    this.lines = new ArrayList<>();
    if (this.overflow == TextOverflow.OVERFLOW) {
      this.lines.add(this.text);
      int width = font.getStringWidth(this.text);
      contentWidth = Dim.min(factory.fromGui(width), maxWidth);
      contentHeight = fontHeight;

    } else if (this.overflow == TextOverflow.TRUNCATE) {
      String text = this.text;
      int width = font.getStringWidth(text);
      if (width > maxWidth.getGui()) {
        text = font.trimStringToWidth(text, (int)maxWidth.getGui());
      }
      this.lines.add(text);
      contentWidth = Dim.min(factory.fromGui(width), maxWidth);
      contentHeight = fontHeight;

    } else if (this.overflow == TextOverflow.SPLIT) {
      this.lines = TextHelpers.splitText(this.text, (int) maxWidth.getGui(), font);
      int actualMaxWidth = Collections.max(this.lines.stream().map(font::getStringWidth));
      contentWidth = factory.fromGui(actualMaxWidth);
      contentHeight = fontHeight.times(this.lines.size()).plus(this.linePadding.times(this.lines.size() - 1));

    } else {
      throw new RuntimeException("Invalid Overflow setting " + this.overflow);
    }

    return this.setLastCalculatedSize(this.getFullBoxSize(new DimPoint(this.layoutMode == LayoutMode.FIT ? contentWidth : maxWidth, contentHeight)));
  }

  @Override
  protected DimPoint setLastCalculatedSize(DimPoint size) {
    this.lastCalculatedSize = size;
    return size;
  }

  @Override
  public void renderElement() {
    FontRenderer font = this.context.fontRenderer;
    DimFactory factory = this.context.dimFactory;

    Dim fontHeight = factory.fromGui(font.FONT_HEIGHT);
    DimRect box = this.getContentBox();
    Dim y = this.getContentBox().getY();

    for (String line : this.lines) {
      Dim width = factory.fromGui(font.getStringWidth(line));
      Dim x;
      if (this.alignment == TextAlignment.LEFT) {
        x = box.getX();
      } else if (this.alignment == TextAlignment.CENTRE) {
        x = box.getX().plus(box.getWidth().minus(width).over(2));
      } else if (this.alignment == TextAlignment.RIGHT) {
        x = box.getX().plus(box.getWidth()).minus(width);
      } else {
        throw new RuntimeException("Invalid TextAlignment " + this.alignment);
      }

      font.drawStringWithShadow(line, x.getGui(), y.getGui(), this.colour.toInt());
      y = y.plus(fontHeight).plus(this.linePadding);
    }
  }

  /** How should the text fill the Label's content box? */
  public enum TextAlignment {
    LEFT,
    CENTRE,
    RIGHT
  }

  /** What happens if the text goes beyond the content box? */
  public enum TextOverflow {
    OVERFLOW,
    SPLIT,
    TRUNCATE
  }

  /** How should the Label calculate its width, given the parent's maxWidth? */
  public enum LayoutMode {
    FIT, // the element's width will be calculated to fit the text
    FULL_WIDTH // the element's width will always take up 100% of the provided width
  }
}
