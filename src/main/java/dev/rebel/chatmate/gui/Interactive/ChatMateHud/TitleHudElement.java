package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.CustomGuiChat;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.hud.TitleComponent;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.services.util.TextHelpers;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Objects;

public class TitleHudElement extends SimpleHudElementWrapper<BlockElement> {
  private final static int TITLE_SCALE = 4;
  private final static int SUB_TITLE_SCALE = 2;
  private final static int DEFAULT_PADDING = 14;
  
  private final BlockElement container;
  private final LabelElement titleElement;
  private final LabelElement subTitleElement;

  private @Nullable TitleOptions options = null;
  private @Nullable Long startTime = null;

  public TitleHudElement(InteractiveContext context, IElement parent) {
    super(context, parent);
    super.setCanDrag(true);
    super.setCanScale(true);
    super.setDefaultPosition(context.dimFactory.getMinecraftRect().getCentre(), Anchor.MIDDLE);
    super.setScrollResizeAnchor(Anchor.MIDDLE);
    super.setContentResizeAnchor(Anchor.MIDDLE);
    super.setHudElementFilter(
        new HudFilters.HudFilterWhitelistNoScreen(),
        new HudFilters.HudFilterScreenWhitelist(CustomGuiChat.class),
        new HudFilters.HudFilterInteractiveScreenTypeBlacklist(InteractiveScreenType.DASHBOARD, InteractiveScreenType.MODAL)
    );

    this.titleElement = new LabelElement(context, this)
        .setFontScale(TITLE_SCALE)
        .setHorizontalAlignment(HorizontalAlignment.CENTRE)
        .cast();
    this.subTitleElement = new LabelElement(context, this)
        .setFontScale(SUB_TITLE_SCALE)
        .setHorizontalAlignment(HorizontalAlignment.CENTRE)
        .setMargin(new RectExtension(ZERO, ZERO, gui(DEFAULT_PADDING), ZERO))
        .cast();
    this.container = new BlockElement(context, this);
    this.container.addElement(this.titleElement);
    this.container.addElement(this.subTitleElement);
    super.setElement(this.container);
  }

  public TitleHudElement setTitle(@Nullable TitleOptions options) {
    if (!Objects.equals(this.options, options)) {
      this.titleElement.setText(options.title).setVisible(!TextHelpers.isNullOrEmpty(options.title));
      this.subTitleElement.setText(options.subTitle).setVisible(!TextHelpers.isNullOrEmpty(options.subTitle));
      this.options = options;
      this.startTime = getTime();
      super.onInvalidateSize();
    }
    return this;
  }
  
  private TitleProgress getProgress() {
    if (this.options == null) {
      return new TitleProgress(TitlePhase.HIDDEN, 0);
    }

    long t = getTime();
    if (this.startTime == null) {
      return new TitleProgress(TitlePhase.HIDDEN, 0);
    }

    long endFadeIn = this.startTime + this.options.fadeInTime;
    long endDisplay = endFadeIn + this.options.displayTime;
    long endFadeOut = endDisplay + this.options.fadeOutTime;

    if (t < endFadeIn) {
      return new TitleProgress(TitlePhase.FADE_IN, (float)(t - this.startTime) / this.options.fadeInTime);
    } else if (t < endDisplay) {
      return new TitleProgress(TitlePhase.SHOWN, (float)(t - endFadeIn) / this.options.displayTime);
    } else if (t < endFadeOut) {
      return new TitleProgress(TitlePhase.FADE_OUT, (float)(t - endDisplay) / this.options.fadeOutTime);
    } else {
      return new TitleProgress(TitlePhase.HIDDEN, 0);
    }
  }

  private static long getTime() {
    return new Date().getTime();
  }
  
  @Override
  protected void onRescaleContent(DimRect oldBox, float oldScale, float newScale) {
    this.titleElement.setFontScale(TITLE_SCALE * newScale);
    this.subTitleElement.setFontScale(SUB_TITLE_SCALE * newScale)
        .setMargin(new RectExtension(ZERO, ZERO, gui(DEFAULT_PADDING * newScale), ZERO));
  }

  @Override
  public void onRenderElement() {
    TitleProgress progress = this.getProgress();
    if (progress.phase == TitlePhase.HIDDEN) {
      return;
    }

    float alpha;
    if (progress.phase == TitlePhase.FADE_IN) {
      alpha = progress.progress;
    } else if (progress.phase == TitlePhase.FADE_OUT) {
      alpha = 1 - progress.progress;
    } else {
      alpha = 1;
    }
    Font font = new Font().withColour(Colour.WHITE.withAlpha(alpha)).withShadow(new Shadow(super.context.dimFactory));
    this.titleElement.setFont(font);
    this.subTitleElement.setFont(font);

    super.onRenderElement();
  }

  private static class TitleProgress {
    public final TitlePhase phase;
    /** 0 <= x < 1 */
    public final float progress;

    private TitleProgress(TitlePhase phase, float progress) {
      this.phase = phase;
      this.progress = progress;
    }
  }

  private enum TitlePhase {
    HIDDEN, FADE_IN, SHOWN, FADE_OUT
  }

  public static class TitleOptions {
    public final String title;
    public final @Nullable String subTitle;
    public final long fadeInTime;
    public final long displayTime;
    public final long fadeOutTime;

    public TitleOptions(String title, @Nullable String subTitle, long fadeInTime, long displayTime, long fadeOutTime) {
      this.title = title;
      this.subTitle = subTitle;
      this.fadeInTime = fadeInTime;
      this.displayTime = displayTime;
      this.fadeOutTime = fadeOutTime;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TitleOptions that = (TitleOptions) o;
      return fadeInTime == that.fadeInTime && displayTime == that.displayTime && fadeOutTime == that.fadeOutTime && Objects.equals(title, that.title) && Objects.equals(subTitle, that.subTitle);
    }

    @Override
    public int hashCode() {
      return Objects.hash(title, subTitle, fadeInTime, displayTime, fadeOutTime);
    }
  }
}
