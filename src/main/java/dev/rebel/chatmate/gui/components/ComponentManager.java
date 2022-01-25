package dev.rebel.chatmate.gui.components;

import dev.rebel.chatmate.gui.components.interactive.SimpleButton.SimpleButton;
import dev.rebel.chatmate.gui.components.interactive.SimpleButton.SimpleButtonController;

import java.util.*;
import java.util.stream.Collectors;

// each component has a component manager which ensures that child components over multiple frames refer to
// the same component instance, even if they were disposed at any point.
// note that IDs only have to be unique among a given component type within a component.
public final class ComponentManager {
  public static GuiContext guiContext;

  // holds a map of component types to multiple instances of that component
  private Map<Class<? extends Component.LazyFactory>, Map<String, Component>> componentMap = new HashMap<>();

  public ReadyComponent getOrCreate(StaticComponent staticComponent) {
    Class<? extends Component.LazyFactory> factory = staticComponent.factory;
    String id = staticComponent.id;
    if (!this.componentMap.containsKey(factory)) {
      this.componentMap.put(factory, new HashMap<>());
    }

    Map<String, Component> mapForType = this.componentMap.get(factory);
    if (!mapForType.containsKey(id)) {
      try {
        // yeah it's yucky, but thanks to Java at SOME point we had to use reflection unless we want to sacrifice
        // maintainability completely by writing everything manually.
        mapForType.put(id, new Component(guiContext, (Component.LazyFactory)factory.getConstructors()[0].newInstance()));
      } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
    }

    Component component = mapForType.get(id);
    ReadyComponent[] children = Arrays.stream(staticComponent.children).map(this::getOrCreate).toArray(ReadyComponent[]::new);
    return new ReadyComponent(component, staticComponent.nextProps, children);
  }

  public static class Interactive {
    public static class Buttons {
      public static StaticComponent SimpleButton(String id, SimpleButtonController.Props props, StaticComponent...children) { return new StaticComponent(id, props, SimpleButton.class, children); }
    }
  }

  public static class StaticComponent {
    public final String id;
    public final Data nextProps;
    public final Class<? extends Component.LazyFactory> factory;
    public final StaticComponent[] children;

    private StaticComponent(String id, Data nextProps, Class<? extends Component.LazyFactory> factory, StaticComponent[] children) {
      this.id = id;
      this.nextProps = nextProps;
      this.factory = factory;
      this.children = children;
    }
  }

  public static class ReadyComponent {
    public final Component component;
    public final Data props;
    public final ReadyComponent[] children;

    public ReadyComponent(Component component, Data props, ReadyComponent[] children) {
      this.component = component;
      this.props = props;
      this.children = children;
    }

    /** The ordering ensures that parents are always listed before children. */
    public List<Component> getOrderedComponents() {
      List<Component> list = new ArrayList<>();
      list.add(this.component);

      List<Component> children = Arrays.stream(this.children).flatMap(child -> child.getOrderedComponents().stream()).collect(Collectors.toList());
      list.addAll(children);

      return list;
    }
  }
}
