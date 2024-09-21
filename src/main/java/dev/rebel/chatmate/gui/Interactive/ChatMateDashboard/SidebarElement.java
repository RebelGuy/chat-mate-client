package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardStore.SettingsPage;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.HorizontalDivider.FillMode;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.Interactive.RequireStreamerElement.RequireStreamerOptions;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.services.CursorService.CursorType;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.List;

import static dev.rebel.chatmate.gui.Interactive.RendererHelpers.drawRect;

public class SidebarElement extends ContainerElement {
  private final DashboardStore store;

  public SidebarElement(InteractiveContext context, IElement parent, DashboardStore store, List<Tuple2<SettingsPage, PageOptions>> pages) {
    super(context, parent, LayoutMode.BLOCK);
    super.setName("SidebarElement");

    this.store = store;

    for (Tuple2<SettingsPage, PageOptions> page : pages) {
      SidebarOption option = new SidebarOption(context, this, store, page._1, page._2.name);
      if (page._2.requiredStreamer) {
        super.addElement(new RequireStreamerElement(super.context, this, option, RequireStreamerOptions.forInline()));
      } else {
        super.addElement(option);
      }
    }

    super.addElement(
        new InlineElement(context, this)
            .addElement(new UrlElement(context, this, "ChatMate Website", context.environment.studioUrl))
            .addElement(
                new ImageElement(context, this)
                    .setImage(Asset.GUI_EXTERNAL_ICON)
                    .setScale(1.0f / 12)
                    .setPadding(new RectExtension(gui(2), ZERO, ZERO, ZERO))
                    .setSizingMode(SizingMode.MINIMISE)
                    .setVerticalAlignment(VerticalAlignment.TOP)
                )
            .setVerticalAlignment(VerticalAlignment.BOTTOM)
            .setSizingMode(SizingMode.FILL)
    );

    super.addElement(new LabelElement(context, this)
        .setText(context.environment.buildName)
        .setColour(Colour.GREY75)
        .setFontScale(0.5f)
        .setPadding(new RectExtension(gui(0), gui(0), gui(4), gui(0)))
        .setMargin(new RectExtension(gui(0), gui(0), gui(0), gui(-8))) // this actually works... wow!
    );
  }

  private static class SidebarOption extends ContainerElement {
    private final DashboardStore store;
    private final SettingsPage page;
    private final String name;
    private final AnimatedBool isHovering;

    private final LabelElement label;
    private final HorizontalDivider horizontalDivider;
    private @Nullable IElement exclamationLabel;

    private final EventCallback<Boolean> _onChangeDebugModeEnabled = this::onChangeDebugModeEnabled;

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
          .setMaxOverflowLines(3);
      this.horizontalDivider = new HorizontalDivider(context, this)
          .setColour(Colour.WHITE)
          .setMode(FillMode.PARENT_CONTENT)
          .setHorizontalAlignment(HorizontalAlignment.LEFT)
          .cast();
      this.exclamationLabel = null;

      super.addElement(this.label);
      super.addElement(this.horizontalDivider);

      this.setSelected(this.store.getSettingsPage() == this.page);

      if (page == SettingsPage.DEBUG) {
        context.config.getDebugModeEnabledEmitter().onChange(this._onChangeDebugModeEnabled, this, true);
        super.addDisposer(() -> super.context.config.getDebugModeEnabledEmitter().off(this));
      } else if (page == SettingsPage.NOTIFICATIONS) {
        super.registerAllStores(true);
      }
    }

    @Override
    protected void onStoreUpdate() {
      if (this.page != SettingsPage.NOTIFICATIONS) {
        return;
      }

      boolean hasErrors = super.context.livestreamApiStore.getError(true) != null ||
          super.context.donationApiStore.getError(true) != null ||
          super.context.streamerApiStore.getError(true) != null;

      if (!hasErrors) {
        super.removeElement(this.exclamationLabel);
        this.exclamationLabel = null;
      } else if (this.exclamationLabel == null) {
        this.exclamationLabel = new ExclamationLabel(super.context, this);
        super.addElement(this.exclamationLabel);
      }
    }

    @Override
    public ContainerElement setVisible(boolean visible) {
      super.setVisible(visible);
      this.setSelected(this.store.getSettingsPage() == this.page);
      return this;
    }

    private void onSettingsPageChange(SettingsPage settingsPage) {
      this.setSelected(this.page == settingsPage);
    }

    private void setSelected(boolean selected) {
      this.label.setColour(selected ? Colour.WHITE : Colour.GREY50);
      this.horizontalDivider.setVisible(selected);
    }

    private void onChangeDebugModeEnabled(Event<Boolean> event) {
      super.setVisible(event.getData());
      this.setSelected(this.store.getSettingsPage() == this.page); // make sure the appearance is correct
    }

    @Override
    public void onMouseEnter(InteractiveEvent<MouseEventData> e) {
      this.isHovering.set(true);
      super.context.cursorService.toggleCursor(CursorType.CLICK, this, super.getDepth());
    }

    @Override
    public void onMouseExit(InteractiveEvent<MouseEventData> e) {
      this.isHovering.set(false);
      super.context.cursorService.untoggleCursor(this);
    }

    @Override
    public void onMouseDown(InteractiveEvent<MouseEventData> e) {
      if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
        this.store.setSettingsPage(this.page);
        e.stopPropagation();
      }
    }

    @Override
    protected void renderElement() {
      float hoveringFrac = this.isHovering.getFrac();
      if (hoveringFrac > 0) {
        DimRect rect = super.getCollisionBox();
        Colour colour = Colour.lerp(Colour.GREY50.withAlpha(0), Colour.GREY50.withAlpha(0.4f), hoveringFrac);
        Dim cornerRadius = screen(2);

        drawRect(0, rect, colour, null, null, cornerRadius);
      }

      super.renderElement();
    }
  }

  public static class PageOptions {
    public final String name;
    public final boolean requiredStreamer;

    public PageOptions(String name, boolean requiredStreamer) {
      this.name = name;
      this.requiredStreamer = requiredStreamer;
    }
  }

  private static class ExclamationLabel extends WrapperElement {
    public ExclamationLabel(InteractiveContext context, IElement parent) {
      super(context, parent);
      super.setLayoutGroup(Layout.LayoutGroup.CHILDREN);
      super.setSizingMode(SizingMode.MINIMISE);

      LabelElement labelElement = new LabelElement(context, this)
          .setText("!")
          .setFontScale(0.5f)
          .setHorizontalAlignment(HorizontalAlignment.CENTRE)
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .setPadding(RectExtension.fromLeft(gui(0.5f)))
          .setMargin(new RectExtension(gui(0.5f)))
          .cast();
      this.setContent(labelElement);
    }

    @Override
    public void setBox(DimRect box) {
      // on first render, we may not yet know where the parent is - force one more pass
      @Nullable DimRect parentBox = super.parent.getBox();
      if (parentBox == null) {
        super.setBox(box);
        super.onInvalidateSize();
        return;
      }

      // we decide where we want to be placed - and it's centred in the top right corner of the parent
      Dim maxDim = Dim.max(box.getSize().getX(), box.getSize().getY());
      DimPoint size = new DimPoint(maxDim, maxDim);
      box = new DimRect(parentBox.getTopRight().minus(new DimPoint(maxDim, ZERO)), size);
      super.setBox(box);
    }

    @Override
    protected void renderElement() {
      // draw circular box
      drawRect(getEffectiveZIndex(), super.getBox(), Colour.RED, null, null, super.getBox().getWidth().over(2));

      super.renderElement();
    }
  }
}
