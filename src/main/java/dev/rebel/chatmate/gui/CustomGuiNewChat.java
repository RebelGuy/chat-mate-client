package dev.rebel.chatmate.gui;

import com.google.common.collect.Lists;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.models.*;
import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.events.models.MouseEventData.MousePositionData;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers.Transform;
import dev.rebel.chatmate.gui.StateManagement.*;
import dev.rebel.chatmate.gui.chat.*;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.*;
import dev.rebel.chatmate.gui.models.ChatLine;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.ForgeEventService;
import dev.rebel.chatmate.events.MinecraftChatEventService;
import dev.rebel.chatmate.events.MouseEventService;
import dev.rebel.chatmate.events.MouseEventService.MouseEventType;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static dev.rebel.chatmate.gui.chat.ComponentHelpers.getFormattedText;

// Note: for maintainability reasons, please do not remove any of the @Override methods, even if they replicate the super method.
/** Responsible for drawing lines of chat and exposing the location of components. See also: `CustomGuiChat`, which is
 * the screen shown when opening the chat. */
public class CustomGuiNewChat extends GuiNewChat {
  private static final Logger logger = LogManager.getLogger();
  private static final Integer MAX_DRAWN_LINES = 500; // limits drawnChatLines
  private static final Integer MAX_LINES = 500; // limits chatLines
  private static final Integer LEFT = 2; // distance of the chat window to the left side of the screen, in GUI units
  private static final Integer BOTTOM = 20;
  private static final Integer LEFT_PADDING = 1; // padding between the start of the chat line, and the start of the text, in GUI units
  private static final float SCROLL_MULT = 0.5f;

  private final Minecraft minecraft;
  private final LogService logService;
  private final Config config;
  private final ForgeEventService forgeEventService;
  private final DimFactory dimFactory;
  private final MouseEventService mouseEventService;
  private final ContextMenuStore contextMenuStore;
  private final FontEngine fontEngine;
  private final ChatComponentRenderer chatComponentRenderer;
  private final MinecraftChatEventService minecraftChatEventService;

  private final List<String> sentMessages = Lists.newArrayList();
  private final List<AbstractChatLine> abstractChatLines = Lists.newArrayList();
  private final List<ChatLine> chatLines = Lists.newArrayList(); // rendered chat lines, where components may be broken up into multiple lines to fit into the GUI.
  private final AnimatedSelection<AbstractChatLine> selectedLine;
  private final AnimatedSelection<AbstractChatLine> hoveredLine;

  private final AnimatedFloat scrollPos; // number of scrolled lines. 0 means we have scrolled to the bottom (most recent chat).
  private boolean isScrolled = false;
  private ChatDimensions currentDimensions;
  private @Nullable DimRect scrollBarRect = null;
  private @Nullable DimPoint scrollBarDragPosition = null;
  private @Nullable Float scrollBarPositionAtDrag = null;
  private final AnimatedBool hoveringOverScrollbar = new AnimatedBool(300L, false);

  public CustomGuiNewChat(Minecraft minecraft,
                          LogService logService,
                          Config config,
                          ForgeEventService forgeEventService,
                          DimFactory dimFactory,
                          MouseEventService mouseEventService,
                          ContextMenuStore contextMenuStore,
                          FontEngine fontEngine,
                          ChatComponentRenderer chatComponentRenderer,
                          MinecraftChatEventService minecraftChatEventService) {
    super(minecraft);
    this.minecraft = minecraft;
    this.logService = logService;
    this.config = config;
    this.forgeEventService = forgeEventService;
    this.dimFactory = dimFactory;
    this.mouseEventService = mouseEventService;
    this.contextMenuStore = contextMenuStore;
    this.fontEngine = fontEngine;
    this.chatComponentRenderer = chatComponentRenderer;
    this.minecraftChatEventService = minecraftChatEventService;

    this.hoveredLine = new AnimatedSelection<>(150L);
    this.selectedLine = new AnimatedSelection<>(100L);
    this.currentDimensions = new ChatDimensions(minecraft.gameSettings);
    this.scrollPos = new AnimatedFloat(400L, 0f);
    this.scrollPos.setEasing(frac -> 1 - (float)Math.pow(1 - frac, 5));

    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
    this.forgeEventService.onGuiScreenChanged(this::onChatLoseFocus, new GuiScreenChangedEventOptions(GuiScreenChangedEventData.ListenType.CLOSE_ONLY, CustomGuiChat.class));
    this.mouseEventService.on(MouseEventType.MOUSE_DOWN, this::onMouseDown, new MouseEventOptions(), null);
    this.mouseEventService.on(MouseEventType.MOUSE_UP, this::onMouseUp, new MouseEventOptions(), null);
    this.mouseEventService.on(MouseEventType.MOUSE_MOVE, this::onMouseMove, new MouseEventOptions(), null);
  }

  private void onChatLoseFocus(Event<GuiScreenChangedEventData> event) {
    this.hoveredLine.setSelected(null);
    this.scrollBarDragPosition = null;
    this.scrollBarPositionAtDrag = null;
    this.hoveringOverScrollbar.set(false);
  }

  private void onMouseDown(Event<MouseEventData> event) {
    MouseEventData data = event.getData();
    DimPoint position = data.mousePositionData.point.setAnchor(DimAnchor.GUI);
    if (data.mouseButtonData.eventButton == MouseButton.LEFT_BUTTON && this.scrollBarRect != null && this.scrollBarRect.checkCollision(position)) {
      this.scrollBarDragPosition = position;
      this.scrollBarPositionAtDrag = this.scrollPos.get();
    }
  }

  private void onMouseUp(Event<MouseEventData> event) {
    if (event.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      this.scrollBarDragPosition = null;
      this.scrollBarPositionAtDrag = null;
    }
  }

  private void onMouseMove(Event<MouseEventData> event) {
    DimPoint position = event.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);
    this.hoveringOverScrollbar.set(this.scrollBarRect != null && this.scrollBarRect.checkCollision(position));

    if (this.scrollBarDragPosition != null) {
      int storedLines = this.chatLines.size();
      int visibleLines = Math.min(storedLines, this.getLineCount());

      float lineHeight = this.fontEngine.FONT_HEIGHT * this.getChatScale();
      float renderedHeight = visibleLines * lineHeight;
      float barHeight = this.scrollBarRect.getHeight().getGui();

      float linesScrolledPerUnitMoved = (storedLines - visibleLines) / (renderedHeight - barHeight); // non-visible lines over the free sidebar space

      // mouseDelta determines how far the bar has moved
      float mouseDelta = position.getY().minus(this.scrollBarDragPosition.getY()).getGui();

      float newScrollTarget = this.scrollBarPositionAtDrag - mouseDelta * linesScrolledPerUnitMoved; // scrolling is inverted: a positive-y delta decreases the scrolling position
      float currentScrollTarget = this.scrollPos.getTarget();
      this.scroll(newScrollTarget - currentScrollTarget, true);
    }

    this.updateHoveredLine();
  }

  private void updateHoveredLine() {
    if (this.contextMenuStore.isShowingContextMenu() || !this.getChatOpen()) {
      this.hoveredLine.setSelected(null);
    } else {
      MousePositionData position = this.mouseEventService.getCurrentPosition();
      this.hoveredLine.setSelected(this.getAbstractChatLine(position.x, position.y));
    }
  }

  private void onRenderChatGameOverlay(Event<RenderChatGameOverlayEventData> event) {
    event.stopPropagation();

    float posX = event.getData().posX + LEFT;
    float posY = event.getData().posY - this.config.getChatVerticalDisplacementEmitter().get() + BOTTOM;
    DimPoint translation = new DimPoint(this.dimFactory.fromGui(posX), this.dimFactory.fromGui(posY)); // bottom left position

    // clip the chat vertically
    Dim height = this.dimFactory.fromGui(this.getChatHeight()).times(this.getChatScale()); // scale has to be applied here
    DimRect chatRect = new DimRect(
        this.dimFactory.zeroGui(),
        translation.getY().minus(height),
        this.dimFactory.getMinecraftSize().getX(),
        height
    );

    RendererHelpers.withTranslation(translation, transform -> {
      RendererHelpers.withScissor(chatRect, this.dimFactory.getMinecraftSize(), () -> {
        this.drawChat(this.minecraft.ingameGUI.getUpdateCounter(), chatRect.withTranslation(translation.scale(-1)), transform);
      });
    });

    this.minecraft.mcProfiler.endSection();
  }

  @Override
  public void drawChat(int updateCounter) {
    this.drawChat(updateCounter, null, null); // I don't think this gets called anywhere
  }

  public void drawChat(int updateCounter, DimRect chatRect, Transform transform) {
    if (this.minecraft.gameSettings.chatVisibility == EntityPlayer.EnumChatVisibility.HIDDEN) {
      return;
    }

    int lineCount = this.chatLines.size();
    if (lineCount == 0) {
      return;
    }

    int maxLines = this.getLineCount();
    float opacity = this.minecraft.gameSettings.chatOpacity * 0.9F + 0.1F;

    float scale = this.getChatScale();

    // the fractional amount of scrollPos represents the proportion of the top line that is visible (or one minus the proportion of the bottom line).
    // we want to effectively scroll the chat down (and it will be clipped by the scissor in the parent method)
    float scrollPos = this.scrollPos.get();
    float verticalOffset = (scrollPos - (float)Math.floor(scrollPos)) * this.fontEngine.FONT_HEIGHT * scale;
    DimPoint verticalOffsetDim = new DimPoint(this.dimFactory.zeroGui(), this.dimFactory.fromGui(verticalOffset));

    RendererHelpers.withMapping(verticalOffsetDim, scale, () -> {
      LineIterator lineIterator = lineAction -> {
        int startLine = (int)Math.floor(scrollPos);
        int endLine = (int)Math.ceil(scrollPos + maxLines);
        int nLines = endLine - startLine; // this is either maxLines (for integer scrollPos) or maxLines + 1 (for fractional scrollPos, where we need to display an additional fractional line)

        for (int i = 0; i + startLine < this.chatLines.size() && i < nLines; i++) {
          ChatLine line = this.chatLines.get(i + startLine);
          if (line == null) {
            continue;
          }

          int lineOpacity = this.getLineOpacity(updateCounter, line, opacity);
          if (lineOpacity < 4) {
            // todo: standardise magic transparency number, we've seen this before when drawing the view count reels
            continue;
          }

          lineAction.act(line, i, lineOpacity);
        }
      };

      // render visible lines. draw the background first so that any chat line contents with negative y offsets are
      // not clipped by the background of the line above it
      lineIterator.forEachLine((line, i, lineOpacity) -> this.drawLineBackground(line, i, lineOpacity));
      lineIterator.forEachLine((line, i, lineOpacity) -> this.drawLine(line, i, lineOpacity, chatRect.withTranslation(verticalOffsetDim.scale(-1))));
    });

    if (this.getChatOpen()) {
      this.drawScrollBar(transform);
    } else {
      this.scrollBarRect = null;
    }
  }

  private void drawLineBackground(ChatLine line, int index, int opacity) {
    int lineBottom = -index * this.fontEngine.FONT_HEIGHT; // negative because we iterate from the bottom line to the top line
    int lineTop = lineBottom - this.fontEngine.FONT_HEIGHT;
    Dim lineWidth = this.getChatWidthForTextDim();

    Dim x = this.dimFactory.fromGui(0);
    Dim y = this.dimFactory.fromGui(lineTop);
    Dim w = lineWidth.plus(this.dimFactory.fromGui(4));
    Dim h = this.fontEngine.FONT_HEIGHT_DIM;
    RendererHelpers.drawRect(-100, new DimRect(x, y, w, h), this.getBackgroundColour(line, opacity));
  }

  private void drawLine(ChatLine line, int index, int opacity, DimRect chatRect) {
    Dim lineLeft = this.dimFactory.fromGui(LEFT_PADDING);
    Dim lineBottom = this.fontEngine.FONT_HEIGHT_DIM.times(-index); // negative because we iterate from the bottom line to the top line
    Dim lineTop = lineBottom.minus(this.fontEngine.FONT_HEIGHT_DIM);

    GlStateManager.enableBlend();

    // unpack the container
    IChatComponent chatComponent = line.getChatComponent();
    if (chatComponent instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)chatComponent;
      chatComponent = container.getComponent();
    }

    if (chatComponent instanceof PrecisionChatComponent) {
      PrecisionChatComponent component = (PrecisionChatComponent)chatComponent;
      Dim width = this.getChatWidthForTextDim();
      for (Tuple2<PrecisionChatComponent.PrecisionLayout, IChatComponent> pair : component.getComponentsForLine(this.fontEngine, width)) {
        Dim left = lineLeft.plus(pair._1.position);
        this.chatComponentRenderer.drawChatComponent(pair._2, left, lineTop, opacity, chatRect);
      }
    } else {
      Dim x = this.dimFactory.zeroGui();
      for (IChatComponent component : chatComponent) {
        x = x.plus(this.chatComponentRenderer.drawChatComponent(component, lineLeft.plus(x), lineTop, opacity, chatRect));
      }
    }
  }

  private Colour getBackgroundColour(ChatLine line, int chatOpacity) {
    Colour standardBackground = Colour.BLACK.withAlpha(chatOpacity / 2);
    Colour selectedColour = new Colour(64, 64, 64, chatOpacity / 2); // hovered colour is just half-opacity of this

    AbstractChatLine abstractLine = line.getParent();
    float hoveredFrac = this.hoveredLine.getFrac(abstractLine);
    float selectedFrac = this.selectedLine.getFrac(abstractLine);

    // this doesn't work 100% when deselecting a message while hovering over that message (it doesn't smoothly transition
    // from selected to hovered, but overshoots to a darker colour first), but I can't figure it out and it's very subtle
    // anyway. I guess a solution is to modify the AnimatedSelection to work with floats rather than booleans, and a value
    // of 0.5 to represent hover, and 1 to represent selection.
    return Colour.lerp(standardBackground, selectedColour, Math.min(1, hoveredFrac / 2 + selectedFrac));
  }

  /** Given the lineCount (total lines) and the renderedLines (how many lines are visible on screen), draws the scrollbar to the left of the chat GUI. */
  private void drawScrollBar(Transform transform) {
    int storedLines = this.chatLines.size();
    int visibleLines = Math.min(storedLines, this.getLineCount());
    if (visibleLines >= storedLines) {
      this.scrollBarRect = null;
      return;
    }

    float lineHeight = this.fontEngine.FONT_HEIGHT * this.getChatScale();
    float fullHeight = storedLines * lineHeight;
    float renderedHeight = visibleLines * lineHeight;
    float barHeight = Math.max(renderedHeight / fullHeight * renderedHeight, lineHeight);
    float scrollRatio = this.scrollPos.get() / (storedLines - visibleLines); // how far up the scroll bar is
    float barOffsetBottom = (renderedHeight - barHeight) * scrollRatio;

    // recall that (0, 0) is at the bottom-left corner of the chat window
    DimRect barRect = new DimRect(
        this.dimFactory.fromGui(-LEFT),
        this.dimFactory.fromGui(-barOffsetBottom - barHeight),
        this.dimFactory.fromGui(LEFT),
        this.dimFactory.fromGui(barHeight)
    );
    Dim cornerRadius = this.dimFactory.fromScreen(4);
    Colour colour;
    if (this.scrollBarDragPosition != null) {
      colour = Colour.WHITE.withAlpha(0.7f);
    } else {
      colour = Colour.lerp(Colour.GREY75, Colour.WHITE, this.hoveringOverScrollbar.getFrac()).withAlpha(0.5f);
    }
    RendererHelpers.drawRect(100, barRect, colour, null, null, cornerRadius);

    this.scrollBarRect = transform.unTransform(barRect);
  }

  private int getLineOpacity(int updateCounter, ChatLine line, float baseOpacity) {
    final int MAX_LINE_AGE = 200;
    int lineAge = updateCounter - line.getUpdatedCounter();
    int lineOpacity = 0;

    if (this.getChatOpen()) {
      lineOpacity = 255;
    } else if (lineAge < MAX_LINE_AGE) {
      // for the last 10% of the line's lifetime, fade it out
      double relAge = (double) lineAge / 200.0D;
      relAge = 1.0D - relAge;
      relAge = relAge * 10.0D;
      relAge = MathHelper.clamp_double(relAge, 0.0D, 1.0D);
      relAge = relAge * relAge;
      lineOpacity = (int) (255.0D * relAge);
    }

    lineOpacity = (int)((float)lineOpacity * baseOpacity);
    return lineOpacity;
  }

  /** Clears the chat. */
  @Override
  public void clearChatMessages() {
    this.chatLines.clear();
    this.abstractChatLines.clear();
    this.sentMessages.clear();
  }

  @Override
  public void printChatMessage(IChatComponent chatComponent) {
    this.addComponent(chatComponent, 0, this.minecraft.ingameGUI.getUpdateCounter());
  }

  /** Prints the ChatComponent to Chat. If the ID is not 0, deletes an existing Chat Line of that ID from the GUI.
   * For example, this API is used by the command suggestion - there is only ever one set of suggested commands! */
  @Override
  public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {
    // note: the ID here is used by callers to withdraw an earlier message and print an updated message.
    if (chatLineId != 0) {
      this.deleteChatLine(chatLineId);
    }

    this.addComponent(chatComponent, chatLineId, this.minecraft.ingameGUI.getUpdateCounter());
  }

  /** Adds a new chat message. Automatically splits up the component's text based on its text width. */
  private void addComponent(IChatComponent chatComponent, int chatLineId, int updateCounter) {
    AbstractChatLine newLine = this.pushFullComponent(chatComponent, chatLineId, updateCounter);
    this.pushDrawnComponent(newLine, chatComponent, chatLineId, updateCounter, true);

    // todo: replace with our own logger
    logger.info("[CHAT] " + chatComponent.getUnformattedText());
  }

  private AbstractChatLine pushFullComponent(IChatComponent chatComponent, int chatLineId, int updateCounter) {
    AbstractChatLine newLine = new AbstractChatLine(updateCounter, chatComponent, chatLineId);
    this.abstractChatLines.add(0, newLine);

    // purge entries at the top
    while (this.abstractChatLines.size() > MAX_LINES) {
      AbstractChatLine lastLine = this.abstractChatLines.get(this.abstractChatLines.size() - 1);
      this.disposeComponentsInLine(lastLine);
      this.abstractChatLines.remove(this.abstractChatLines.size() - 1);
    }

    return newLine;
  }

  /** Adds the component to `drawnChatLines` after processing its contents. */
  private void pushDrawnComponent(AbstractChatLine parent, IChatComponent chatComponent, int chatLineId, int updateCounter, boolean autoScroll) {
    boolean processContents = true;
    if (chatComponent instanceof PrecisionChatComponent) {
      processContents = false;
    } else if (chatComponent instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)chatComponent;
      processContents = !(container.getComponent() instanceof PrecisionChatComponent);
    }

    if (!processContents) {
      // push as-is
      this.pushDrawnChatLine(new ChatLine(updateCounter, chatComponent, chatLineId, parent), autoScroll);

    } else {
      int lineWidth = this.getLineWidth();
      List<IChatComponent> splitComponents = ComponentHelpers.splitText(chatComponent, lineWidth, this.fontEngine); // useful
      for (IChatComponent component : splitComponents) {
        this.pushDrawnChatLine(new ChatLine(updateCounter, component, chatLineId, parent), autoScroll);
      }
    }

    this.updateHoveredLine();
  }

  /** Adds the ChatLine to the `drawnChatLines`. If `autoScroll` is true, will attempt to retain the current chat window view by adjusting the scroll target. */
  private void pushDrawnChatLine(ChatLine line, boolean autoScroll) {
    this.chatLines.add(0, line);

    // make sure we keep the same lines visible even as we push more lines to the bottom of the chat
    if (this.getChatOpen() && autoScroll && this.scrollPos.getTarget() > 0) {
      this.isScrolled = true;
      this.scroll(1, true);
    }

    // purge entries at the top
    while (this.chatLines.size() > MAX_DRAWN_LINES) {
      this.chatLines.remove(this.chatLines.size() - 1);
    }
  }

  /** Re-draws all `this.chatLines`, generating a new list of `drawnChatLines`. Probably used when the chat width has been changed. */
  @Override
  public void refreshChat() {
    this.refreshChat(false);
  }

  public void refreshChat(boolean keepScrollPos) {
    // `refreshChat` is called by `GameSettings` when the dimensions have been changed
    ChatDimensions newDimensions = new ChatDimensions(this.minecraft.gameSettings);
    if (!Objects.equals(this.currentDimensions, newDimensions)) {
      this.currentDimensions = newDimensions;
      this.minecraftChatEventService.dispatchUpdateChatDimensionsEvent();
    }

    this.chatLines.clear();
    if (!keepScrollPos) {
      this.resetScroll();
    }

    for (int i = this.abstractChatLines.size() - 1; i >= 0; --i) {
      AbstractChatLine chatline = this.abstractChatLines.get(i);
      this.pushDrawnComponent(chatline, chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), false);
    }
  }

  @Override
  public List<String> getSentMessages() {
    return this.sentMessages;
  }

  /** Adds this string to the list of sent messages, for recall using the up/down arrow keys. */
  @Override
  public void addToSentMessages(String message) {
    if (this.sentMessages.isEmpty() || !(this.sentMessages.get(this.sentMessages.size() - 1)).equals(message)) {
      this.sentMessages.add(message);
    }
  }

  /** Resets the chat scroll (executed when the GUI is closed, among others). */
  @Override
  public void resetScroll() {
    this.scrollPos.set(0f);
    this.isScrolled = false;
  }

  /** Scrolls the chat by the given number of lines. */
  @Override
  public void scroll(int amount) {
    this.scroll(amount * SCROLL_MULT, false);
  }

  /** Scrolls the chat by the given number of lines. */
  public void scroll(float amount, boolean skipAnimation) {
    // prevent scrolling while dragging the scroll bar
    if (this.scrollBarDragPosition != null && !skipAnimation) {
      return;
    }

    float currentScrollTarget = this.scrollPos.getTarget();
    float newScrollTarget = currentScrollTarget + amount;

    int i = this.chatLines.size();

    // clamp maximum
    if (newScrollTarget > i - this.getLineCount()) {
      newScrollTarget = i - this.getLineCount();
    }

    // clamp minimum and stop scrolling
    if (newScrollTarget <= 0) {
      newScrollTarget = 0;
      this.isScrolled = false;
    }

    if (skipAnimation) {
      amount = newScrollTarget - currentScrollTarget; // in case it was clipped above
      this.scrollPos.translate(amount);
    } else {
      this.scrollPos.set(newScrollTarget);
    }
  }

  public IChatComponent getChatComponent(Dim x, Dim y) {
    int mouseX = (int)x.getScreen();
    int mouseY = this.minecraft.displayHeight - (int)y.getScreen() - 1;
    return this.getChatComponent(mouseX, mouseY);
  }

  // yep.. it uses the inverted screen coordinates
  /** Gets the chat component at the screen position. */
  @Override
  public IChatComponent getChatComponent(int mouseX, int mouseY) {
    Tuple2<Integer, Integer> coords = this.mapInvertedScreenPositionIntoChat(mouseX, mouseY);
    Dim x = this.dimFactory.fromGui(coords._1); // mouseX and mouseY are in gui coords...
    Dim y = this.dimFactory.fromGui(coords._2);
    Dim maxX = this.getLineWidthDim();

    ChatLine chatLine = this.getRenderedChatLine((int)x.getGui(), (int)y.getGui());
    if (chatLine == null) {
      return null;
    }

    // walk from component to component until we first pass our desired x-position
    Dim lineX = this.dimFactory.zeroGui();
    for (IChatComponent component : chatLine.getChatComponent()) {
      IChatComponent originalComponent = component;

      // unwrap if required
      if (component instanceof ContainerChatComponent) {
        component = ((ContainerChatComponent)component).getComponent();
      }

      if (component instanceof ChatComponentText) {
        lineX = lineX.plus(this.fontEngine.getStringWidthDim(getFormattedText((ChatComponentText)component)));
        if (lineX.gt(x)) {
          return originalComponent;
        }

      } else if (component instanceof PrecisionChatComponent) {
        PrecisionChatComponent precisionComponent = (PrecisionChatComponent)component;

        // there will be no siblings - only one Precision component is supported per line
        return precisionComponent.getComponentAtGuiPosition(x, maxX, false, this.fontEngine);

      } else if (component instanceof ImageChatComponent) {
        ImageChatComponent imageComponent = (ImageChatComponent)component;
        lineX = lineX.plus(imageComponent.paddingGuiLeft);

        Dim width = imageComponent.getImageWidth(this.fontEngine.FONT_HEIGHT_DIM);
        if (lineX.lte(x) && lineX.plus(width).gte(x)) {
          return originalComponent;
        }
        lineX = lineX.plus(width).plus(imageComponent.paddingGuiRight);

      } else if (component instanceof UserNameChatComponent) {
        UserNameChatComponent userNameChatComponent = (UserNameChatComponent)component;
        lineX = lineX.plus(userNameChatComponent.getWidth(this.fontEngine.FONT_HEIGHT_DIM));
        if (lineX.gt(x)) {
          return originalComponent;
        }

      } else if (component instanceof InteractiveElementChatComponent) {
        InteractiveElementChatComponent interactiveElementChatComponent = (InteractiveElementChatComponent)component;
        lineX = lineX.plus(interactiveElementChatComponent.getLastWidth());
        if (lineX.gt(x)) {
          return originalComponent;
        }

      } else if (component instanceof PlatformRankChatComponent) {
        PlatformRankChatComponent platformRankChatComponent = (PlatformRankChatComponent)component;
        lineX = lineX.plus(this.fontEngine.getStringWidthDim(getFormattedText(platformRankChatComponent.getChatComponentText())));
        if (lineX.gt(x)) {
          return originalComponent;
        }

      } else if (component instanceof ContainerChatComponent) {
        throw new RuntimeException("Cannot get chat component because a container has not been unwrapped.");
      }
    }

    return null;
  }

  /** Returns the abstract ChatLine at some y-value, if one exists. */
  public @Nullable AbstractChatLine getAbstractChatLine(Dim x, Dim y) {
    int mouseX = (int) x.getScreen();
    int mouseY = this.minecraft.displayHeight - (int) y.getScreen() - 1;

    Tuple2<Integer, Integer> coords = this.mapInvertedScreenPositionIntoChat(mouseX, mouseY);
    int mappedX = coords._1;
    int mappedY = coords._2;
    ChatLine chatLine = this.getRenderedChatLine(mappedX, mappedY);

    if (chatLine == null) {
      return null;
    } else {
      return chatLine.getParent();
    }
  }

  private @Nullable ChatLine getRenderedChatLine(int mappedX, int mappedY) {
    if (!this.getChatOpen()) {
      return null;
    }

    if (mappedX < 0 || mappedY < 0) {
      return null;
    }

    int visibleLines = Math.min(this.getLineCount(), this.chatLines.size());

    int maxX = this.getLineWidth();
    int maxY = this.fontEngine.FONT_HEIGHT * visibleLines + 1;
    if (mappedX > maxX || mappedY > maxY) {
      return null;
    }

    // the line index at the current y-position
    int lineIndex = (int)Math.floor((float)mappedY / this.fontEngine.FONT_HEIGHT + this.scrollPos.get());
    if (lineIndex < 0 || lineIndex >= this.chatLines.size()) {
      return null;
    }

    return this.chatLines.get(lineIndex);
  }

  /** Given the mouse position (where 0,0 is the bottom left corner), returns the transformed coordinates within the chat window minus padding.
   * That is, 0,0 in the new coordinates points to the position at which the chat text starts. */
  private Tuple2<Integer, Integer> mapInvertedScreenPositionIntoChat(int mouseX, int mouseY) {
    // 27 is a magic number
    int bottom = 27 + this.config.getChatVerticalDisplacementEmitter().get();
    int scaleFactor = new ScaledResolution(this.minecraft).getScaleFactor();
    float chatScale = this.getChatScale();
    int x = mouseX / scaleFactor;
    int y = mouseY / scaleFactor - bottom;

    int xOffset = LEFT + LEFT_PADDING; // start of the line should be x = 0
    x = MathHelper.floor_float((float)x / chatScale) - xOffset;
    y = MathHelper.floor_float((float)y / chatScale);

    return new Tuple2<>(x, y);
  }

  public @Nullable AbstractChatLine getSelectedLine() {
    return this.selectedLine.getSelected();
  }

  public void setSelectedLine(@Nullable AbstractChatLine line) {
    this.selectedLine.setSelected(line);
  }

  @Override
  public boolean getChatOpen() {
    return this.minecraft.currentScreen instanceof GuiChat;
  }

  @Override
  public void deleteChatLine(int id) {
    this.deleteLine(ln -> ln.getChatLineID() == id);
  }

  /** Removes the component. Note that you must call `refreshChat` for the changes to come into effect. */
  public void deleteComponent(IChatComponent component) {
    this.deleteLine(ln -> ln.getChatComponent() == component);
  }

  public void deleteLine(AbstractChatLine line) {
    this.deleteLine(ln -> ln == line);
  }

  public void deleteLine(Predicate<AbstractChatLine> predicate) {
    this.abstractChatLines.removeIf(predicate);

    int removed = 0;
    Iterator<ChatLine> chatLines = this.chatLines.iterator();
    while (chatLines.hasNext()) {
      ChatLine nextLine = chatLines.next();
      if (predicate.test(nextLine.getParent())) {
        chatLines.remove();
        this.disposeComponentsInLine(nextLine.getParent());
        removed++;
      }
    }

    // this ensures that the bottom lines will shift upwards to fill the gap, if we are currently scrolled
    this.scroll(-removed, true);
  }

  /** Replaces the component of the given line. Note that you must call `refreshChat` for the changes to come into effect. */
  public void replaceLine(Predicate<AbstractChatLine> predicate, UnaryOperator<IChatComponent> componentGenerator) {
    float currentScroll = this.scrollPos.getTarget();
    float highestVisibleLine = currentScroll + this.getLineCount();
    State<Integer> scrollDelta = new State<>(0); // how many printed lines we added (positive) or removed (negative)
    this.abstractChatLines.replaceAll(abstractChatLine -> {
      if (predicate.test(abstractChatLine)) {
        List<ChatLine> oldMessageLines = Collections.filter(this.chatLines, line -> line.getParent() == abstractChatLine);
        ChatLine highestChatLine = Collections.last(oldMessageLines);

        int lineWidth = this.getLineWidth();
        IChatComponent newComponent = componentGenerator.apply(abstractChatLine.getChatComponent());
        List<IChatComponent> splitComponents = ComponentHelpers.splitText(newComponent, lineWidth, this.fontEngine);

        // only update the scroll position if the replacement happened in or below our current view.
        // if the update is in our view, this causes the lines below the replaced lines to update position, but fixes the top ones in place.
        if (this.chatLines.indexOf(highestChatLine) < highestVisibleLine) {
          int thisDelta = splitComponents.size() - oldMessageLines.size();
          scrollDelta.setState(delta -> delta + thisDelta);
        }

        return abstractChatLine.withComponentReplaced(newComponent);
      } else {
        return abstractChatLine;
      }
    });

    this.scroll(scrollDelta.getState(), true);
  }

  private void disposeComponentsInLine(AbstractChatLine chatLine) {
    for (IChatComponent component : chatLine.getChatComponent()) {
      if (component instanceof ImageChatComponent) {
        // avoid memory leaks by unloading textures before removing the line
        ImageChatComponent imageComponent = (ImageChatComponent) component;
        imageComponent.destroy(this.minecraft.getTextureManager());
      } else if (component instanceof InteractiveElementChatComponent) {
        InteractiveElementChatComponent interactiveElementChatComponent = (InteractiveElementChatComponent) component;
        interactiveElementChatComponent.dispose();
      }
    }
  }

  /** Returns the actual chat width. */
  @Override
  public int getChatWidth() {
    return calculateChatboxWidth(this.minecraft.gameSettings.chatWidth);
  }

  /** Returns the actual chat width. */
  public Dim getChatWidthDim() {
    return this.dimFactory.fromGui(this.getChatWidth());
  }

  /** Returns the effective chat width that takes into account scaling. If the font renderer measures text to be at most this width, it will fit onto the chat GUI. */
  public int getChatWidthForText() {
    float scale = this.getChatScale();
    return MathHelper.floor_float((float)this.getChatWidth() / scale);
  }

  /** Returns the effective chat width that takes into account scaling. If the font renderer measures text to be at most this width, it will fit onto the chat GUI. */
  public Dim getChatWidthForTextDim() {
    float scale = this.getChatScale();
    return this.getChatWidthDim().over(scale);
  }

  @Override
  public int getChatHeight() {
    return calculateChatboxHeight(this.getChatOpen() ? this.minecraft.gameSettings.chatHeightFocused : this.minecraft.gameSettings.chatHeightUnfocused);
  }

  @Override
  public float getChatScale() {
    return this.minecraft.gameSettings.chatScale;
  }

  public static int calculateChatboxWidth(float scale) {
    int i = 320;
    int j = 40;
    return MathHelper.floor_float(scale * (float)(i - j) + (float)j);
  }

  public static int calculateChatboxHeight(float scale) {
    int i = 180;
    int j = BOTTOM;
    return MathHelper.floor_float(scale * (float)(i - j) + (float)j);
  }

  /** Returns the maximum number of lines that fit into the visible chat window. */
  @Override
  public int getLineCount() {
    return this.getChatHeight() / this.fontEngine.FONT_HEIGHT;
  }

  /** Returns the width in GUI space. */
  private int getLineWidth() {
    return MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
  }

  private Dim getLineWidthDim() {
    return this.getChatWidthDim().over(this.getChatScale());
  }

  @FunctionalInterface
  private interface LineIterator {
    void forEachLine(LineAction step);
  }

  @FunctionalInterface
  private interface LineAction {
    void act(ChatLine line, int index, int opacity);
  }

  private static class ChatDimensions {
    public final float chatHeightFocused;
    public final float chatHeightUnfocused;
    public final float chatWidth;
    public final float chatScale;

    private ChatDimensions(float chatHeightFocused, float chatHeightUnfocused, float chatWidth, float chatScale) {
      this.chatHeightFocused = chatHeightFocused;
      this.chatHeightUnfocused = chatHeightUnfocused;
      this.chatWidth = chatWidth;
      this.chatScale = chatScale;
    }

    public ChatDimensions(GameSettings gameSettings) {
      this(gameSettings.chatHeightFocused, gameSettings.chatHeightUnfocused, gameSettings.chatWidth, gameSettings.chatScale);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ChatDimensions that = (ChatDimensions) o;
      return Float.compare(that.chatHeightFocused, chatHeightFocused) == 0 && Float.compare(that.chatHeightUnfocused, chatHeightUnfocused) == 0 && Float.compare(that.chatWidth, chatWidth) == 0 && Float.compare(that.chatScale, chatScale) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(chatHeightFocused, chatHeightUnfocused, chatWidth, chatScale);
    }
  }
}
