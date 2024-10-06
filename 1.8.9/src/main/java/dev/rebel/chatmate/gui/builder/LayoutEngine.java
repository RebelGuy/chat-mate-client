package dev.rebel.chatmate.gui.builder;

import dev.rebel.chatmate.util.Collections;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// 1D
public class LayoutEngine {
  public static List<Layout> calculateLayouts(int parentSize, int padChildren, String[] ...childLayouts) {
    ArrayList<ChildLayout> children = new ArrayList<>();
    for (String[] layout : childLayouts) {
      if (layout.length == 1) {
        children.add(new ChildLayout(parentSize, layout[0]));
      } else if (layout.length == 2) {
        children.add(new ChildLayout(parentSize, layout[0], layout[1]));
      } else {
        throw new RuntimeException("Child layouts must be defined using an array of 1 (size) or 2 (minSize and maxSize) strings");
      }
    }

    int availableSpace = parentSize - padChildren * (children.size() - 1);
    int minUsed = Collections.sum(children, child -> child.minSize);
    int maxUsed = Collections.sum(children, child -> child.maxSize);

    if (minUsed > availableSpace) {
      // reduce the padding between each child (negative padding). this means there might be some clipping, but that's to be expected.
      return fitFixedChildren(Collections.map(children, c -> c.minSize), padChildren, availableSpace, minUsed);

    } else if (maxUsed < availableSpace) {
      // pad out each child
      return fitFixedChildren(Collections.map(children, c -> c.maxSize), padChildren, availableSpace, maxUsed);

    } else {
      // denote the proportion between the min and max of each child as the frac. Move the frac between 0 and 1 until we find
      // the point at which the available space is filled up. There will necessarily be such a point, and children will always
      // remain sized within their bounds.
      // in fact, this is a simple linear equation that can be solved for the frac.
      float frac = (float)(availableSpace - minUsed) / (maxUsed - minUsed);
      int totalSize = Collections.sum(children, c -> (int)Math.floor(c.minSize + frac * (c.maxSize - c.minSize)));
      int extraPixelSizeForFirst = availableSpace - totalSize;
      return constructLayouts(
          Collections.map(children, (c, i) -> (int)Math.floor(c.minSize + frac * (c.maxSize - c.minSize)) + (i < extraPixelSizeForFirst ? 1 : 0)),
          i -> padChildren
      );
    }
  }

  private static List<Layout> constructLayouts(List<Integer> sizes, Function<Integer, Integer> getPaddingAfterIndex) {
    List<Layout> layouts = new ArrayList<>();
    layouts.add(new Layout(0, sizes.get(0)));

    for (int i = 1; i < sizes.size(); i++) {
      Layout prev = layouts.get(i - 1);
      int position = prev.position + prev.size + getPaddingAfterIndex.apply(i - 1);
      layouts.add(new Layout(position, sizes.get(i)));
    }

    return layouts;
  }

  private static List<Layout> fitFixedChildren(List<Integer> sizes, int basePadding, int availableSize, int currentSize) {
    int additionalPadding = sizes.size() == 1 ? 0 : (availableSize - currentSize) / (sizes.size() - 1);
    int extraPixelPaddingForFirst = availableSize - currentSize - additionalPadding * (sizes.size() - 1); // takes care of rounding
    int paddingDelta = extraPixelPaddingForFirst > 0 ? 1 : -1;

    // variable used in loop must be final......
    int extra = Math.abs(extraPixelPaddingForFirst);
    return constructLayouts(
        sizes,
        i -> (i < extra ? additionalPadding + paddingDelta : additionalPadding) + basePadding
    );
  }

  public static class ChildLayout {
    public final int minSize;
    public final int maxSize;

    public ChildLayout(int parentSize, String size) {
      this(parentSize, size, size);
    }

    public ChildLayout(int parentSize, String minSize, String maxSize) {
      this.minSize = calculateSize(parentSize, minSize);
      this.maxSize = calculateSize(parentSize, maxSize);
    }

    private static int calculateSize(int parentSize, String size) {
      if (size.endsWith("px")) {
        return Integer.parseInt(size.replace("px", ""));
      } else if (size.endsWith("%")) {
        return (int)(parentSize * Float.parseFloat(size.replace("%", "")));
      } else {
        throw new RuntimeException("Invalid format for size - must end with `px` or `%`.");
      }
    }
  }

  public static class Layout {
    public final int position;
    public final int size;

    private Layout(int position, int size) {
      this.position = position;
      this.size = size;
    }
  }
}
