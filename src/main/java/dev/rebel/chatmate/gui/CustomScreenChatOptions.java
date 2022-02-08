package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.builder.SliderLayout;
import dev.rebel.chatmate.gui.builder.TableLayout;
import dev.rebel.chatmate.models.Config;
import net.minecraft.client.gui.*;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;

public class CustomScreenChatOptions extends ScreenChatOptions {
  private TableLayout tableLayout;
  private final Config config;

  public CustomScreenChatOptions(GuiScreen parentScreen, GameSettings gameSettings, Config config) {
    super(parentScreen, gameSettings);
    this.config = config;
  }

  public void initGui() {
    super.initGui();

    // yet another yucky override, but not much we can do when it comes to modifying the existing screens.
    GuiButton firstButton = this.buttonList.stream().filter(b -> b.id == 15).findFirst().get();
    GuiButton secondButton = this.buttonList.stream().filter(b -> b.id == 16).findFirst().get();
    GuiButton doneButton = this.buttonList.stream().filter(b -> b.id == 200).findFirst().get();

    int buttonW = firstButton.width;
    int buttonH = firstButton.height;
    int left = firstButton.xPosition;
    int top = doneButton.yPosition;
    int right = secondButton.xPosition + buttonW;
    int width = right - left;

    SliderLayout chatOffsetSlider = new SliderLayout(new String[]{ buttonW + "px" }, "Chat height: ", "px", 0, 100, this::onChangeChatOffset, config.getChatVerticalDisplacementEmitter()::get);
    this.tableLayout = new TableLayout(this.buttonList, this.labelList, left, top, width, 1, buttonH, 1, 1)
        .withRow(chatOffsetSlider)
        .instantiate();

    doneButton.yPosition += buttonH + 4; // 4 is padding
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (this.tableLayout == null || !this.tableLayout.onActionPerformed(button)) {
      super.actionPerformed(button);
    }
  }

  @Override
  protected void mouseClicked(int x, int y, int mouseEvent) throws IOException {
    super.mouseClicked(x, y, mouseEvent);
    if (this.tableLayout != null) {
      this.tableLayout.onPostMousePressed(x, y);
    }
  }

  @Override
  protected void mouseClickMove(int x, int y, int mouseButton, long timeSinceLastClick) {
    super.mouseClickMove(x, y, mouseButton, timeSinceLastClick);
    if (this.tableLayout != null) {
      this.tableLayout.onPostMouseDragged(x, y);
    }
  }

  @Override
  protected void mouseReleased(int x, int y, int mouseEvent) {
    super.mouseReleased(x, y, mouseEvent);
    if (this.tableLayout != null) {
      this.tableLayout.onPostMouseReleased(x, y);
    }
  }

  private void onChangeChatOffset(SliderLayout.SliderAction.SliderActionValueChangedData sliderActionChangeData) {
    this.config.getChatVerticalDisplacementEmitter().set(sliderActionChangeData.newValue);
  }
}
