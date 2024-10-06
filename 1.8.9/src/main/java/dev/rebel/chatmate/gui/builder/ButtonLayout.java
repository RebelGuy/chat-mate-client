package dev.rebel.chatmate.gui.builder;

import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction;
import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction.ButtonActionClickData;
import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction.ButtonActionType;
import net.minecraft.client.gui.GuiButton;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonLayout extends ContentLayout<GuiButton, ButtonAction> {
  private final int id;
  private final Consumer<ButtonActionClickData> onClick;
  private final Supplier<String> onRenderText;

  private GuiButton button;

  /** Create a new button within a layout component. */
  public ButtonLayout(String[] width, Supplier<String> onRenderText, Consumer<ButtonActionClickData> onClick) {
    super(width);
    this.id = (int)Math.round(Math.random() * Integer.MAX_VALUE);
    this.onClick = onClick;
    this.onRenderText = onRenderText;

    this.button = null;
  }

  /** Wrap an existing button in a layout component - will hold on to this button reference. */
  public ButtonLayout(GuiButton existingButton, String[] width, Supplier<String> onRenderText, Consumer<ButtonActionClickData> onClick) {
    this(width, onRenderText, onClick);
    this.button = existingButton;
  }

  public GuiButton instantiateGui(int x, int y, int width, int height) {
    if (this.button == null) {
      this.button = new GuiButton(this.id, x, y, width, height, this.onRenderText.get());
    } else {
      this.button.xPosition = x;
      this.button.yPosition = y;
      this.button.width = width;
      this.button.height = height;
    }
    return this.button;
  }

  public GuiButton tryGetGui() {
    return this.button;
  }

  public void refreshContents() {
    if (this.onRenderText != null && this.button != null) {
      this.button.displayString = this.onRenderText.get();
    }
  }

  /** Returns true if the button action was handled */
  public boolean dispatchAction(ButtonAction actionData) {
    if (this.onClick != null && actionData.type == ButtonActionType.CLICK) {
      this.onClick.accept((ButtonActionClickData)actionData.data);
      return true;
    } else {
      return false;
    }
  }

  public static class ButtonAction {
    public final ButtonActionType type;
    public final ButtonActionData data;

    public ButtonAction(ButtonActionType type, ButtonActionData data) {
      this.type = type;
      this.data = data;
    }

    public enum ButtonActionType {
      CLICK
    }

    public static abstract class ButtonActionData {}

    public static class ButtonActionClickData extends ButtonActionData { /* no data for now */ }
  }
}
