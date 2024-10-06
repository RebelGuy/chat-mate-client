package dev.rebel.chatmate.gui.builder;

import dev.rebel.chatmate.gui.builder.LayoutEngine.Layout;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LayoutEngineTests {
//  @Test
//  calculateLayouts_throws_ifTotalPaddingTooLarge() throws Exception {
//    // each child must be at least 1 px, and so the total expected
//  }

  @Test
  public void calculateLayouts_fixedItemsSpacedOut_ifTooSmall() throws Exception {
    List<Layout> layouts = LayoutEngine.calculateLayouts(100, 10, new String[]{"10px"}, new String[]{"20px"}, new String[]{"25px"});

    Assert.assertEquals(layouts.size(), 3);
    assertLayout(layouts, 0, 0, 10);
    assertLayout(layouts, 1, 23, 20); // the single px padding delta is applied first
    assertLayout(layouts, 2, 22, 25);
  }

  @Test
  public void calculateLayouts_fixedItemsPushedIn_ifTooLarge() throws Exception {
    List<Layout> layouts = LayoutEngine.calculateLayouts(100, 100, new String[]{"10px"}, new String[]{"20px"}, new String[]{"25px"});

    Assert.assertEquals(layouts.size(), 3);
    assertLayout(layouts, 0, 0, 10);
    assertLayout(layouts, 1, 22, 20); // the single px padding delta is applied first
    assertLayout(layouts, 2, 23, 25);
  }

  @Test
  public void calculateLayouts_dynamicAndFixed_dynamicResizesToFillParent() throws Exception {
    List<Layout> layouts = LayoutEngine.calculateLayouts(100, 10, new String[]{"10px", "100%"}, new String[]{"10px", "100%"}, new String[]{"20px"});

    Assert.assertEquals(layouts.size(), 3);
    assertLayout(layouts, 0, 0, 30);
    assertLayout(layouts, 1, 10, 30);
    assertLayout(layouts, 2, 10, 20);
  }

  // position starting at the endpoint of the previous layout.
  private void assertLayout(List<Layout> layouts, int index, int expectedRelPos, int expectedSize) {
    int start = index == 0 ? 0 : layouts.get(index - 1).position + layouts.get(index - 1).size;
    int expectedPos = start + expectedRelPos;

    Assert.assertEquals(expectedPos, layouts.get(index).position);
    Assert.assertEquals(expectedSize, layouts.get(index).size);
  }
}
