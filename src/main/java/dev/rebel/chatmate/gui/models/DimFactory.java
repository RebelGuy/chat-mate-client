package dev.rebel.chatmate.gui.models;

import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
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

  /** Create a `Dim` that is anchored to a screen value. */
  public Dim fromScreen(float screenValue) {
    return new Dim(this::getScaleFactor, DimAnchor.SCREEN).setScreen(screenValue);
  }

  /** Create a `Dim` that is anchored to a gui value. */
  public Dim fromGui(float guiValue) {
    return new Dim(this::getScaleFactor, DimAnchor.GUI).setGui(guiValue);
  }

  public Dim zeroGui() {
    return this.fromGui(0);
  }

  private int getScaleFactor() {
    return this.memoiser.memoise("getScaleFactor", () -> new ScaledResolution(this.minecraft).getScaleFactor(),
        this.minecraft.displayHeight, this.minecraft.displayWidth, this.minecraft.gameSettings.guiScale);
  }
}