package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.api.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.HudElementTransform;
import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.EnumHelpers;

import javax.annotation.Nullable;
import java.util.Map;

/** A HUD element that can be separated into platform-specific elements, with optional platform icons. */
public class SeparableHudElement extends SimpleHudElementWrapper<ContainerElement> {
  private final Config.StatefulEmitter<Config.SeparableHudElement> settings;

  private final Dim defaultHeight;
  private final boolean isMainElement;
  private final ISeparableElement mainElement;
  private final float platformIconScale; // image scale at 100% hud scale
  private final ImageElement platformIcon;

  private @Nullable PlatformIconPosition prevIconPosition; // null if it isn't shown
  private boolean requiresSecondPass; // if true, should re-initialise the container after the box has been set

  public SeparableHudElement(InteractiveContext context,
                             IElement parent,
                             boolean isMainElement,
                             ISeparableElementFactory separableMainElementFactory,
                             Config.StatefulEmitter<Config.SeparableHudElement> settings,
                             Config.StatefulEmitter<Map<String, HudElementTransform>> transformEmitter,
                             HudElementTransform defaultTransform,
                             String persistName) {
    super(context, parent);

    this.settings = settings;
    this.name = persistName;
    this.defaultHeight = gui(8); // size at 100% scale
    this.isMainElement = isMainElement;

    super.setCanDrag(true);
    super.setCanScale(true);
    super.setScrollResizeAnchor(Anchor.MIDDLE);
    super.setContentResizeAnchor(Anchor.TOP_LEFT); // when toggling the platform icon, fix the hud element's position. this can result in overlap between the indicators and icons if set to top/bottom, and it is up to the user to fix this
    super.setHudElementFilter(); // shown everywhere

    this.prevIconPosition = null;
    this.requiresSecondPass = false;

    this.mainElement = separableMainElementFactory.create(context, this, this.isMainElement, this.defaultHeight)
        .setMargin(new RectExtension(gui(2)))
        .cast();

    Texture image = this.isMainElement ? Asset.LOGO_YOUTUBE : Asset.LOGO_TWITCH;
    this.platformIconScale = this.defaultHeight.over(gui(image.width));
    this.platformIcon = new ImageElement(context, this)
        .setImage(image)
        .setScale(this.platformIconScale)
        .setMargin(new RectExtension(gui(2)))
        .cast();

    settings.onChange(this::onChangeStatusIndicatorSettings);
    this.onChangeStatusIndicatorSettings(new Event<>(settings.get()));

    settings.onChange(this::onChangeStatusIndicatorConfig);
    this.onChangeStatusIndicatorConfig(new Event<>(settings.get()));

    super.setDefaultPosition(defaultTransform.getPosition(), Anchor.TOP_LEFT);
    super.setDefaultScale(defaultTransform.scale);
    super.enablePersistTransform(persistName);
  }

  private void onChangeStatusIndicatorSettings(Event<Config.SeparableHudElement> event) {
    // updating (or toggling) the icon position is done in two steps so that the indicator appears stationary, while the icon "orbits" around it
    // 1. remove the existing icon by creating a new container that is resize-anchored such that the indicator doesn't move (this is done here)
    // 2. add the new icon (if applicable) to the container, changing the resize anchor again such that the indicator doesn't move (this is done just after setting the box)

    Config.SeparableHudElement settings = event.getData();
    Anchor resizeAnchor = Anchor.TOP_LEFT;
    if (this.prevIconPosition != null) {
      switch (this.prevIconPosition) {
        case LEFT:
          resizeAnchor = Anchor.RIGHT_CENTRE;
          break;
        case RIGHT:
          resizeAnchor = Anchor.LEFT_CENTRE;
          break;
        case TOP:
          resizeAnchor = Anchor.BOTTOM_CENTRE;
          break;
        case BOTTOM:
          resizeAnchor = Anchor.TOP_CENTRE;
          break;
        default:
          throw EnumHelpers.<PlatformIconPosition>assertUnreachable(settings.platformIconPosition);
      }
    }

    super.setContentResizeAnchor(resizeAnchor);
    super.setElement(new BlockElement(super.context, this).addElement(this.mainElement)); // first pass

    if (settings.separatePlatforms && settings.showPlatformIcon) {
      this.prevIconPosition = settings.platformIconPosition;
      this.requiresSecondPass = true;
    } else {
      this.prevIconPosition = null;
      this.requiresSecondPass = false;
    }
  }

  private void onChangeStatusIndicatorConfig(Event<Config.SeparableHudElement> event) {
    Config.SeparableHudElement data = event.getData();
    this.updateVisibility(data.enabled, data.separatePlatforms);
  }

  private void updateVisibility(boolean indicatorEnabled, boolean separatePlatforms) {
    if (this.isMainElement) {
      super.setVisible(indicatorEnabled);
    } else {
      super.setVisible(indicatorEnabled && separatePlatforms);
    }
  }

  @Override
  protected void onElementRescaled(float oldScale, float newScale) {
    this.mainElement.setHudScale(newScale);
    this.platformIcon.setScale(this.platformIconScale * newScale);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    super.onHudBoxSet(box);

    if (this.requiresSecondPass) {
      this.requiresSecondPass = false;

      Config.SeparableHudElement settings = this.settings.get();
      if (!(settings.separatePlatforms && settings.showPlatformIcon)) {
        return;
      }

      // the current box is only for the indicator. here we add the icon as well (in a new container, so the ordering/layout is right)
      ContainerElement container;
      Anchor resizeAnchor;
      switch (settings.platformIconPosition) {
        case TOP:
          container = new BlockElement(super.context, this)
              .addElement(this.platformIcon)
              .addElement(this.mainElement);
          resizeAnchor = Anchor.BOTTOM_CENTRE;
          break;
        case BOTTOM:
          container = new BlockElement(super.context, this)
              .addElement(this.mainElement)
              .addElement(this.platformIcon);
          resizeAnchor = Anchor.TOP_CENTRE;
          break;
        case LEFT:
          container = new InlineElement(super.context, this)
              .addElement(this.platformIcon)
              .addElement(this.mainElement);
          resizeAnchor = Anchor.RIGHT_CENTRE;
          break;
        case RIGHT:
          container = new InlineElement(super.context, this)
              .addElement(this.mainElement)
              .addElement(this.platformIcon);
          resizeAnchor = Anchor.LEFT_CENTRE;
          break;
        default:
          throw EnumHelpers.<PlatformIconPosition>assertUnreachable(settings.platformIconPosition);
      }

      super.setContentResizeAnchor(resizeAnchor);
      super.setElement(container);
    } else {
      // we have finished our second pass (or don't need to do it), set the anchor back to the default
      super.setContentResizeAnchor(Anchor.TOP_LEFT);
    }
  }

  private boolean isLive() {
    PublicLivestreamStatus status = super.context.statusService.getLivestreamStatus();
    return status != null && (status.isYoutubeLive() || status.isTwitchLive());
  }

  @Override
  public void onRenderElement() {
    if (super.context.config.getOnlyShowIndicatorsWhenLive().get() && !this.isLive()) {
      return;
    }

    super.onRenderElement();
  }

  public interface ISeparableElement extends IElement {
    public void setHudScale(float scale);
  }

  @FunctionalInterface
  public interface ISeparableElementFactory {
    public ISeparableElement create(InteractiveContext context, IElement parent, boolean isMainIndicator, Dim defaultHeight);
  }
}
