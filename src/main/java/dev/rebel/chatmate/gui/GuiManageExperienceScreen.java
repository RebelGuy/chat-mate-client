package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.builder.Constants.Color;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.models.api.experience.ModifyExperienceRequest;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.services.McChatService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.fml.client.config.GuiUtils;


import javax.annotation.Nullable;
import java.io.IOException;

public class GuiManageExperienceScreen extends GuiScreen implements GuiPageButtonList.GuiResponder {
  private final static int MODAL_WIDTH = 200;
  private final static int MODAL_HEIGHT = 200;
  private final static int MODAL_PADDING = 10;
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final PublicUser user;
  private final ExperienceEndpointProxy experienceEndpointProxy;
  private final McChatService mcChatService;
  private final FontEngineProxy fontEngineProxy;

  private GuiButton submitButton;
  private GuiButton cancelButton;
  private GuiTextField deltaField;
  private GuiTextField messageField;

  public GuiManageExperienceScreen(Minecraft minecraft, DimFactory dimFactory, PublicUser user, ExperienceEndpointProxy experienceEndpointProxy, McChatService mcChatService, FontEngineProxy fontEngineProxy) {
    super();
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;
    this.user = user;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.mcChatService = mcChatService;
    this.fontEngineProxy = fontEngineProxy;
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

    DimPoint display = this.dimFactory.getMinecraftSize();
    int displayWidth = (int)display.getX().getGui();
    int displayHeight = (int)display.getY().getGui();

    // todo: set modal height based on contents so it's centred vertically!
    int x = (displayWidth - MODAL_WIDTH) / 2;
    int y = (displayHeight - MODAL_HEIGHT) / 2;

    int usableWidth = MODAL_WIDTH - MODAL_PADDING * 2;
    int effectiveLeft = x + MODAL_PADDING;

    // header
    FontEngineProxy font = this.fontEngineProxy;
    int headerId = 150;
    y = y + 10;
    GuiLabel header = new GuiLabel(font, headerId, effectiveLeft, y, usableWidth, font.FONT_HEIGHT, Color.WHITE);
    header.func_175202_a("Manage Experience for " + user.userInfo.channelName); // todo: this will overflow for long strings - make it wrap!
    header.setCentered();
    this.labelList.add(header);

    // body
    int componentHeight = font.FONT_HEIGHT / 2 + font.FONT_HEIGHT;
    int labelWidth = (int)(0.3f * usableWidth);
    int textFieldWidth = (int)(0.7f * usableWidth);
    int fieldX = effectiveLeft + usableWidth - textFieldWidth;

    y += 30;
    GuiLabel deltaLabel = new GuiLabel(font, 151, effectiveLeft, y, labelWidth, componentHeight, Color.WHITE);
    deltaLabel.func_175202_a("Add level:");
    this.deltaField = new GuiTextField(151, font, fieldX, y, textFieldWidth, componentHeight);
    deltaField.func_175207_a(this);

    y += 20;
    GuiLabel messageLabel = new GuiLabel(font, 152, effectiveLeft, y, labelWidth, componentHeight, Color.WHITE);
    messageLabel.func_175202_a("Message:");
    this.messageField = new GuiTextField(153, font, fieldX, y, textFieldWidth, componentHeight);
    messageField.setMaxStringLength(1023);

    // footer - use vanilla buttons?
    int buttonHeight = font.FONT_HEIGHT * 2; // needs to be large enough to render the button outline
    int buttonWidth = 50;
    y += 30;
    this.submitButton = new GuiButton(155, effectiveLeft, y, buttonWidth, buttonHeight, "Submit");
    this.submitButton.enabled = false;
    this.cancelButton = new GuiButton(155, effectiveLeft + usableWidth - buttonWidth, y, buttonWidth, buttonHeight, "Cancel");

    this.buttonList.add(this.submitButton);
    this.buttonList.add(this.cancelButton);
    this.labelList.add(header);
    this.labelList.add(deltaLabel);
    this.labelList.add(messageLabel);
  }

  @Override
  public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
    DimPoint display = this.dimFactory.getMinecraftSize();
    int displayWidth = (int)display.getX().getGui();
    int displayHeight = (int)display.getY().getGui();

    int actualHeight = 120;
    int x = (displayWidth - MODAL_WIDTH) / 2;
    int y = (displayHeight - MODAL_HEIGHT) / 2;

    this.drawGradientRect(x, y, displayWidth - x, y + actualHeight, -1072689136, -804253680);

    // by modifying these values, we make the border completely flush with the background rect
    int zLevel = 300;
    int tooltipX = x + 2;
    int tooltipY = y + 2;
    int tooltipWidth = MODAL_WIDTH - 4;
    int tooltipHeight = actualHeight - 4;

//    final int borderColorStart = 0x505000FF;
//    final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
    final int borderColorStart = new Colour(org.lwjgl.util.Color.BLACK).toSafeInt();
    final int borderColorEnd = new Colour(org.lwjgl.util.Color.BLACK).toSafeInt();
    GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
    GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
    GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
    GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

    super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);

    this.messageField.drawTextBox();
    this.deltaField.drawTextBox();

    // todo: draw background. probably dirt background, or very dark, near-opaque background.
    // rounded borders?
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button == this.cancelButton) {
      this.minecraft.displayGuiScreen(null);
    } else if (button == this.submitButton) {
      this.experienceEndpointProxy.modifyExperienceAsync(
          new ModifyExperienceRequest(this.user.id, this.tryParseDelta(this.deltaField.getText()), this.messageField.getText()),
          data -> this.mcChatService.printInfo("Successfully modified experience for " + data.updatedUser.userInfo.channelName + "."),
          this.mcChatService::printError
      );
      this.minecraft.displayGuiScreen(null);
    }
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (this.deltaField.isFocused()) {
      this.deltaField.textboxKeyTyped(typedChar, keyCode);
    } else if (this.messageField.isFocused()) {
      this.messageField.textboxKeyTyped(typedChar, keyCode);
    } else {
      super.keyTyped(typedChar, keyCode);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    this.deltaField.mouseClicked(mouseX, mouseY, mouseButton);
    this.messageField.mouseClicked(mouseX, mouseY, mouseButton);
    super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {

  }

  @Override
  public void onTick(int id, float value) {

  }

  @Override
  public void func_175319_a(int p_175319_1_, String p_175319_2_) {
    // called when the text has changed
    int id = p_175319_1_;
    String newText = p_175319_2_;

    if (id != 151) {
      return;
    }

    boolean valid = tryParseDelta(newText) != null;
    this.submitButton.enabled = valid;
  }

  private @Nullable Float tryParseDelta(String str) {
    if (str == null || str.isEmpty()) {
      return null;
    }

    try {
      float parsed = Float.parseFloat(str);
      return Math.abs(parsed) < 100 ? parsed : null;
    } catch (Exception ignored) {
      return null;
    }
  }
}
