package dev.rebel.chatmate.gui.components.interactive.SimpleButton;

import dev.rebel.chatmate.gui.components.ComponentManager;
import dev.rebel.chatmate.gui.components.View;

import javax.annotation.Nonnull;

public class SimpleButtonView extends View<SimpleButton.VProps, SimpleButton.State> {
  private final static SimpleButton.State initialState = new SimpleButton.State();

  public SimpleButtonView(ComponentManager manager) {
    super(manager, initialState);
  }

  @Override
  protected void onInitialise(@Nonnull SimpleButton.VProps initialProps) {

  }

  @Override
  protected void onUpdate(@Nonnull SimpleButton.VProps prevProps, @Nonnull SimpleButton.VProps props) {

  }

  @Override
  protected void onDispose() {

  }

  @Override
  protected void onRenderComponents() {

  }

  @Override
  protected void onRenderScreen() {

  }
}
