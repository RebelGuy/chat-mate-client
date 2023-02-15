package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.events.KeyboardEventService;
import dev.rebel.chatmate.events.KeyboardEventService.KeyboardEventType;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.KeyboardEventOptions;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.LifecycleType;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import org.lwjgl.input.Keyboard;

/** Misleading name: this won't allow any interactions! */
public abstract class InteractiveElementChatComponent extends ChatComponentBase {
  private final InteractiveContext context;
  private final EventCallback<KeyboardEventData> _onKeyDown = this::onKeyDown;

  private boolean initialised;
  protected final InteractiveScreen screen;

  public InteractiveElementChatComponent(InteractiveContext context) {
    this.context = context;
    this.initialised = false;
    this.screen = new InteractiveScreen(context, null, InteractiveScreenType.CHAT_COMPONENT, LifecycleType.PRIVATE);
    context.keyboardEventService.on(KeyboardEventType.KEY_DOWN, 1, this._onKeyDown, new KeyboardEventOptions(null, null, null), this);
  }

  private void onKeyDown(Event<KeyboardEventData> event) {
    // don't close the chat component's screen if we hit escape!
    if (event.getData().eventKey == Keyboard.KEY_ESCAPE) {
      event.stopPropagation();
    }
  }

  /** It is safe to add/remove/modify elements in this method. */
  abstract void onPreRender(Dim x, Dim y);

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
      mainElement.setTargetHeight(lineHeight);
      mainElement.setMaxWidth(lineWidth);
      DimPoint size = mainElement.calculateSize(lineWidth);
      return size.getX();
    } else {
      return this.context.dimFactory.zeroGui();
    }
  }

  public Dim getLastWidth() {
    if (this.screen.getMainElement() == null) {
      return this.context.dimFactory.zeroGui();
    }

    DimPoint lastSize = this.screen.getMainElement().getLastCalculatedSize();
    return lastSize != null ? lastSize.getX() : this.context.dimFactory.zeroGui();
  }

  /** Returns the width taken up by the component. */
  public Dim render(Dim x, Dim y, DimRect chatRect) {
    if (!this.initialised) {
      this.initialised = true;
      this.screen.initGui();
    } else if (this.screen.getMainElement() == null) {
      return this.context.dimFactory.zeroGui();
    }

    this.onPreRender(x, y);
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
