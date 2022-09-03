package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

public class LoadingSpinnerElement extends SingleElement {
  private Dim lineWidth;
  private float minGapRadians;
  private float maxGapRadians;
  private float rotationsPerSecond;
  private Colour colour;

  public LoadingSpinnerElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);
    super.setMaxContentWidth(gui(8));

    this.lineWidth = gui(2);
    this.minGapRadians = 2 * (float)Math.PI / 4;
    this.maxGapRadians = 6 * (float)Math.PI / 4;
    this.rotationsPerSecond = 1.5f;
    this.colour = new Colour(50, 200, 255);
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return null;
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    // content box will always be a square, constrained either by the width or height, if set
    @Nullable Dim maxHeight = super.getTargetContentHeight();
    if (maxHeight != null && maxHeight.lt(maxContentSize)) {
      maxContentSize = maxHeight;
    }

    return new DimPoint(maxContentSize, maxContentSize);
  }

  @Override
  protected void renderElement() {
    DimRect box = super.getContentBox();

    double t = new Date().getTime() / 1000d;

    // the gap oscillates with a period derived by the rotational period
    float gapFrequency = (float)Math.PI / 3.1f * 2.5f; // make sure it's not in phase with the rotation
    float gapRadians = ((float)Math.cos(2 * Math.PI * (t * rotationsPerSecond / gapFrequency % 1)) + 1) / 2 * (this.maxGapRadians - this.minGapRadians) + this.minGapRadians;

    // this determines the rotation of the construct
    float offsetRadians = (float)(2 * Math.PI * (t * rotationsPerSecond % 1));

    // expand the gap on both sides for a more symmetric feel
    float gapStart = (float)Math.PI * 2 - gapRadians / 2 + offsetRadians;
    float gapEnd = ((float)Math.PI * 2 + gapRadians / 2) + offsetRadians;

    Dim outerRadius = box.getWidth().over(2);
    Dim innerRadius = outerRadius.minus(this.lineWidth);
    RendererHelpers.drawPartialCircle(0, box.getCentre(), innerRadius, outerRadius, gapStart, gapEnd, this.colour, this.colour);
  }
}
