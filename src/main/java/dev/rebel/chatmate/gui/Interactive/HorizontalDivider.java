package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.Line;
import org.lwjgl.util.Color;

public class HorizontalDivider extends SingleElement {
  private Dim thickness;
  private Colour colour;
  private SizingMode mode;

  public HorizontalDivider(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.thickness = context.dimFactory.fromGui(1);
    this.mode = SizingMode.PARENT_CONTENT;
    this.colour = new Colour(Color.GREY);
  }

  public HorizontalDivider setThickness(Dim thickness) {
    this.thickness = thickness;
    return this;
  }

  public HorizontalDivider setColour(Colour colour) {
    this.colour = colour;
    return this;
  }

  public HorizontalDivider setMode(SizingMode mode) {
    this.mode = mode;
    return this;
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    // cheating a little - the maxWidth may actually be wider than this, but the important part is that we will the available width completely
    return new DimPoint(maxWidth, this.thickness);
  }

  @Override
  public void render() {
    Dim y = this.getBox().getY().plus(this.thickness.over(2));
    Dim x1, x2;
    if (this.mode == SizingMode.PARENT_CONTENT) {
      x1 = this.getContentBox().getX();
      x2 = this.getContentBox().getRight();
    } else if (this.mode == SizingMode.PARENT_COLLISION) {
      x1 = this.getCollisionBox().getX();
      x2 = this.getCollisionBox().getRight();
    } else if (this.mode == SizingMode.PARENT_FULL) {
      x1 = this.getBox().getX();
      x2 = this.getBox().getRight();
    } else {
      throw new RuntimeException("Invalid SizingMode " + this.mode);
    }

    Line line = new Line(new DimPoint(x1, y), new DimPoint(x2, y));
    RendererHelpers.drawLine(line, this.thickness, this.colour, false);
  }

  public enum SizingMode {
    PARENT_CONTENT,
    PARENT_COLLISION,
    PARENT_FULL
  }
}
