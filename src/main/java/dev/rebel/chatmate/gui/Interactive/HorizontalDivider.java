package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.Line;
import dev.rebel.chatmate.util.EnumHelpers;
import org.lwjgl.util.Color;

import java.util.List;

public class HorizontalDivider extends SingleElement {
  private Dim thickness;
  private Colour colour;
  private FillMode mode;

  public HorizontalDivider(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.thickness = context.dimFactory.fromGui(0.5f);
    this.mode = FillMode.PARENT_CONTENT;
    this.colour = new Colour(Color.BLACK);
  }

  public HorizontalDivider setThickness(Dim thickness) {
    this.thickness = thickness;
    return this;
  }

  public HorizontalDivider setColour(Colour colour) {
    this.colour = colour;
    return this;
  }

  public HorizontalDivider setMode(FillMode mode) {
    this.mode = mode;
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    // cheating a little - the maxWidth may actually be wider than this, but the important part is that we fill the available width completely
    return new DimPoint(maxContentSize, this.thickness);
  }

  @Override
  protected void renderElement() {
    Dim y = this.getBox().getY().plus(this.thickness.over(2));
    Dim x1, x2;
    if (this.mode == FillMode.PARENT_CONTENT) {
      x1 = getContentBox(this.parent).getX();
      x2 = getContentBox(this.parent).getRight();
    } else if (this.mode == FillMode.PARENT_COLLISION) {
      x1 = getCollisionBox(this.parent).getX();
      x2 = getCollisionBox(this.parent).getRight();
    } else if (this.mode == FillMode.PARENT_FULL) {
      x1 = getFullBox(this.parent).getX();
      x2 = getFullBox(this.parent).getRight();
    } else {
      throw EnumHelpers.<FillMode>assertUnreachable(this.mode);
    }

    Line line = new Line(new DimPoint(x1, y), new DimPoint(x2, y));
    RendererHelpers.drawLine(this.getZIndex(), line, this.thickness, this.colour, this.colour, false);
  }

  public enum FillMode {
    PARENT_CONTENT,
    PARENT_COLLISION,
    PARENT_FULL
  }
}
