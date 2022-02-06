package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.builder.ButtonLayout;
import dev.rebel.chatmate.gui.builder.ButtonLayout.ButtonAction.ButtonActionClickData;
import dev.rebel.chatmate.gui.builder.Constants.Layout;
import dev.rebel.chatmate.gui.builder.TableLayout;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.GuiService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;

import java.io.IOException;

public class CustomGuiPause extends GuiIngameMenu {
  private final GuiService guiService;
  private final Config config;
  private TableLayout tableLayout;

  public CustomGuiPause(GuiService guiService, Config config)
  {
    super();
    this.guiService = guiService;
    this.config = config;
    this.config.listenAny(this::onConfigUpdate);

    this.tableLayout = null;
  }

  @Override
  public void initGui()
  {
    super.initGui();

    GuiButton optionsButton = this.buttonList.stream().filter(b -> b.id == 0).findFirst().get();
    GuiButton modOptionsButton = this.buttonList.stream().filter(b -> b.id == 12).findFirst().get();

    int left = optionsButton.xPosition;
    int top = optionsButton.yPosition;
    int right = modOptionsButton.xPosition + modOptionsButton.width;
    int width = right - left;

    ButtonLayout optionsLayout = new ButtonLayout(optionsButton, new String[]{"10px", "50%"}, null, null);
    ButtonLayout modOptionsLayout = new ButtonLayout(modOptionsButton, new String[]{"10px", "50%"}, null, null);
    ButtonLayout ytLayout = new ButtonLayout(new String[] {"20px"}, this::onRenderYtButtonText, this::onClickYtButton);
    ButtonLayout dashboardLayout = new ButtonLayout(new String[] {"20px"}, () -> "⚙", this::onClickDashboardButton);

    this.tableLayout = new TableLayout(this.buttonList, this.labelList, left, top, width, 4, Layout.HEIGHT, 0, Layout.HORIZONTAL_PADDING)
        .withRow(optionsLayout, modOptionsLayout, ytLayout, dashboardLayout)
        .instantiate();
    }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException
  {
    if (this.tableLayout == null || !this.tableLayout.onActionPerformed(button)) {
      super.actionPerformed(button);
    }
  }

  private void onConfigUpdate() {
    if (this.tableLayout != null) {
      this.tableLayout.refreshContents();
    }
  }

  private void onClickYtButton(ButtonLayout.ButtonAction.ButtonActionClickData data) {
    this.config.getChatMateEnabled().set(!this.config.getChatMateEnabled().get());
  }

  private String onRenderYtButtonText() {
    return (this.config.getChatMateEnabled().get() ? "§2" : "§4") + "YT";
  }

  private void onClickDashboardButton(ButtonActionClickData buttonActionClickData) {
    this.guiService.onDisplayDashboard();
  }
}
