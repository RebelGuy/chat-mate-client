package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardStore.SettingsPage;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.HorizontalDivider.FillMode;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

public class SidebarElement extends ContainerElement {
  private final DashboardStore store;

  public SidebarElement(InteractiveContext context, IElement parent, DashboardStore store, List<Tuple2<SettingsPage, String>> pageNames) {
    super(context, parent, LayoutMode.BLOCK);
    super.setName("SidebarElement");

    this.store = store;

    for (Tuple2<SettingsPage, String> pair : pageNames) {
      SidebarOption option = new SidebarOption(context, this, store, pair._1, pair._2);
      super.addElement(option);
    }
  }

  private static class SidebarOption extends ContainerElement {
    private final DashboardStore store;
    private final SettingsPage page;
    private final String name;
    private final AnimatedBool isHovering;

    private final LabelElement label;
    private final HorizontalDivider horizontalDivider;

    public SidebarOption(InteractiveContext context, IElement parent, DashboardStore store, SettingsPage page, String name) {
      super(context, parent, LayoutMode.BLOCK);
      super.setSizingMode(SizingMode.FILL);
      super.setPadding(new RectExtension(gui(2)));
      super.setMargin(new RectExtension(gui(2)));

      this.store = store;
      this.page = page;
      this.name = name;
      this.isHovering = new AnimatedBool(250L, false);

      store.onSettingsPageChange(this::onSettingsPageChange);

      this.label = new LabelElement(context, this)
          .setText(this.name)
          .setOverflow(TextOverflow.SPLIT)
          .setMaxLines(3);
      this.horizontalDivider = new HorizontalDivider(context, this)
          .setColour(Colour.WHITE)
          .setMode(FillMode.PARENT_CONTENT)
          .setHorizontalAlignment(HorizontalAlignment.LEFT)
          .cast();

      super.addElement(this.label);
      super.addElement(this.horizontalDivider);

      this.setSelected(this.store.getSettingsPage() == this.page);
    }

    private void onSettingsPageChange(SettingsPage settingsPage) {
      this.setSelected(this.page == settingsPage);
    }

    private void setSelected(boolean selected) {
      this.label.setColour(selected ? Colour.WHITE : Colour.GREY);
      this.horizontalDivider.setVisible(selected);
    }

    @Override
    public void onMouseEnter(IEvent<In> e) {
      this.isHovering.set(true);
      super.context.cursorService.toggleCursor(CursorType.CLICK, this);
    }

    @Override
    public void onMouseExit(IEvent<In> e) {
      this.isHovering.set(false);
      super.context.cursorService.untoggleCursor(this);
    }

    @Override
    public void onMouseDown(IEvent<In> e) {
      if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
        this.store.setSettingsPage(this.page);
        e.stopPropagation();
      }
    }

    @Override
    public void renderElement() {
      float hoveringFrac = this.isHovering.getFrac();
      if (hoveringFrac > 0) {
        DimRect rect = super.getCollisionBox();
        Colour colour = Colour.lerp(Colour.GREY.withAlpha(0), Colour.GREY.withAlpha(0.4f), hoveringFrac);
        Dim cornerRadius = screen(2);

        RendererHelpers.drawRect(0, rect, colour, null, null, cornerRadius);
      }

      super.renderElement();
    }
  }
}
