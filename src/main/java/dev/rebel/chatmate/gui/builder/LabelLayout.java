package dev.rebel.chatmate.gui.builder;

import dev.rebel.chatmate.gui.builder.ContentLayout.NO_ACTION;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class LabelLayout extends ContentLayout<GuiLabel, NO_ACTION> {
  private final FontRenderer fontRenderer;
  private final int id;
  private final Supplier<String> onRenderText;
  private final int color;

  private @Nullable Supplier<GuiLabel> lastInstantiator;

  private GuiLabel label;

  // use 0xFFFFFF for white
  public LabelLayout(FontRenderer fontRenderer, String[] width, Supplier<String> onRenderText, int color) {
    super(width);
    this.fontRenderer = fontRenderer;
    this.id = (int)Math.round(Math.random() * Integer.MAX_VALUE);
    this.onRenderText = onRenderText;
    this.color = color;

    this.lastInstantiator = null;

    this.label = null;
  }

  public GuiLabel instantiateGui(int x, int y, int width, int height) {
    this.lastInstantiator = () -> new GuiLabel(this.fontRenderer, this.id, x, y, width, height, this.color);
    this.refreshContents();
    return this.label;
  }

  public GuiLabel tryGetGui() {
    return this.label;
  }

  public void refreshContents() {
    if (this.lastInstantiator != null) {
      // can not modify text, have to add line - thus we have to instantiate a new object every time
      this.label = this.lastInstantiator.get();
      this.label.func_175202_a(this.onRenderText.get());
    }
  }

  public boolean dispatchAction(NO_ACTION no_op) { return false; }
}
