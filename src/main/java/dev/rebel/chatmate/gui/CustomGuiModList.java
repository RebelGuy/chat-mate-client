package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.GuiService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;

import java.io.IOException;
import java.util.Optional;

public class CustomGuiModList extends GuiModList {
  private final Minecraft minecraft;
  private final Config config;
  private final GuiService guiService;
  private final int modIndex;
  private GuiButton configButton;

  // Initially we implemented the GuiFactory and registered it with Forge to automatically show the config screen for
  // ChatMate, but that prevented us from doing any sort of dependency injection. Turns out Forge makes it rather
  // unpleasant to override the default behaviour, but this class does it anyway.
  public CustomGuiModList(GuiScreen parent, Minecraft minecraft, Config config, GuiService guiService)
  {
    super(parent);
    this.minecraft = minecraft;
    this.config = config;
    this.guiService = guiService;

    // we can only check which mod index is currently selected, so get the index for ChatMate
    Optional<ModContainer> mod = Loader.instance().getModList().stream().filter(m -> m.getName().equals("ChatMate")).findFirst();
    this.modIndex = Loader.instance().getModList().indexOf(mod.orElse(null));
  }

  @Override
  public void initGui()
  {
    super.initGui();
    this.configButton = this.buttonList.stream().filter(b -> b.id == 20).findFirst().get();

    // todo: the button is literally useless. in the future we can override the on-click event to implement
    // deactivation for the ChatMate mod.
    GuiButton disableModButton = this.buttonList.stream().filter(b -> b.id == 21).findFirst().get();
    this.buttonList.remove(disableModButton);
  }

  @Override
  public void updateScreen() {
    super.updateScreen();

    if (this.configButton != null && this.modIndexSelected(modIndex)) {
      // since we haven't provided a GuiFactory for ChatMate, Forge will assume there is no config screen and disable the button
      this.configButton.enabled = true;
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    // manually show the ChatMate config screen so we can inject dependencies :)
    // todo: deprecate the CustomGuiConfig screen and instead show the dashboard
    if (button == this.configButton && this.modIndexSelected(modIndex)) {
      this.minecraft.displayGuiScreen(new CustomGuiConfig(this, this.config));
//      this.guiService.onDisplayDashboard();
      return;
    }

    super.actionPerformed(button);
  }
}
