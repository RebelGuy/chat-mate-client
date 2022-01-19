package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.gui.builder.CheckBoxLayout;
import dev.rebel.chatmate.gui.builder.CheckBoxLayout.CheckBoxAction.CheckBoxActionCheckedData;
import dev.rebel.chatmate.gui.builder.Constants.Color;
import dev.rebel.chatmate.gui.builder.Constants.Layout;
import dev.rebel.chatmate.gui.builder.LabelLayout;
import dev.rebel.chatmate.gui.builder.SliderLayout;
import dev.rebel.chatmate.gui.builder.SliderLayout.SliderAction.SliderActionValueChangedData;
import dev.rebel.chatmate.gui.builder.TableLayout;
import dev.rebel.chatmate.models.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.io.IOException;
import java.util.ArrayList;

public class CustomGuiConfig extends GuiConfig {
  private final Config config;
  private TableLayout tableLayout;

  // fantastic tutorial here: http://jabelarminecraft.blogspot.com/p/minecraft-modding-configuration-guis.html
  // (use the above when we have config files one day)
  // Note: do NOT change this constructor, as the GuiFactory that instantiates this class is registered with the mod.
  // Todo: In the future, we could add a button-click handler in the CustomGuiModList that does this properly
  public CustomGuiConfig(GuiScreen parent)
  {
    super(parent, new ArrayList<>(), "chatmate", false, false, "Configure ChatMate");
    titleLine2 = "Mod Settings";
    this.config = ChatMate.instance_hack.config;
    this.config.listenAny(this::onConfigUpdate);
  }

  // great tutorial: https://medium.com/@andreshj87/drawing-a-gui-screen-on-minecraft-forge-7e0059015596
  @Override
  public void initGui()
  {
    super.initGui();

    int top = 40;
    int width = Math.min(300, this.width - 50); // shrink with screen, but only expand so much
    int left = this.width / 2 - width / 2; // centre table horizontally

    LabelLayout apiLabel = new LabelLayout(this.fontRendererObj, new String[]{ "0%", "100%" }, () -> "Enable API", Color.WHITE);
    CheckBoxLayout apiCheckbox = new CheckBoxLayout(this::onToggleApi, config.apiEnabled::get);

    LabelLayout soundLabel = new LabelLayout(this.fontRendererObj, new String[]{ "0%", "100%" }, () -> "Enable Sound", Color.WHITE);
    CheckBoxLayout soundCheckbox = new CheckBoxLayout(this::onToggleSound, config.soundEnabled::get);

    LabelLayout chatOffsetLabel = new LabelLayout(this.fontRendererObj, new String[] { "0%", "100%" }, () -> "Chat Height Offset", Color.WHITE);
    SliderLayout chatOffsetSlider = new SliderLayout(new String[]{ "100px" , "50%" }, "", "px", 0, 100, this::onChangeChatOffset, config.chatVerticalDisplacement::get);

    this.tableLayout = new TableLayout(this.buttonList, this.labelList, left, top, width, 2, Layout.HEIGHT, Layout.VERTICAL_PADDING, Layout.HORIZONTAL_PADDING)
        .withRow(apiLabel, apiCheckbox)
        .withRow(soundLabel, soundCheckbox)
        .withRow(chatOffsetLabel, chatOffsetSlider)
        .instantiate();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks)
  {
    // You can do things like create animations, draw additional elements, etc. here
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void actionPerformed(GuiButton button)
  {
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

  private void onConfigUpdate() {
    if (this.tableLayout != null) {
      this.tableLayout.refreshContents();
    }
  }

  private void onToggleApi(CheckBoxActionCheckedData checkBoxActionCheckedData) {
    this.config.apiEnabled.set(checkBoxActionCheckedData.checked);
  }

  private void onToggleSound(CheckBoxActionCheckedData checkBoxActionCheckedData) {
    this.config.soundEnabled.set(checkBoxActionCheckedData.checked);
  }

  private void onChangeChatOffset(SliderActionValueChangedData sliderActionChangeData) {
    this.config.chatVerticalDisplacement.set(sliderActionChangeData.newValue);
  }
}
