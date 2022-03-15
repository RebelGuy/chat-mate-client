package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

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
        if (ElementBase.getCollisionBox(child).checkCollision(point)) {
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
}
