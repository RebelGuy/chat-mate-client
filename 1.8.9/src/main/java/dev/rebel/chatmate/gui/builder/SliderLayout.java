package dev.rebel.chatmate.gui.builder;

import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction;
import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction.SliderActionType;
import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction.SliderActionValueChangedData;
import net.minecraftforge.fml.client.config.GuiSlider;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderLayout extends ContentLayout<GuiSlider, SliderAction> {
  private final int id;
  private final int minValue;
  private final int maxValue;
  private final String prefix;
  private final String suffix;
  private final Supplier<Integer> onGetValue;
  private final Consumer<SliderActionValueChangedData> onChange;

  private GuiSlider slider;

  /** Create a new button within a layout component. */
  public SliderLayout(String[] width, String prefix, String suffix, int minValue, int maxValue, Consumer<SliderActionValueChangedData> onChange, Supplier<Integer> onGetValue) {
    super(width);
    this.id = (int)Math.round(Math.random() * Integer.MAX_VALUE);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.prefix = prefix;
    this.suffix = suffix;
    this.onGetValue = onGetValue;
    this.onChange = onChange;

    this.slider = null;
  }

  public GuiSlider instantiateGui(int x, int y, int width, int height) {
    if (this.slider == null) {
      this.slider = new GuiSlider(this.id, x, y, width, height, this.prefix, this.suffix, this.minValue, this.maxValue, this.onGetValue.get(), false, true);
    } else {
      this.slider.xPosition = x;
      this.slider.yPosition = y;
      this.slider.width = width;
      this.slider.height = height;
      this.slider.setValue(this.onGetValue.get());
    }
    return this.slider;
  }

  public GuiSlider tryGetGui() {
    return this.slider;
  }

  public void refreshContents() {
    if (this.slider != null) {
      this.slider.setValue(this.onGetValue.get());
    }
  }

  /** Returns true if the button action was handled */
  public boolean dispatchAction(SliderAction actionData) {
    if (this.slider != null && this.onChange != null && actionData.type == SliderActionType.MOUSE_EVENT) {
      this.onChange.accept(new SliderActionValueChangedData(this.slider.getValueInt()));
      return true;
    } else {
      return false;
    }
  }

  public static class SliderAction {
    public final SliderActionType type;
    public final SliderActionData data;

    public SliderAction(SliderActionType type, SliderActionData data) {
      this.type = type;
      this.data = data;
    }

    public enum SliderActionType {
      MOUSE_EVENT,
      VALUE_CHANGED
    }

    public static abstract class SliderActionData {}

    public static class SliderActionMouseEventData extends SliderActionData {
      public final MouseEventState state;
      public final int x;
      public final int y;

      public SliderActionMouseEventData(MouseEventState state, int x, int y) {
        this.state = state;
        this.x = x;
        this.y = y;
      }

      public enum MouseEventState {
        DOWN,
        MOVE,
        UP
      }
    }

    public static class SliderActionValueChangedData extends SliderActionData {
      public final int newValue;

      public SliderActionValueChangedData(int newValue) {
        this.newValue = newValue;
      }
    }
  }
}
