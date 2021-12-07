package dev.rebel.chatmate.services;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class SoundService {
    public SoundService() {
        // see https://minecraft.fandom.com/wiki/Sounds.json for all possible ResourceLocations
        // testing can be done in-game with commands enabled using
        // /tp 0 0 0
        // /playsound <resource.location> @a 0 0 0 <volume> <pitch>
    }

    public void playDing() {
        this.playSound("random.successful_hit", 0.5F);
    }

    private void playSound(String resourceLocation) {
        this.playSound(resourceLocation, 1);
    }
    private void playSound(String resourceLocation, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(resourceLocation), pitch));
    }
}
