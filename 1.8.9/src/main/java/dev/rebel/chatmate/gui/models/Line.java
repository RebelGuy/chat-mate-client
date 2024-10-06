package dev.rebel.chatmate.gui.models;

import dev.rebel.chatmate.util.Collections;

import java.util.ArrayList;
import java.util.List;

public class Line {
  public final DimPoint from;
  public final DimPoint to;

  public Line(DimPoint from, DimPoint to) {
    this.from = from;
    this.to = to;
  }

  /** Returns the angle in radians, where positive values go clockwise, and 0 means the line is going horizontally to the right. */
  public float getAngle() {
    // ignore the fact that y is inverted.
    float x = this.to.minus(this.from).getX().getGui();
    float y = this.to.minus(this.from).getY().getGui();

    return (float)(Math.atan2(y, x) + Math.PI);
  }

  /** Gets the outline in counter-clockwise order, [from, from, to, to]. */
  public List<Line> getOutline(Dim padding, boolean includeCaps) {
    if (includeCaps) {
      throw new RuntimeException("NYI");
    }

    float angle = this.getAngle();
    float theta1 = angle + (float)Math.PI / 2;
    float theta2 = angle - (float)Math.PI / 2;

    float shortOffset = padding.getGui() / 2;

    float x1 = this.from.getX().getGui();
    float y1 = this.from.getY().getGui();
    float x2 = this.to.getX().getGui();
    float y2 = this.to.getY().getGui();

    float dx1 = (float)Math.cos(theta1) * shortOffset;
    float dx2 = (float)Math.cos(theta2) * shortOffset;
    float dy1 = (float)Math.sin(theta1) * shortOffset;
    float dy2 = (float)Math.sin(theta2) * shortOffset;

    DimPoint p1 = new DimPoint(this.from.getX().setGui(x1 + dx1), this.from.getY().setGui(y1 + dy1)); // from+
    DimPoint p2 = new DimPoint(this.from.getX().setGui(x1 + dx2), this.from.getY().setGui(y1 + dy2)); // from-
    DimPoint p3 = new DimPoint(this.to.getX().setGui(x2 + dx2), this.to.getY().setGui(y2 + dy2)); // to-
    DimPoint p4 = new DimPoint(this.to.getX().setGui(x2 + dx1), this.to.getY().setGui(y2 + dy1)); // to+

    List<Line> lines = new ArrayList<>();
    lines.add(new Line(p1, p2));
    lines.add(new Line(p2, p3));
    lines.add(new Line(p3, p4));
    lines.add(new Line(p4, p1));
    return lines;
  }

  public Line invert() {
    return new Line(this.to, this.from);
  }

  @Override
  public String toString() {
    return String.format("[%s, %s]", this.from.toString(), this.to.toString());
  }

  public static List<Line> reverse(List<Line> lines) {
    return Collections.map(Collections.reverse(lines), line -> new Line(line.to, line.from));
  }
}
