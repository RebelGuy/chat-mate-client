package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations.BackgroundElement;
import dev.rebel.chatmate.gui.Interactive.DropdownMenu.AnchorBoxSizing;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownSelectionElement<V> extends InputElement {
  public final LabelElement label;
  public final HorizontalDivider line;
  public final DropdownMenu dropdownMenu;
  private final BlockElement container;

  private @Nullable V currentSelection;
  private Map<V, Option<V, ? extends IElement>> options;

  private boolean showOptionBackground;
  private Colour enabledColour;
  private Colour disabledColour;

  public DropdownSelectionElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.currentSelection = null;
    this.options = new HashMap<>();

    this.showOptionBackground = true;
    this.enabledColour = Colour.WHITE;
    this.disabledColour = Colour.GREY50;

    this.container = new BlockElement(context, this);
    this.label = new LabelElement(context, this)
        .setColour(this.enabledColour)
        .setSizingMode(SizingMode.FILL)
        .setMargin(new RectExtension(ZERO, gui(4), ZERO, ZERO)) // leave room for the arrow
        .cast();
    this.line = new HorizontalDivider(context, this)
        .setColour(this.enabledColour)
        .setMode(HorizontalDivider.FillMode.PARENT_CONTENT);
    this.dropdownMenu = new DropdownMenu(context, this.container, AnchorBoxSizing.BORDER)
        .setSizingMode(SizingMode.FILL)
        .setMaxWidth(gui(100))
        .cast();

    this.container
        .addElement(this.label)
        .addElement(this.line)
        .addElement(this.dropdownMenu);
  }

  /** `onSelect` is only triggered by user input, NOT when programmatically setting the current selection.
   * `applySelectStyle` can be used to style the option element based on its current selection. */
  public <E extends IElement> DropdownSelectionElement<V> addOption(E element, V value, @Nullable Consumer<V> onSelect, BiConsumer<E, Boolean> applySelectStyle, Function<V, String> stringRepresentation) {
    Runnable onSelectOption = () -> {
      this.onUserSelect(value);
      if (onSelect != null) {
        onSelect.accept(value);
      }
    };
    applySelectStyle.accept(element, this.currentSelection == value);

    OptionWrapperElement optionWrapperElement = new OptionWrapperElement(super.context, this, element, onSelectOption);
    if (this.showOptionBackground) {
      this.dropdownMenu.addOption(new BackgroundElement(context, this, optionWrapperElement)
          .setCornerRadius(gui(2))
          .setHoverColour(Colour.GREY75.withAlpha(0.2f))
          .setMargin(new RectExtension(gui(1), gui(1)))
      );
    } else {
      this.dropdownMenu.addOption(optionWrapperElement);
    }

    this.options.put(value, new Option<>(element, value, applySelectStyle, stringRepresentation));
    return this;
  }

  /** Programmatically set the currently selected value. */
  public void setSelection(V value) {
    this.currentSelection = value;
    this.options.values().forEach(option -> option.applySelectStyle(option.value == value));

    if (this.options.containsKey(value)) {
      this.label.setText(this.options.get(value).stringRepresentation.apply(value));
    }
  }

  /** Returns only null if the selection has never been initialised and no selection has been made by the user yet. */
  public @Nullable V getSelection() {
    return this.currentSelection;
  }

  public DropdownSelectionElement<V> setEnabledColour(Colour enabledColour) {
    this.enabledColour = enabledColour;
    return this;
  }

  public DropdownSelectionElement<V> setDisabledColour(Colour disabledColour) {
    this.disabledColour = disabledColour;
    return this;
  }

  private void onUserSelect(V value) {
    this.setSelection(value);
    this.dropdownMenu.setVisible(false);
  }

  private Colour getEffectiveColour() {
    return super.getEnabled() ? this.enabledColour : this.disabledColour;
  }

  @Override
  public InputElement setEnabled(Object key, boolean enabled) {
    super.setEnabled(key, enabled);
    this.label.setColour(this.getEffectiveColour());
    this.line.setColour(this.getEffectiveColour());
    return this;
  }

  @Override
  public void onMouseDown(InteractiveEvent<MouseEventData> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON && (this.dropdownMenu.getVisible() || super.getEnabled())) {
      this.dropdownMenu.toggleVisible();
    }
  }

  @Override
  public List<IElement> getChildren() {
    return Collections.list(this.container);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.container.calculateSize(maxContentSize);
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);
    this.container.setBox(super.alignChild(this.container));
  }

  @Override
  protected void renderElement() {
    Dim arrowSize = gui(1.5f);
    DimPoint arrowCentre = this.label.getBox().getRightCentre().minus(new DimPoint(arrowSize.times(2), ZERO));

    // pointing down (positive y)
    RendererHelpers.drawArrow(arrowCentre, arrowSize, (float)Math.PI / 2, this.getEffectiveColour());
    this.container.render(null);
  }

  private static class OptionWrapperElement extends WrapperElement {
    private final Runnable onSelect;

    public OptionWrapperElement(InteractiveContext context, IElement parent, IElement option, Runnable onSelect) {
      super(context, parent);
      super.setCursor(CursorType.CLICK);
      this.onSelect = onSelect;

      super.setContent(option);
    }

    @Override
    public void onMouseDown(InteractiveEvent<MouseEventData> e) {
      this.onSelect.run();
      e.stopPropagation();
    }
  }

  private static class Option<V, E extends IElement> {
    private final E element;
    private final V value;
    private final BiConsumer<E, Boolean> applySelectStyle;
    private final Function<V, String> stringRepresentation;

    public Option(E element, V value, BiConsumer<E, Boolean> applySelectStyle, Function<V, String> stringRepresentation) {
      this.element = element;
      this.value = value;
      this.applySelectStyle = applySelectStyle;
      this.stringRepresentation = stringRepresentation;
    }

    // courtesy of Java's fantastic generics!
    public void applySelectStyle(boolean isSelected) {
      this.applySelectStyle.accept(this.element, isSelected);
    }
  }
}
