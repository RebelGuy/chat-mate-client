package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.models.Line;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.rebel.chatmate.gui.Interactive.ElementBase.*;
import static dev.rebel.chatmate.gui.Interactive.ElementBase.getBorderBox;

public class ElementHelpers {

  /** Traverses the tree from the parent, keeping track of the path, and picking the first child whose bounds intersect the given point.
   * Returns if a child has no children of its own, or if none of the children intersect the point. */
  public static List<IElement> getElementsAtPointStrict(IElement parent, DimPoint point) {
    List<IElement> result = Collections.list(parent);

    while (true) {
      List<IElement> children = parent.getChildren();
      if (children == null || children.size() == 0) {
        return result;
      }

      // reversing the order of the children ensures that, if two children are drawn at the same position, the one that
      // was most likely added last is picked. in the future, we could use z indexes for a more robust solution.
      boolean foundMatch = false;
      for (IElement child : orderDescendingByZIndex(Collections.reverse(children))) {
        if (isCompletelyVisible(child) && getCollisionBox(child).checkCollision(point)) {
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

  /** Returns the list of elements present on the highest z-layer, in inverse order in which they appear in the list of children of their parent. */
  public static List<IElement> raycast(IElement parent, DimPoint point) {
    List<IElement> elements = getElementsAtPoint(parent, point);
    if (elements.size() == 0) {
      return elements;
    }

    int zIndex = Collections.first(elements).getEffectiveZIndex();
    return Collections.filter(elements, el -> el.getEffectiveZIndex() == zIndex);
  }

  /** Returns all completely visible elements at the given point, ordered from the largest zIndex to the lowest, then in reverse by their position within the list of children of their parent. */
  public static List<IElement> getElementsAtPoint(IElement parent, DimPoint point) {
    List<IElement> children = getAllChildren(parent);
    List<IElement> childrenAtPoint = Collections.filter(children, el -> isCompletelyVisible(el) && el.getBox() != null && getCollisionBox(el).checkCollision(point));
    return orderDescendingByZIndex(Collections.reverse(childrenAtPoint));
  }

  /** Returns all completely visible elements at the given point, ordered from the largest zIndex to the lowest, then in the original position within the list of children of their parent. */
  public static List<IElement> getElementsAtPointInverted(IElement parent, DimPoint point) {
    List<IElement> children = getAllChildren(parent);
    List<IElement> childrenAtPoint = Collections.filter(children, el -> isCompletelyVisible(el) && el.getBox() != null && getCollisionBox(el).checkCollision(point));
    return orderDescendingByZIndex(childrenAtPoint);
  }

  /** Returns true if an element and all its ancestors are visible. It is entirely feasible for an element to be visible, but for its parent to not be visible, so this methods distinguishes between "soft" and "total" visibility. */
  public static boolean isCompletelyVisible(IElement element) {
    while (!(element instanceof InteractiveScreen)) {
      if (!element.getVisible()) {
        return false;
      }
      element = element.getParent();
    }
    return true;
  }

  public static List<IElement> getAllChildren(IElement parent) {
    List<IElement> result = Collections.list(parent);

    List<IElement> children = parent.getChildren();
    if (Collections.any(children)) {
      children.forEach(el -> result.addAll(getAllChildren(el)));
    }

    return result;
  }

  /** For each layer, from highest to lowest, returns the element in the same order as they appear in the list of children of their parents. */
  public static List<IElement> orderDescendingByZIndex(List<IElement> orderedElements) {
    // reverse ordered elements since we will be reversing the final list again
    Map<Integer, List<IElement>> groups = Collections.groupBy(Collections.reverse(orderedElements), IElement::getEffectiveZIndex);
    List<IElement> orderedAscending = Collections.collapseGroups(groups, g -> g);
    return Collections.reverse(orderedAscending);
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

  /** Lays out the size within the given box, using the provided alignment options.
   * Note: If the size is larger than the provided box, it will NOT be contained entirely. */
  public static DimRect alignElementInBox(DimPoint size, DimRect box, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
    Dim x;
    switch (horizontalAlignment) {
      case LEFT:
        x = box.getX();
        break;
      case CENTRE:
        x = box.getX().plus(box.getWidth().minus(size.getX()).over(2));
        break;
      case RIGHT:
        x = box.getRight().minus(size.getX());
        break;
      default:
        throw new RuntimeException("Invalid HorizontalAlignment " + horizontalAlignment);
    }

    Dim y;
    switch (verticalAlignment) {
      case TOP:
        y = box.getY();
        break;
      case MIDDLE:
        y = box.getY().plus(box.getHeight().minus(size.getY()).over(2));
        break;
      case BOTTOM:
        y = box.getBottom().minus(size.getY());
        break;
      default:
        throw new RuntimeException("Invalid VerticalAlignment " + verticalAlignment);
    }

    return new DimRect(new DimPoint(x, y), size);
  }

  /** Returns elements that are of, or inherit from, the given type. */
  public static <E extends IElement> List<E> getElementsOfType(IElement parent, Class<E> elementClass) {
    return (List<E>)flattenTree(parent).stream().filter(el -> elementClass.isAssignableFrom(el.getClass())).collect(Collectors.toList());
  }

  public static List<IElement> flattenTree(IElement parent) {
    List<IElement> result = Collections.list(parent);
    List<IElement> children = parent.getChildren();
    if (Collections.any(children)) {
      for (IElement child : children) {
        result.addAll(flattenTree(child));
      }
    }
    return result;
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
      RendererHelpers.drawLine(1000, new Line(new DimPoint(ZERO, mousePos.getY()), new DimPoint(screen.getX(), mousePos.getY())), borderWidth, borderColour, borderColour, false);
      RendererHelpers.drawLine(1000, new Line(new DimPoint(mousePos.getX(), ZERO), new DimPoint(mousePos.getX(), screen.getY())), borderWidth, borderColour, borderColour, false);
    }

    List<String> lines = new ArrayList<>();
    lines.add("Mouse: " + (mousePos == null ? "n/a" : mousePos.toString()));
    lines.add("Content: " + getContentBox(element).toString());
    lines.add("Padding: " + element.getPadding().toString());
    lines.add("Border: " + element.getBorder().toString());
    lines.add("Margin: " + element.getMargin().toString());
    lines.add("Hor Algn: " + element.getHorizontalAlignment());
    lines.add("Vert Algn: " + element.getVerticalAlignment());
    lines.add("Sizing: " + element.getSizingMode());
    lines.add("Layout group: " + element.getLayoutGroup());
    lines.add(String.format("Z-index: %d (%d)", element.getZIndex(), element.getEffectiveZIndex()));
    lines.add("");

    List<IElement> siblings = parent == null ? Collections.list() : Collections.filter(Collections.without(parent.getChildren(), element), IElement::getVisible);
    int nChildren = Collections.size(element.getChildren());
    lines.add(String.format("%s (with %d %s and %d %s)", element.getClass().getSimpleName(), nChildren, nChildren == 1 ? "child" : "children", siblings.size(), siblings.size() == 1 ? "sibling" : "siblings"));

    while (parent != null) {
      List<IElement> children = Collections.filter(parent.getChildren(), IElement::getVisible);
      if (children == null) {
        children = new ArrayList<>();
      }

      lines.add(String.format("%s (with %d %s)", parent.getClass().getSimpleName(), children.size(), children.size() == 1 ? "child" : "children"));
      parent = parent.getParent();
    }

    FontEngine fontEngine = context.fontEngine;
    Font font = new Font().withShadow(new Shadow(context.dimFactory));
    float scale = 0.5f;
    RendererHelpers.withMapping(new DimPoint(ZERO, ZERO), scale, () -> {
      float y = 0;
      float right = context.dimFactory.getMinecraftSize().getX().getGui();
      for (String line : lines) {
        float x = right / scale - (float)fontEngine.getStringWidth(line);
        fontEngine.drawString(line, x, y, font);
        y += fontEngine.FONT_HEIGHT;
      }
    });
  }
}
