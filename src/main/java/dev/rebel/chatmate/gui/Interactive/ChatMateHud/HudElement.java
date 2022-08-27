package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.ElementBase;
import dev.rebel.chatmate.gui.Interactive.Events;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseScrollData.ScrollDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class HudElement extends ElementBase {
  protected @Nonnull DimPoint defaultPosition;
  protected float currentScale = 1;
  protected Anchor anchor = Anchor.TOP_LEFT;
  
  private boolean canDrag = false;
  private boolean canScale = false;
  
  private @Nullable DimPoint lastDraggingPosition = null;

  public HudElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);
    
    this.defaultPosition = new DimPoint(ZERO, ZERO);
  }
  
  public HudElement setCanDrag(boolean canDrag) {
    this.canDrag = canDrag;
    return this;
  }
  
  public HudElement setCanScale(boolean canScale) {
    this.canScale = canScale;
    return this;
  }

  @Override
  public void onMouseDown(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON && this.canDrag) {
      this.lastDraggingPosition = e.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);
    }
  }

  @Override
  public void onMouseMove(Events.IEvent<MouseEventData.In> e) {
    if (this.canDrag && this.lastDraggingPosition != null && e.getData().isDragged(MouseButton.LEFT_BUTTON)) {
      DimPoint positionDelta = e.getData().mousePositionData.point.minus(this.lastDraggingPosition).setAnchor(DimAnchor.GUI);
      super.setBoxUnsafe(super.getBox().withTranslation(positionDelta));
      super.onInvalidateSize();
    }
  }

  @Override
  public void onMouseUp(Events.IEvent<MouseEventData.In> e) {
    if (this.lastDraggingPosition != null) {
      this.lastDraggingPosition = null;
      e.stopPropagation();
      super.onInvalidateSize();
    }
  }

  @Override
  public final void setBox(DimRect box) {
    // the box provided here will be incorrect, we handle our own sizing functionality.
    if (super.getBox() == null) {
      box = new DimRect(this.defaultPosition, this.lastCalculatedSize);
    } else {
      // if there's no resize, the box will stay the same
      box = resizeBox(super.getBox(), this.lastCalculatedSize.getX(), this.lastCalculatedSize.getY(), this.anchor);
    }

    super.setBox(box);
    this.onHudBoxSet(box);
  }

  /** Called when the box has been set for this element. Provides the positioned box with the size specified by the returned value of `calculateThisSize`. */
  public abstract void onHudBoxSet(DimRect box);

  /** Called when the user has changed the scale of the component, before re-rendering occurs. `oldScale` and `newScale` are guaranteed to be different. */
  protected void onRescaleContent(DimRect oldBox, float oldScale, float newScale) { }

  @Override
  public void onMouseScroll(Events.IEvent<MouseEventData.In> e) {
    if (this.canScale) {
      int multiplier = e.getData().mouseScrollData.scrollDirection == ScrollDirection.UP ? 1 : -1;
      float newScale = Math.min(5, Math.max(0.1f, this.currentScale + multiplier * 0.1f));
      
      if (this.currentScale != newScale) {
        float oldScale = this.currentScale;
        this.currentScale = newScale;
        this.onRescaleContent(super.getBox(), oldScale, newScale);
      }
      super.onInvalidateSize();
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
        throw new RuntimeException("Invalid anchor: " + resizeAnchor);
    }

    return new DimRect(x, y, newW, newH);
  }
}