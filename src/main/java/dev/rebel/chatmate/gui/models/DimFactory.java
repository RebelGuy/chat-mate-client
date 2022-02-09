package dev.rebel.chatmate.gui.models;

import dev.rebel.chatmate.util.Memoiser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/** Dimension factory. */
public class DimFactory {
  private final Memoiser memoiser;
  private final Minecraft minecraft;

  public DimFactory(Minecraft minecraft) {
    this.memoiser = new Memoiser();
    this.minecraft = minecraft;
  }

  public Dim fromScreen(float screenValue) {
    return new Dim(this::getScaleFactor).setScreen(screenValue);
  }

  public Dim fromGui(float guiValue) {
    return new Dim(this::getScaleFactor).setGui(guiValue);
  }

  public Dim zero() {
    return this.fromScreen(0);
  }

  private int getScaleFactor() {
    return this.memoiser.memoise("getScaleFactor", () -> new ScaledResolution(this.minecraft).getScaleFactor(),
        this.minecraft.displayHeight, this.minecraft.displayWidth, this.minecraft.gameSettings.guiScale);
  }
}
