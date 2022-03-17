package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.Line;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static dev.rebel.chatmate.gui.Interactive.ElementBase.*;
import static dev.rebel.chatmate.gui.Interactive.ElementBase.getBorderBox;

public class ElementHelpers {
  /** Traverses the tree from the parent, keeping track of the path, and picking the first child whose bounds intersect the given point.
   * Returns if a child has no children of its own, or if none of the children intersect the point. */
  public static List<IElement> getElementsAtPoint(IElement parent, DimPoint point) {
    List<IElement> result = Collections.list(parent);

    while (true) {
      List<IElement> children = parent.getChildren();
      if (children == null || children.size() == 0) {
        return result;
      }

      // reversing the order of the children ensures that, if two children are drawn at the same position, the one that
      // was most likely added last is picked. in the future, we could use z indexes for a more robust solution.
      boolean foundMatch = false;
      for (IElement child : Collections.reverse(children)) {
        if (getCollisionBox(child).checkCollision(point)) {
          result.add(child);
          foundMatch = true;
          parent = child;
          break;
        }
      }

      if (!foundMatch) {
        return result;
      }
    }
  }

  public static @Nullable List<IElement> findElementFromChild(IElement child, IElement toFind) {
    List<IElement> result = Collections.list(child);
    if (child == toFind) {
      return result;
    }

    while (true) {
      IElement parent = child.getParent();
      if (parent == null) {
        return null;
      }

      result.add(parent);
      if (parent == toFind) {
        return result;
      } else {
        child = parent;
      }
    }
  }

  public static void renderDebugInfo(IElement element, InteractiveContext context) {
    IElement parent = element.getParent();
    Dim ZERO = context.dimFactory.zeroGui();

    Dim borderWidth = context.dimFactory.fromScreen(1);
    Colour borderColour = Colour.BLACK.withAlpha(0.2f);
    if (parent != null) {
      RendererHelpers.renderRectWithCutout(1000, getContentBox(parent), element.getBox(), Colour.YELLOW.withAlpha(0.2f), borderWidth, borderColour);
    }
    RendererHelpers.renderRectWithCutout(1000, element.getBox(), getBorderBox(element), Colour.RED.withAlpha(0.2f), borderWidth, borderColour);
    RendererHelpers.renderRectWithCutout(1000, getBorderBox(element), getPaddingBox(element), Colour.ORANGE.withAlpha(0.2f), borderWidth, borderColour);
    RendererHelpers.renderRectWithCutout(1000, getPaddingBox(element), getContentBox(element), Colour.GREEN.withAlpha(0.2f), borderWidth, borderColour);
    RendererHelpers.drawRect(1000, getContentBox(element), Colour.BLUE.withAlpha(0.2f), borderWidth, borderColour);

    DimPoint mousePos = context.mousePosition;
    if (mousePos != null) {
      mousePos = mousePos.setAnchor(DimAnchor.GUI);
      DimPoint screen = context.dimFactory.getMinecraftSize();
      RendererHelpers.drawLine(new Line(new DimPoint(ZERO, mousePos.getY()), new DimPoint(screen.getX(), mousePos.getY())), borderWidth, borderColour, false);
      RendererHelpers.drawLine(new Line(new DimPoint(mousePos.getX(), ZERO), new DimPoint(mousePos.getX(), screen.getY())), borderWidth, borderColour, false);
    }

    List<String> lines = new ArrayList<>();
    lines.add("Mouse: " + (mousePos == null ? "n/a" : mousePos.toString()));
    lines.add("Content: " + getContentBox(element).toString());
    lines.add("Padding: " + element.getPadding().toString());
    lines.add("Border: " + element.getBorder().toString());
    lines.add("Margin: " + element.getMargin().toString());
    lines.add("Hor Algn: " + element.getHorizontalAlignment());
    lines.add("Vert Algn: " + element.getVerticalAlignment());
    lines.add("Focus: " + (element.getFocusable() ? context.focusedElement == element ? "Yes" : "No" : "n/a"));
    lines.add("");

    List<IElement> siblings = parent == null ? Collections.list() : Collections.without(parent.getChildren(), element);
    int nChildren = Collections.size(element.getChildren());
    lines.add(String.format("%s (with %d %s and %d %s)", element.getClass().getSimpleName(), nChildren, nChildren == 1 ? "child" : "children", siblings.size(), siblings.size() == 1 ? "sibling" : "siblings"));

    while (parent != null) {
      List<IElement> children = parent.getChildren();
      if (children == null) {
        children = new ArrayList<>();
      }

      lines.add(String.format("%s (with %d %s)", parent.getClass().getSimpleName(), children.size(), children.size() == 1 ? "child" : "children"));
      parent = parent.getParent();
    }

    FontRenderer font = context.fontRenderer;
    int y = 0;
    int left = (int)context.dimFactory.getMinecraftSize().getX().getGui();
    for (String line : lines) {
      int x = left - font.getStringWidth(line);
      font.drawStringWithShadow(line, x, y, Colour.WHITE.toSafeInt());
      y += font.FONT_HEIGHT;
    }
  }
}
