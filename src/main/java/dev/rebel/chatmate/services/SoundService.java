package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.Config;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class SoundService {
  private final LogService logService;
  private final MinecraftProxyService minecraftProxyService;
  private final Config config;

  public SoundService(LogService logService, MinecraftProxyService minecraftProxyService, Config config) {
    // see https://minecraft.fandom.com/wiki/Sounds.json for all possible ResourceLocations.
    // there is also a list in .minecraft/assets/indexes/1.8.json
    // testing can be done in-game with commands enabled using
    // /playsound <resource.location> @a ~ ~ ~ <volume> <pitch>
    // note that 0 <= volume <= 1 and 0.5 <= pitch <= 2

    this.logService = logService;
    this.minecraftProxyService = minecraftProxyService;
    this.config = config;
  }

  public void playDing() {
    this.playSound("random.successful_hit", 0.5F);
  }

  public void playButtonSound() { this.playSound("gui.button.press"); }

  public void playLevelUp(float pitch) {
    this.playSound("random.levelup", pitch);
  }

  public void playDragonKill(float pitch) {
    this.playSound("mob.enderdragon.end", pitch);
  }

  private void playSound(String resourceLocation) {
    this.playSound(resourceLocation, 1);
  }
  private void playSound(String resourceLocation, float pitch) {
    if (!this.config.getSoundEnabledEmitter().get()) {
      return;
    }

    if (pitch < 0.5f) {
      pitch = 0.5f;
    } else if (pitch > 2) {
      pitch = 2;
    }

    this.logService.logInfo(this, "Playing sound", resourceLocation, "with pitch", pitch);
    ISound sound = PositionedSoundRecord.create(new ResourceLocation(resourceLocation), pitch);
    this.minecraftProxyService.playSound(sound);
  }
}
