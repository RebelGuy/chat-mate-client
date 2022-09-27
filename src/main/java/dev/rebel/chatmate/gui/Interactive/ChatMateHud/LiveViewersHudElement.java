package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.BlockElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.Interactive.SingleElement;
import dev.rebel.chatmate.gui.hud.DigitReel;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.StatefulEmitter;
import dev.rebel.chatmate.services.StatusService;

import javax.annotation.Nullable;
import java.util.List;

public class LiveViewersHudElement extends SimpleHudElementWrapper<BlockElement> {
  private final Config config;
  private final BlockElement container;

  private final static int INITIAL_X_GUI = 23;
  private final static int INITIAL_Y_GUI = 10;

  private final LiveViewersElement mainElement;
  private final LiveViewersElement secondaryElement;

  public LiveViewersHudElement(InteractiveContext context, IElement parent, StatusService statusService, Config config) {
    super(context, parent);
    super.setCanDrag(true);
    super.setCanScale(true);
    super.setDefaultPosition(new DimPoint(gui(INITIAL_X_GUI), gui(INITIAL_Y_GUI)), Anchor.TOP_LEFT);
    super.setScrollResizeAnchor(null); // auto
    super.setHudElementFilter(); // shown everywhere

    this.mainElement = new LiveViewersElement(context, this, true, statusService, config)
        .cast();
    this.secondaryElement = new LiveViewersElement(context, this, false, statusService, config)
        .setMargin(new RectExtension(ZERO, ZERO, gui(4), ZERO))
        .cast();
    this.container = new BlockElement(context, this)
        .addElement(this.mainElement)
        .addElement(this.secondaryElement)
        .cast();
    super.setElement(this.container);

    this.config = config;
  }

  @Override
  protected void onRescaleContent(DimRect oldBox, float oldScale, float newScale) {
    this.mainElement.setScale(newScale);
    this.secondaryElement.setScale(newScale).setMargin(new RectExtension(ZERO, ZERO, gui(5 * newScale), ZERO));
  }

  protected static class LiveViewersElement extends SingleElement {
    private static final int MAX_REEL_VALUE = 99;

    private final boolean isMainElement;
    private final StatusService statusService;
    private final Config config;
    private final DigitReel reel;
    private final Font indicatorFont;

    private float scale = 1;

    public LiveViewersElement(InteractiveContext context, IElement parent, boolean isMainElement, StatusService statusService, Config config) {
      super(context, parent);
      this.isMainElement = isMainElement;
      this.statusService = statusService;
      this.config = config;
      this.reel = new DigitReel(context.minecraft, context.dimFactory, context.fontEngine);

      this.indicatorFont = new Font().withShadow(new Shadow(context.dimFactory));

      config.getViewerCountEmitter().onChange(this::onChangeViewerCountConfig);
      this.onChangeViewerCountConfig(config.getViewerCountEmitter().get());
    }

    private void onChangeViewerCountConfig(Config.SeparableHudElement data) {
      this.updateVisibility(data.enabled, data.separatePlatforms);
    }

    private void updateVisibility(boolean showViewerCount, boolean identifyPlatforms) {
      if (this.isMainElement) {
        super.setVisible(showViewerCount);
      } else {
        super.setVisible(showViewerCount && identifyPlatforms);
      }
    }

    private @Nullable Integer getViewerCount() {
      if (this.config.getViewerCountEmitter().get().separatePlatforms) {
        if (this.isMainElement) {
          return this.statusService.getYoutubeLiveViewerCount();
        } else {
          return this.statusService.getTwitchLiveViewerCount();
        }
      } else {
        return this.statusService.getTotalLiveViewerCount();
      }
    }

    private String getText() {
      @Nullable Integer viewerCount = this.getViewerCount();
      return viewerCount == null ? "n/a" : String.format("%02d", viewerCount);
    }

    @Override
    public List<IElement> getChildren() {
      return null;
    }

    public LiveViewersElement setScale(float scale) {
      this.scale = scale;
      return this;
    }

    @Override
    protected DimPoint calculateThisSize(Dim maxContentSize) {
      String text = this.getText();
      return new DimPoint(super.context.fontEngine.getStringWidthDim(text), super.context.fontEngine.FONT_HEIGHT_DIM).scale(this.scale);
    }

    @Override
    protected void renderElement() {
      RendererHelpers.withMapping(this.getContentBox().getPosition(), this.scale, () -> {
        String text = this.getText();
        @Nullable Integer viewerCount = this.getViewerCount();
        if (viewerCount == null || viewerCount > MAX_REEL_VALUE) {
          this.fontEngine.drawString(text, 0, 0, this.indicatorFont);
        } else {
          Dim digitWidth = this.fontEngine.getCharWidth('0');
          Dim digitPadding = gui(1);
          Dim digitHeight = super.context.fontEngine.FONT_HEIGHT_DIM;
          reel.drawReel(text, digitWidth, digitPadding, digitHeight);
        }
      });
    }
  }
}
