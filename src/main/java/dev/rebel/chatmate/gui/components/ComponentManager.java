package dev.rebel.chatmate.gui.components;

import java.util.*;

// each component instance has a component manager which ensures that child components over multiple frames refer to
// the same component instance, even if they were disposed at any point.
// note that IDs only have to be unique among a given component type within a component.
public final class ComponentManager {
  public static GuiContext guiContext;

  // holds a map of component types to multiple instances of that component
  private Map<Class<? extends Component.ComponentFactory>, Map<String, Component>> componentMap = new HashMap<>();

  public Component.ReadyComponent getOrCreate(Component.StaticComponent staticComponent) {
    Class<? extends Component.ComponentFactory> factory = staticComponent.factory;
    String id = staticComponent.id;
    if (!this.componentMap.containsKey(factory)) {
      this.componentMap.put(factory, new HashMap<>());
    }

    Map<String, Component> mapForType = this.componentMap.get(factory);
    if (!mapForType.containsKey(id)) {
      try {
        // yeah it's yucky, but thanks to Java at SOME point we had to use reflection unless we want to sacrifice
        // maintainability completely by writing everything manually.
        mapForType.put(id, new Component(guiContext, (Component.ComponentFactory)factory.getConstructors()[0].newInstance()));
      } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
    }

    Component component = mapForType.get(id);
    Component.ReadyComponent[] children = Arrays.stream(staticComponent.children).map(this::getOrCreate).toArray(Component.ReadyComponent[]::new);
    return new Component.ReadyComponent(component, staticComponent.nextProps, children);
  }
}
