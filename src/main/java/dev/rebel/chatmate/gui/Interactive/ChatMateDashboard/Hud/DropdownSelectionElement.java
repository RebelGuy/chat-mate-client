package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Hud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.DropdownMenuV2.AnchorBoxSizing;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.util.Collections;

import java.util.List;

public class DropdownSelectionElement extends InputElement {
  public final LabelElement label;
  public final HorizontalDivider line;
  public final DropdownMenuV2 dropdownMenuV2;
  private final BlockElement container;

  private Colour enabledColour;
  private Colour disabledColour;

  public DropdownSelectionElement(InteractiveContext context, IElement parent) {
    super(context, parent);

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
    this.dropdownMenuV2 = new DropdownMenuV2(context, this.container, AnchorBoxSizing.BORDER)
        .setSizingMode(SizingMode.FILL)
        .setMaxWidth(gui(100))
        .cast();

    this.container
        .addElement(this.label)
        .addElement(this.line)
        .addElement(this.dropdownMenuV2);
  }

  @Override
  public InputElement setEnabled(Object key, boolean enabled) {
    super.setEnabled(key, enabled);
    this.label.setColour(this.getEffectiveColour());
    this.line.setColour(this.getEffectiveColour());
    return this;
  }

  public DropdownSelectionElement setEnabledColour(Colour enabledColour) {
    this.enabledColour = enabledColour;
    return this;
  }

  public DropdownSelectionElement setDisabledColour(Colour disabledColour) {
    this.disabledColour = disabledColour;
    return this;
  }

  private Colour getEffectiveColour() {
    return super.getEnabled() ? this.enabledColour : this.disabledColour;
  }

  @Override
  public void onMouseDown(IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON && (this.dropdownMenuV2.getVisible() || super.getEnabled())) {
      this.dropdownMenuV2.toggleVisible();
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
}
