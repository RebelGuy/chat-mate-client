package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.builder.*;
import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction.ButtonActionClickData;
import dev.rebel.chatmate.gui.builder.Constants.Color;
import dev.rebel.chatmate.gui.builder.Constants.Layout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

public class GuiManageExperienceScreen extends GuiScreen {
  private final static int MODAL_WIDTH = 200;
  private final static int MODAL_HEIGHT = 200;
  private final Minecraft minecraft;

  private TableLayout tableLayout;

  public GuiManageExperienceScreen(Minecraft minecraft) {
    super();
    this.minecraft = minecraft;
  }

  @Override
  public void setWorldAndResolution(Minecraft mc, int width, int height) {
    // for some reason only the buttonList is cleared, but not the labelList
    this.labelList.clear();
    super.setWorldAndResolution(mc, width, height);
  }

  @Override
  public void initGui() {
    super.initGui();

    int left = (this.minecraft.displayWidth - MODAL_WIDTH) / 2;
    int top = (this.minecraft.displayHeight - MODAL_HEIGHT) / 2;

    // header
    // title, something like "Manage Experience for <Channel Name>"

    // todo: can simplify this and just use vanilla components - this is fine because it's a simple modal.
    LabelLayout deltaLabel = new LabelLayout(this.fontRendererObj, new String[] { "30%" }, () -> "Add level:", Color.WHITE);
    TextFieldLayout deltaTextField = new TextFieldLayout();

    LabelLayout messageLabel = new LabelLayout(this.fontRendererObj, new String[] { "30%" }, () -> "Message:", Color.WHITE);
    TextFieldLayout messageTextField = new TextFieldLayout();

    this.tableLayout = new TableLayout(this.buttonList, this.labelList, left, top, MODAL_WIDTH, 2, Layout.HEIGHT, Layout.VERTICAL_PADDING, Layout.HORIZONTAL_PADDING)
        .withRow(deltaLabel, deltaTextField)
        .withRow(messageLabel, messageTextField)
        .instantiate();

    // footer - use vanilla buttons?
    ButtonLayout cancelButton = new ButtonLayout(this.fontRendererObj, new String[] { "30%" }, () -> "Cancel", this::onCancel);
    ButtonLayout submitButton = new ButtonLayout(this.fontRendererObj, new String[] { "30%" }, () -> "Submit", this::onSubmit);

  }

  private void onSubmit(ButtonActionClickData buttonActionClickData) {

  }

  private void onCancel(ButtonActionClickData buttonActionClickData) {

  }

  @Override
  public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
    super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);

    // todo: draw background. probably dirt background, or very dark, near-opaque background.
    // rounded borders?
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
}
