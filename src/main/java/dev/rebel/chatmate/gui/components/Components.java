package dev.rebel.chatmate.gui.components;

import dev.rebel.chatmate.gui.shared.components.SimpleButton.SimpleButton;

public class Components {
  public static class Interactive {
    public static class Buttons {
      public static Component.StaticComponent SimpleButton(String id, SimpleButton.Props props, Component.StaticComponent... children) {
        return new Component.StaticComponent(id, props, SimpleButton.class, children);
      }
    }
  }
}
