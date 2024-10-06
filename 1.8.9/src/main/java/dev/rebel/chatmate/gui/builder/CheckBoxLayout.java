package dev.rebel.chatmate.gui.builder;

import dev.rebel.chatmate.gui.builder.CheckBoxLayout.CheckBoxAction;
import dev.rebel.chatmate.gui.builder.CheckBoxLayout.CheckBoxAction.CheckBoxActionCheckedData;
import dev.rebel.chatmate.gui.builder.CheckBoxLayout.CheckBoxAction.CheckBoxActionType;
import dev.rebel.chatmate.gui.builder.Constants.Layout;
import net.minecraft.client.gui.GuiButton;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CheckBoxLayout extends ContentLayout<GuiButton, CheckBoxAction> {
  private final int id;
  private final Consumer<CheckBoxActionCheckedData> onCheckedChanged;
  private final Supplier<Boolean> getChecked;

  private GuiButton button;

  /** Create a new checkbox within a layout component.
   * Note that this component does NOT handle its own checked state - if the checkbox is toggled, the UI will only change if the supplied value also changes.
   */
  public CheckBoxLayout(Consumer<CheckBoxActionCheckedData> onCheckedChanged, Supplier<Boolean> getChecked) {
    super(new String[]{ Layout.HEIGHT + "px" });
    this.id = (int)Math.round(Math.random() * Integer.MAX_VALUE);
    this.onCheckedChanged = onCheckedChanged;
    this.getChecked = getChecked;

    this.button = null;
  }

  public GuiButton instantiateGui(int x, int y, int width, int height) {
    this.button = new GuiButton(this.id, x, y, width, height, this.getButtonText());
    return this.button;
  }

  public GuiButton tryGetGui() {
    return this.button;
  }

  public void refreshContents() {
    if (this.button != null) {
      this.button.displayString = this.getButtonText();
    }
  }

  private String getButtonText() {
    return this.getChecked.get() ? "x" : " ";
  }

  /** Returns true if the button action was handled */
  public boolean dispatchAction(CheckBoxAction actionData) {
    if (this.onCheckedChanged != null && actionData.type == CheckBoxActionType.CLICK) {
      this.onCheckedChanged.accept(new CheckBoxActionCheckedData(!this.getChecked.get()));
      return true;
    } else {
      return false;
    }
  }

  public static class CheckBoxAction {
    public final CheckBoxActionType type;
    public final CheckBoxActionData data;

    public CheckBoxAction(CheckBoxActionType type, CheckBoxActionData data) {
      this.type = type;
      this.data = data;
    }

    public enum CheckBoxActionType {
      CLICK,
      CHECKED_CHANGED
    }

    public static abstract class CheckBoxActionData {}

    public static class CheckBoxActionClickData extends CheckBoxActionData {}

    public static class CheckBoxActionCheckedData extends CheckBoxActionData {
      public final boolean checked;

      public CheckBoxActionCheckedData(boolean checked) {
        this.checked = checked;
      }
    }
  }
}
