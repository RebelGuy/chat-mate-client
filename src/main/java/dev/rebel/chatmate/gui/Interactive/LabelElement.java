package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.util.Color;

import javax.annotation.Nullable;
import java.util.List;

public class LabelElement extends SingleElement {
  private String text;
  private TextAlignment alignment;
  private TextOverflow overflow;
  private SizingMode sizingMode;
  private Dim linePadding;
  private Colour colour;
  private float fontScale;
  private @Nullable Integer maxLines;

  private List<String> lines;

  public LabelElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);
    this.text = "";
    this.alignment = TextAlignment.LEFT;
    this.overflow = TextOverflow.TRUNCATE;
    this.sizingMode = SizingMode.MINIMISE;
    this.linePadding = context.dimFactory.fromGui(1);
    this.colour = new Colour(Color.WHITE);
    this.fontScale = 1.0f;
    this.maxLines = null;
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

  public LabelElement setSizingMode(SizingMode sizingMode) {
    this.sizingMode = sizingMode;
    this.onInvalidateSize();
    return this;
  }

  public LabelElement setColour(Colour colour) {
    this.colour = colour;
    return this;
  }

  public LabelElement setFontScale(float fontScale) {
    this.fontScale = Math.max(0, fontScale);
    this.onInvalidateSize();
    return this;
  }

  /** Only used if the overflow mode is SPLIT. */
  public LabelElement setMaxLines(@Nullable Integer maxLines) {
    this.maxLines = maxLines;
    this.onInvalidateSize();
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  /** Returns the full box width that would be required to contain the longest word of this label on a single line. */
  public Dim calculateWidthToFitLongestWord() {
    List<Integer> widths = Collections.map(Collections.list(this.text.split(" ")), this.context.fontRenderer::getStringWidth);
    Dim maxWidth = this.context.dimFactory.fromGui(this.fontScale * Collections.max(widths));
    return super.getFullBoxWidth(maxWidth);
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    FontRenderer font = this.context.fontRenderer;
    DimFactory factory = this.context.dimFactory;
    Dim fontHeight = factory.fromGui(font.FONT_HEIGHT);
    maxContentSize = maxContentSize.over(this.fontScale);

    Dim contentWidth;
    Dim contentHeight;
    if (this.overflow == TextOverflow.OVERFLOW) {
      this.addTextForRendering(this.text);
      int width = font.getStringWidth(this.text);
      contentWidth = Dim.min(factory.fromGui(width), maxContentSize);
      contentHeight = fontHeight;

    } else if (this.overflow == TextOverflow.TRUNCATE) {
      String text = this.text;
      int width = font.getStringWidth(text);
      if (width > maxContentSize.getGui()) {
        text = font.trimStringToWidth(text, (int) maxContentSize.getGui());
      }
      addTextForRendering(text);
      contentWidth = Dim.min(factory.fromGui(width), maxContentSize);
      contentHeight = fontHeight;

    } else if (this.overflow == TextOverflow.SPLIT) {
      List<String> lines = TextHelpers.splitText(this.text, (int) maxContentSize.getGui(), font);
      addTextLinesForRendering(lines);
      int actualMaxWidth = Collections.max(this.lines.stream().map(font::getStringWidth));
      contentWidth = factory.fromGui(actualMaxWidth);
      contentHeight = fontHeight.times(this.lines.size()).plus(this.linePadding.times(this.lines.size() - 1));

    } else {
      throw new RuntimeException("Invalid Overflow setting " + this.overflow);
    }

    return new DimPoint(this.sizingMode == SizingMode.FILL ? maxContentSize : contentWidth, contentHeight).scale(this.fontScale);
  }

  private void addTextForRendering(String text) {
    this.addTextLinesForRendering(Collections.list(text));
  }

  private void addTextLinesForRendering(List<String> lines) {
    if (this.overflow == TextOverflow.SPLIT && this.maxLines != null) {
      this.lines = Collections.trim(lines, this.maxLines);
    } else {
      this.lines = lines;
    }
  }

  @Override
  public void renderElement() {
    FontRenderer font = this.context.fontRenderer;
    DimFactory factory = this.context.dimFactory;

    Dim fontHeight = factory.fromGui(font.FONT_HEIGHT);
    DimRect box = this.getContentBox();
    Dim y = this.getContentBox().getY();

    for (String line : this.lines) {
      Dim width = factory.fromGui(font.getStringWidth(line) * this.fontScale); // todo: simplify scaling by creating a FontRender wrapper with extra options
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

      RendererHelpers.withMapping(new DimPoint(x, y), this.fontScale, () -> {
        font.drawStringWithShadow(line, 0, 0, this.colour.toInt());
      });

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
}
