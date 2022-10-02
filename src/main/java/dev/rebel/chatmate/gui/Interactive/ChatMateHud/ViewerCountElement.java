package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.Interactive.SingleElement;
import dev.rebel.chatmate.gui.hud.DigitReel;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ViewerCountElement extends SingleElement implements SeparableHudElement.ISeparableElement {
  private static final int MAX_REEL_VALUE = 99;

  private final boolean isMainElement;
  private final StatusService statusService;
  private final Config config;
  private final DigitReel reel;
  private final Font indicatorFont;

  private float scale = 1;
  private String prevText = "";

  public ViewerCountElement(InteractiveContext context, IElement parent, boolean isMainElement, Dim defaultHeight, StatusService statusService, Config config) {
    super(context, parent);
    this.isMainElement = isMainElement;
    this.statusService = statusService;
    this.config = config;
    this.reel = new DigitReel(context.minecraft, context.dimFactory, context.fontEngine);

    this.indicatorFont = new Font().withShadow(new Shadow(context.dimFactory));
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
    String text = viewerCount == null ? "n/a" : String.format("%02d", viewerCount);

    if (!Objects.equals(this.prevText, text)) {
      super.onInvalidateSize();
      this.prevText = text;
    }

    return text;
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  public void setHudScale(float scale) {
    this.scale = scale;
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

  public static class Factory implements SeparableHudElement.ISeparableElementFactory {
    private final Config config;
    private final StatusService statusService;

    public Factory(Config config, StatusService statusService) {
      this.config = config;
      this.statusService = statusService;
    }

    @Override
    public ViewerCountElement create(InteractiveScreen.InteractiveContext context, IElement parent, boolean isMainIndicator, Dim defaultHeight) {
      return new ViewerCountElement(context, parent, isMainIndicator, defaultHeight, this.statusService, this.config);
    }
  }
}
