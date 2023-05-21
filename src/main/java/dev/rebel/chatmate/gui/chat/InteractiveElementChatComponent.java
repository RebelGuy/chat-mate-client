package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.LifecycleType;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;

/** Misleading name: this won't allow any interactions! */
public abstract class InteractiveElementChatComponent extends ChatComponentBase {
  private final InteractiveContext context;

  private boolean initialised;
  protected final InteractiveScreen screen;

  public InteractiveElementChatComponent(InteractiveContext context) {
    this.context = context;
    this.initialised = false;
    this.screen = new InteractiveScreen(context, null, InteractiveScreenType.CHAT_COMPONENT, LifecycleType.PRIVATE)
        .preventCloseByEscapeKey(true);
  }

  /** It is safe to add/remove/modify elements in this method. */
  abstract void onPreRender(Dim x, Dim y, int opacity);

  public void dispose() {
    this.screen.onCloseScreen();
  }

  public Dim getWidth(Dim lineWidth, Dim lineHeight) {
    if (!this.initialised) {
      this.initialised = true;
      this.screen.initGui();
    }

    IElement mainElement = this.screen.getMainElement();
    if (mainElement != null && mainElement.getVisible()) {
      mainElement.setTargetContentHeight(lineHeight);
      mainElement.setMaxContentWidth(lineWidth);
      DimPoint size = mainElement.calculateSize(lineWidth);

      // dirty hack: subtract the horizontal margin as it is assumed that the margin is used to position the main element
      return size.getX().minus(this.screen.getMainElement().getMargin().getExtendedWidth());
    } else {
      return this.context.dimFactory.zeroGui();
    }
  }

  public Dim getLastWidth() {
    if (this.screen.getMainElement() == null) {
      return this.context.dimFactory.zeroGui();
    }

    DimPoint lastSize = this.screen.getMainElement().getLastCalculatedSize();
    return lastSize != null ? lastSize.getX().minus(this.screen.getMainElement().getMargin().getExtendedWidth()) : this.context.dimFactory.zeroGui();
  }

  /** Returns the width taken up by the component. */
  public Dim render(Dim x, Dim y, int opacity, DimRect chatRect) {
    if (!this.initialised) {
      this.initialised = true;
      this.screen.initGui();
    } else if (this.screen.getMainElement() == null) {
      return this.context.dimFactory.zeroGui();
    }

    this.onPreRender(x, y, opacity);
    if (this.screen.getMainElement() == null) {
      return this.context.dimFactory.zeroGui();
    }

    this.screen.drawScreen(0, 0, 0);
    if (this.screen.getMainElement() == null) {
      return this.context.dimFactory.zeroGui();
    }

    return this.screen.getMainElement().getLastCalculatedSize().getX();
  }
}
