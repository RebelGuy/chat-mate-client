package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.config.Config.HudElementTransform;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudFilters.IHudFilter;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Events.ScreenSizeData;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.events.models.MouseEventData.In.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.EnumHelpers;
import dev.rebel.chatmate.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static dev.rebel.chatmate.util.TextHelpers.isNullOrEmpty;

public abstract class HudElement extends ElementBase {
  private @Nullable List<IHudFilter> hudElementFilter = null;

  protected @Nonnull DimPoint defaultPosition;
  protected @Nonnull Anchor defaultPositionAnchor;
  protected @Nullable Anchor scrollResizeAnchor;
  protected @Nullable Anchor contentResizeAnchor;
  protected float currentScale = 1;
  private float defaultScale = 1;
  protected Anchor autoAnchor = Anchor.TOP_LEFT;

  private boolean isSelected = false;
  private boolean canDrag = false;
  private boolean canScale = false;
  private boolean isScrolling = false;
  private boolean isInitialised = false;
  private String persistName = "";
  private boolean persistTransform = false;

  public HudElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    this.defaultPosition = new DimPoint(ZERO, ZERO);
    this.defaultPositionAnchor = Anchor.TOP_LEFT;
    this.scrollResizeAnchor = null;
    this.contentResizeAnchor = null;
  }
  
  public HudElement setCanDrag(boolean canDrag) {
    this.canDrag = canDrag;
    return this;
  }
  
  public HudElement setCanScale(boolean canScale) {
    this.canScale = canScale;
    return this;
  }

  /** Call this in your constructor to automatically manage persistence (read and write).
   * If a persisted transform exists, it will take precedence over `setDefaultPosition` and `setDefaultScale`. */
  public HudElement enablePersistTransform(String persistName) {
    this.persistName = persistName;
    this.persistTransform = true;
    return this;
  }

  public boolean canDrag() {
    return this.canDrag;
  }

  public boolean canScale() {
    return this.canScale;
  }

  /** Sets the initial position of the HUD element, where the anchored point of the box will be placed at the provided position. Only effective before the element has first been rendered. */
  public HudElement setDefaultPosition(@Nonnull DimPoint point, @Nonnull Anchor boxAnchor) {
    this.defaultPosition = point;
    this.defaultPositionAnchor = boxAnchor;
    return this;
  }

  public HudElement setDefaultScale(float defaultScale) {
    this.defaultScale = defaultScale;
    return this;
  }

  /** Sets the anchor of the box about which the box should be resized when scrolling. If null, automatically chooses the resize anchor. */
  public HudElement setScrollResizeAnchor(@Nullable Anchor anchor) {
    this.scrollResizeAnchor = anchor;
    return this;
  }

  /** Sets the anchor of the box about which the box should be resized when the content changes. If null, automatically chooses the resize anchor. */
  public HudElement setContentResizeAnchor(@Nullable Anchor anchor) {
    this.contentResizeAnchor = anchor;
    return this;
  }

  /** By default, HUD elements are always shown, but you can provide a list of filters to customise this behaviour. */
  public HudElement setHudElementFilter(@Nullable IHudFilter... filters) {
    this.hudElementFilter = filters == null || filters.length == 0 ? null : Collections.list(filters);
    return this;
  }

  public @Nullable List<IHudFilter> getHudElementFilter() {
    return this.hudElementFilter;
  }

  private void persistTransform() {
    if (!this.persistTransform || isNullOrEmpty(this.persistName)) {
      return;
    }

    HudElementTransform transform = new HudElementTransform(
        super.getBox().getX(),
        super.getBox().getY(),
        this.autoAnchor,
        super.context.dimFactory.getMinecraftRect(),
        super.context.dimFactory.getScaleFactor(),
        this.currentScale
    );
    super.context.config.getHudTransformsEmitter().set(prev -> {
      Map<String, HudElementTransform> current = new HashMap<>(prev);
      current.put(this.persistName, transform);
      return current;
    });
  }

  private @Nullable HudElementTransform getPersistedTransform() {
    if (!this.persistTransform || isNullOrEmpty(this.persistName)) {
      return null;
    }

    return super.context.config.getHudTransformsEmitter().get().get(this.persistName);
  }

  public HudElement setSelected(boolean isSelected) {
    this.isSelected = isSelected;
    return this;
  }

  public void onDrag(DimPoint positionDelta) {
    if (!this.canDrag) {
      return;
    }

    super.setBoxUnsafe(super.getBox().withTranslation(positionDelta));
    super.onInvalidateSize();
  }

  public void onScroll(ScrollDirection direction) {
    if (!this.canScale) {
      return;
    }

    int multiplier = direction == ScrollDirection.UP ? 1 : -1;
    float newScale = Math.min(5, Math.max(0.1f, this.currentScale + multiplier * 0.1f));

    if (this.currentScale != newScale) {
      float oldScale = this.currentScale;
      this.currentScale = newScale;
      this.isScrolling = true;
      this.onElementRescaled(oldScale, newScale);
      this.persistTransform();
    }
    super.onInvalidateSize();
  }

  @Override
  public final void setBox(DimRect box) {
    // the box provided here will be incorrect, we handle our own sizing functionality.
    if (super.getBox() == null) {
      // this branch is only run once to set the initial box
      @Nullable HudElementTransform persistedTransform = this.getPersistedTransform();
      if (persistedTransform != null) {
        // if there is persisted scaling, apply this first and come back after the next recalculation to set the box
        float scale = persistedTransform.scale;
        if (scale != this.currentScale) {
          float oldScale = this.currentScale;
          this.currentScale = scale;
          this.onElementRescaled(oldScale, scale);
          super.onInvalidateSize();
          return;
        }

        // e.g. if we persisted the element to be 2 pixels from the right of the window and 5 pixels from the top, then we should retain that position relative to the current window
        DimRect persistedBox = new DimRect(new DimPoint(persistedTransform.x, persistedTransform.y), super.lastCalculatedSize);
        ScreenSizeData persistedScreenSizeData = new ScreenSizeData(
            persistedTransform.screenRect.getSize(),
            persistedTransform.screenScaleFactor,
            super.context.dimFactory.getMinecraftSize(),
            super.context.dimFactory.getScaleFactor()
        );
        DimPoint position = repositionInResizedWindow(persistedTransform.positionAnchor, persistedBox, persistedScreenSizeData);
        box = box.withPosition(position);
        this.setBoxUnsafe(box);
      } else {
        // no persistence - use defaults
        box = setBoxPositionAtAnchor(new DimRect(this.defaultPosition, super.lastCalculatedSize), this.defaultPosition, this.defaultPositionAnchor);
        this.setBoxUnsafe(box);

        // since elements start off with a scale of 1, we have to notify listeners of the resizing so they can handle it as required
        if (this.defaultScale != this.currentScale) {
          float oldScale = this.currentScale;
          this.currentScale = this.defaultScale;
          this.onElementRescaled(oldScale, this.defaultScale);
        }
      }
    } else {
      // infer why we are resizing the box
      Anchor resizeAnchor;
      if (!this.isInitialised) {
        resizeAnchor = Anchor.TOP_LEFT;
        this.isInitialised = true;
      } else if (this.isScrolling) {
        resizeAnchor = scrollResizeAnchor;
      } else {
        resizeAnchor = this.contentResizeAnchor;
      }

      // if there's no resize, the box will stay the same.
      box = resizeBox(super.getBox(), this.lastCalculatedSize.getX(), this.lastCalculatedSize.getY(), Objects.firstOrNull(resizeAnchor, this.autoAnchor));
    }

    this.isScrolling = false;
    this.autoAnchor = calculateAnchor(super.context.dimFactory.getMinecraftRect(), box);
    super.setBox(box);
    this.onHudBoxSet(box);
    this.persistTransform();
  }

  /** Called when the box has been set for this element (do NOT call `super.setBox`). Provides the positioned box with the size specified by the returned value of `calculateThisSize`. */
  public abstract void onHudBoxSet(DimRect box);

  /** Called when the user has changed the scale of the component, before re-rendering occurs. `oldScale` and `newScale` are guaranteed to be different. */
  protected void onElementRescaled(float oldScale, float newScale) { }

  @Override
  public final void renderElement() {
    if (this.isSelected || super.isHovering() && (this.canScale || this.canDrag)) {
      float alpha = this.isSelected ? 0.2f : 0.1f;
      RendererHelpers.drawRect(0, super.getBox(), Colour.BLACK.withAlpha(alpha));
    }

    this.onRenderElement();
  }

  /** Implement this instead of the standard `renderElement`. */
  public abstract void onRenderElement();

  @Override
  public void onWindowResize(InteractiveEvent<ScreenSizeData> e) {
    DimRect box = super.getBox();
    DimPoint newPosition = repositionInResizedWindow(this.autoAnchor, box, e.getData());
    super.setBox(box.withPosition(newPosition));
  }

  private static DimPoint repositionInResizedWindow(Anchor resizeAnchor, DimRect box, ScreenSizeData screenSizeData) {
    Function<DimPoint, Dim> horizontal = DimPoint::getX;
    Function<DimPoint, Dim> vertical = DimPoint::getY;
    DimPoint position = box.getPosition();
    DimPoint size = box.getSize();

    // we want to keep the element's GUI position relative to the window the same. use the anchor to determine how we should change the box.
    // note that we take into consideration whether the box's position is anchored to the GUI or screen when working out the new position.
    Dim x;
    Dim y;
    switch (resizeAnchor) {
      case TOP_LEFT:
        x = alignLower(horizontal, position, size, screenSizeData);
        y = alignLower(vertical, position, size, screenSizeData);
        break;
      case TOP_CENTRE:
        x = alignMiddle(horizontal, position, size, screenSizeData);
        y = alignLower(vertical, position, size, screenSizeData);
        break;
      case TOP_RIGHT:
        x = alignUpper(horizontal, position, size, screenSizeData);
        y = alignLower(vertical, position, size, screenSizeData);
        break;
      case LEFT_CENTRE:
        x = alignLower(horizontal, position, size, screenSizeData);
        y = alignMiddle(vertical, position, size, screenSizeData);
        break;
      case MIDDLE:
        x = alignMiddle(horizontal, position, size, screenSizeData);
        y = alignMiddle(vertical, position, size, screenSizeData);
        break;
      case RIGHT_CENTRE:
        x = alignUpper(horizontal, position, size, screenSizeData);
        y = alignMiddle(vertical, position, size, screenSizeData);
        break;
      case BOTTOM_LEFT:
        x = alignLower(horizontal, position, size, screenSizeData);
        y = alignUpper(vertical, position, size, screenSizeData);
        break;
      case BOTTOM_CENTRE:
        x = alignMiddle(horizontal, position, size, screenSizeData);
        y = alignUpper(vertical, position, size, screenSizeData);
        break;
      case BOTTOM_RIGHT:
        x = alignUpper(horizontal, position, size, screenSizeData);
        y = alignUpper(vertical, position, size, screenSizeData);
        break;
      default:
        throw EnumHelpers.<Anchor>assertUnreachable(resizeAnchor);
    }

    return new DimPoint(x, y);
  }

  private static Dim alignLower(Function<DimPoint, Dim> dimSelector, DimPoint boxPosition, DimPoint boxSize, ScreenSizeData screenSizeData) {
    Dim x = dimSelector.apply(boxPosition);
    DimAnchor anchor = x.anchor;

    if (anchor == DimAnchor.SCREEN) {
      // keep the screen gap on the left side the same
      return x;
    }

    Dim oldWidth = dimSelector.apply(screenSizeData.oldSize);
    Dim newWidth = dimSelector.apply(screenSizeData.newSize);
    float scaling = (float)screenSizeData.newScaleFactor / screenSizeData.oldScaleFactor; // to determine the GUI position on the old screen
    float relLeft = x.over(scaling).over(oldWidth);
    return newWidth.times(relLeft).setAnchor(anchor);
  }

  private static Dim alignMiddle(Function<DimPoint, Dim> dimSelector, DimPoint boxPosition, DimPoint boxSize, ScreenSizeData screenSizeData) {
    Dim x = dimSelector.apply(boxPosition);
    DimAnchor anchor = x.anchor;
    Dim w = dimSelector.apply(boxSize);
    Dim oldWidth = dimSelector.apply(screenSizeData.oldSize);
    Dim newWidth = dimSelector.apply(screenSizeData.newSize);
    float scaling = (float)screenSizeData.newScaleFactor / screenSizeData.oldScaleFactor; // to determine the GUI position on the old screen
    float relCentre = x.plus(w.over(2)).over(scaling).over(oldWidth);

    // obtained by rearranging the relCentre equation
    return newWidth.times(relCentre).minus(w.over(2)).setAnchor(anchor);
  }

  private static Dim alignUpper(Function<DimPoint, Dim> dimSelector, DimPoint boxPosition, DimPoint boxSize, ScreenSizeData screenSizeData) {
    Dim x = dimSelector.apply(boxPosition);
    Dim w = dimSelector.apply(boxSize);
    DimAnchor anchor = x.anchor;
    Dim oldWidth = dimSelector.apply(screenSizeData.oldSize);
    Dim newWidth = dimSelector.apply(screenSizeData.newSize);

    if (anchor == DimAnchor.SCREEN) {
      // keep the screen gap on the right the same
      Dim absRight = oldWidth.minus(x.plus(w));
      return newWidth.minus(absRight).minus(w).setAnchor(anchor);
    } else {
      float scaling = (float)screenSizeData.newScaleFactor / screenSizeData.oldScaleFactor; // to determine the GUI position on the old screen
      float relRight = x.plus(w).over(scaling).over(oldWidth);
      return newWidth.times(relRight).minus(w).setAnchor(anchor);
    }
  }

  /** Gets the anchor such that, if the component were to be resized about the anchor, the resize will be performed towards the centre of the screen. */
  public static Anchor calculateAnchor(DimRect screen, DimRect componentBox) {
    DimRect left = screen.partial(0, 0.25f, 0, 1);
    DimRect centre = screen.partial(0.25f, 0.75f, 0, 1);
    DimRect right = screen.partial(0.75f, 1, 0, 1);
    DimRect top = screen.partial(0, 1, 0, 0.25f);
    DimRect middle = screen.partial(0, 1, 0.25f, 0.75f);
    DimRect bottom = screen.partial(0, 1, 0.75f, 1);

    Dim x = componentBox.getX();
    Dim y = componentBox.getY();
    Dim r = componentBox.getRight();
    Dim b = componentBox.getBottom();

    int xAlignment;
    if (left.checkCollisionX(x) && right.checkCollisionX(r) || centre.checkCollisionX(x) && centre.checkCollisionX(r)) {
      xAlignment = 0;
    } else if (left.checkCollisionX(x)) {
      xAlignment = -1;
    } else {
      xAlignment = 1;
    }

    int yAlignment;
    if (top.checkCollisionY(y) && bottom.checkCollisionY(b) || middle.checkCollisionY(y) && middle.checkCollisionY(b)) {
      yAlignment = 0;
    } else if (top.checkCollisionY(y)) {
      yAlignment = -1;
    } else {
      yAlignment = 1;
    }

    Anchor anchor;
    if (xAlignment == -1 && yAlignment == -1) {
      anchor = Anchor.TOP_LEFT;
    } else if (xAlignment == 0 && yAlignment == -1) {
      anchor = Anchor.TOP_CENTRE;
    } else if (xAlignment == 1 && yAlignment == -1) {
      anchor = Anchor.TOP_RIGHT;
    } else if (xAlignment == -1 && yAlignment == 0) {
      anchor = Anchor.LEFT_CENTRE;
    } else if (xAlignment == 0 && yAlignment == 0) {
      anchor = Anchor.MIDDLE;
    } else if (xAlignment == 1 && yAlignment == 0) {
      anchor = Anchor.RIGHT_CENTRE;
    } else if (xAlignment == -1 && yAlignment == 1) {
      anchor = Anchor.BOTTOM_LEFT;
    } else if (xAlignment == 0 && yAlignment == 1) {
      anchor = Anchor.BOTTOM_CENTRE;
    } else {
      anchor = Anchor.BOTTOM_RIGHT;
    }
    return anchor;
  }

  /** Resizes the given rect to the given dimensions about its anchor. */
  protected static DimRect resizeBox(DimRect rect, Dim newW, Dim newH, Anchor resizeAnchor) {
    if (rect.getWidth().equals(newW) && rect.getHeight().equals(newH)) {
      return rect;
    }

    Dim dw = newW.minus(rect.getWidth());
    Dim dh = newH.minus(rect.getHeight());
    Dim x = rect.getX();
    Dim y = rect.getY();
    Dim ZERO = x.setGui(0);

    switch (resizeAnchor) {
      case TOP_LEFT:
        x = x.plus(ZERO);
        y = y.plus(ZERO);
        break;
      case LEFT_CENTRE:
        x = x.plus(ZERO);
        y = y.minus(dh.over(2));
        break;
      case BOTTOM_LEFT:
        x = x.plus(ZERO);
        y = y.minus(dh);
        break;

      case TOP_CENTRE:
        x = x.minus(dw.over(2));
        y = y.plus(ZERO);
        break;
      case MIDDLE:
        x = x.minus(dw.over(2));
        y = y.minus(dh.over(2));
        break;
      case BOTTOM_CENTRE:
        x = x.minus(dw.over(2));
        y = y.minus(dh);
        break;

      case TOP_RIGHT:
        x = x.minus(dw);
        y = y.plus(ZERO);
        break;
      case RIGHT_CENTRE:
        x = x.minus(dw);
        y = y.minus(dh.over(2));
        break;
      case BOTTOM_RIGHT:
        x = x.minus(dw);
        y = y.minus(dh);
        break;

      default:
        throw EnumHelpers.<Anchor>assertUnreachable(resizeAnchor);
    }

    return new DimRect(x, y, newW, newH);
  }

  protected static DimRect setBoxPositionAtAnchor(DimRect box, DimPoint point, Anchor anchor) {
    // calculate the anchor's position relative to the box's position
    Dim x = box.getX();
    Dim y = box.getY();
    Dim w = box.getWidth();
    Dim h = box.getHeight();
    Dim ZERO = x.setGui(0);

    Dim dx;
    Dim dy;
    switch (anchor) {
      case TOP_LEFT:
        dx = ZERO;
        dy = ZERO;
        break;
      case LEFT_CENTRE:
        dx = ZERO;
        dy = h.over(2);
        break;
      case BOTTOM_LEFT:
        dx = ZERO;
        dy = h;
        break;

      case TOP_CENTRE:
       dx = w.over(2);
       dy = ZERO;
        break;
      case MIDDLE:
        dx = w.over(2);
        dy = h.over(2);
        break;
      case BOTTOM_CENTRE:
        dx = w.over(2);
        dy = h;
        break;

      case TOP_RIGHT:
        dx = w;
        dy = ZERO;
        break;
      case RIGHT_CENTRE:
        dx = w;
        dy = h.over(2);
        break;
      case BOTTOM_RIGHT:
        dx = w;
        dy = w;
        break;

      default:
        throw EnumHelpers.<Anchor>assertUnreachable(anchor);
    }

    DimPoint newPosition = point.minus(new DimPoint(dx, dy));
    return box.withPosition(newPosition);
  }

  /** Denotes the point of the box. */
  public enum Anchor {
    TOP_LEFT,
    TOP_CENTRE,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTRE,
    BOTTOM_RIGHT,
    LEFT_CENTRE,
    RIGHT_CENTRE,
    MIDDLE
  }
}
