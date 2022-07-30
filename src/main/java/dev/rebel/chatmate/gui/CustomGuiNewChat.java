package dev.rebel.chatmate.gui;

import com.google.common.collect.Lists;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.StateManagement.AnimatedSelection;
import dev.rebel.chatmate.gui.chat.*;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.AbstractChatLine;
import dev.rebel.chatmate.gui.models.ChatLine;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.MouseEventService.Events;
import dev.rebel.chatmate.services.events.models.GuiScreenChanged;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static dev.rebel.chatmate.gui.chat.ComponentHelpers.getFormattedText;

// Note: for maintainability reasons, please do not remove any of the @Override methods, even if they replicate the super method.
/** Responsible for drawing lines of chat and exposing the location of components. See also: `CustomGuiChat`, which is
 * the screen shown when opening the chat. */
public class CustomGuiNewChat extends GuiNewChat {
  private static final Logger logger = LogManager.getLogger();
  private static final Integer MAX_DRAWN_LINES = 100; // limits drawnChatLines
  private static final Integer MAX_LINES = 100; // limits chatLines
  private static final Integer LEFT = 2; // distance of the chat window to the left side of the screen, in GUI units
  private static final Integer LEFT_PADDING = 1; // padding between the start of the chat line, and the start of the text, in GUI units

  private final Minecraft minecraft;
  private final LogService logService;
  private final Config config;
  private final ForgeEventService forgeEventService;
  private final DimFactory dimFactory;
  private final MouseEventService mouseEventService;
  private final ContextMenuStore contextMenuStore;

  private final List<String> sentMessages = Lists.newArrayList();
  private final List<AbstractChatLine> abstractChatLines = Lists.newArrayList();
  private final List<ChatLine> chatLines = Lists.newArrayList(); // rendered chat lines, where components may be broken up into multiple lines to fit into the GUI.
  private final AnimatedSelection<AbstractChatLine> selectedLine;
  private final AnimatedSelection<AbstractChatLine> hoveredLine;

  private int scrollPos; // number of scrolled lines. 0 means we have scrolled to the bottom (most recent chat).
  private boolean isScrolled;

  public CustomGuiNewChat(Minecraft minecraft,
                          LogService logService,
                          Config config,
                          ForgeEventService forgeEventService,
                          DimFactory dimFactory,
                          MouseEventService mouseEventService,
                          ContextMenuStore contextMenuStore) {
    super(minecraft);
    this.minecraft = minecraft;
    this.logService = logService;
    this.config = config;
    this.forgeEventService = forgeEventService;
    this.dimFactory = dimFactory;
    this.mouseEventService = mouseEventService;
    this.contextMenuStore = contextMenuStore;

    this.hoveredLine = new AnimatedSelection<>(150L);
    this.selectedLine = new AnimatedSelection<>(100L);

    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
    this.forgeEventService.onGuiScreenChanged(this::onChatLoseFocus, new GuiScreenChanged.Options(GuiScreenChanged.ListenType.CLOSE_ONLY, CustomGuiChat.class));
    this.mouseEventService.on(Events.MOUSE_MOVE, this::onMouseMove, new MouseEventData.Options(), null);
  }

  private GuiScreenChanged.Out onChatLoseFocus(GuiScreenChanged.In in) {
    this.hoveredLine.setSelected(null);
    return new GuiScreenChanged.Out();
  }

  private MouseEventData.Out onMouseMove(MouseEventData.In in) {
    this.updateHoveredLine();
    return new MouseEventData.Out();
  }

  private void updateHoveredLine() {
    if (this.contextMenuStore.isShowingContextMenu() || !this.getChatOpen()) {
      this.hoveredLine.setSelected(null);
    } else {
      MouseEventData.In.MousePositionData position = this.mouseEventService.getCurrentPosition();
      this.hoveredLine.setSelected(this.getAbstractChatLine(position.x, position.y));
    }
  }

  private RenderChatGameOverlay.Out onRenderChatGameOverlay(RenderChatGameOverlay.In eventIn) {
    RenderGameOverlayEvent.Chat event = eventIn.event;
    event.setCanceled(true);
    float posX = event.posX;
    float posY = eventIn.event.posY - this.config.getChatVerticalDisplacementEmitter().get();

    // copied from the GuiIngameForge::renderChat, except using our own GuiNewChat implementation
    GlStateManager.pushMatrix();
    GlStateManager.translate(posX, posY, 0.0F);
    this.drawChat(this.minecraft.ingameGUI.getUpdateCounter());
    GlStateManager.popMatrix();
    this.minecraft.mcProfiler.endSection();

    return new RenderChatGameOverlay.Out();
  }

  @Override
  public void drawChat(int updateCounter) {
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
    int width = MathHelper.ceiling_float_int((float)this.getChatWidth() / scale);
    GlStateManager.pushMatrix();
    GlStateManager.translate(LEFT, 20.0F, 0.0F);
    GlStateManager.scale(scale, scale, 1.0F);

    // for every line that is between the scroll position and
    int renderedLines = 0; // how many lines we have actually rendered
    for (int i = 0; i + this.scrollPos < this.chatLines.size() && i < maxLines; ++i) {
      ChatLine line = this.chatLines.get(i + this.scrollPos);
      if (line == null) {
        continue;
      }

      int lineOpacity = this.getLineOpacity(updateCounter, line, opacity);
      if (lineOpacity < 4) {
        // todo: standardise magic transparency number, we've seen this before when drawing the view count reels
        continue;
      }

      this.drawLine(line, i, lineOpacity, width);
      renderedLines++;
    }

    if (this.getChatOpen()) {
      this.drawScrollBar(lineCount, renderedLines);
    }

    GlStateManager.popMatrix();
  }

  private void drawLine(ChatLine line, int index, int opacity, int width) {
    int lineLeft = LEFT_PADDING;
    int lineBottom = -index * 9; // negative because we iterate from the bottom line to the top line

    Colour backgroundColour = this.getBackgroundColour(line, opacity);
    drawRect(0, lineBottom - 9, lineLeft + width + 4, lineBottom, backgroundColour.toSafeInt());
    GlStateManager.enableBlend();

    // unpack the container
    IChatComponent chatComponent = line.getChatComponent();
    if (chatComponent instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)chatComponent;
      chatComponent = container.getComponent();
    }

    if (chatComponent instanceof PrecisionChatComponentText) {
      PrecisionChatComponentText component = (PrecisionChatComponentText)chatComponent;
      for (Tuple2<PrecisionChatComponentText.PrecisionLayout, IChatComponent> pair : component.getComponentsForLine(this.minecraft.fontRendererObj, width)) {
        int left = lineLeft + pair._1.position.getGuiValue(width);
        this.drawChatComponent(pair._2, left, lineBottom, opacity);
      }
    } else {
      int x = 0;
      for (IChatComponent component : chatComponent) {
        x += this.drawChatComponent(component, lineLeft + x, lineBottom, opacity);
      }
    }

    GlStateManager.disableAlpha();
    GlStateManager.disableBlend();
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

  /** Returns the width of the drawn component. */
  private int drawChatComponent(IChatComponent component, int x, int lineBottom, int opacity) {
    if (component instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)component;
      return drawChatComponent(container.getComponent(), x, lineBottom, opacity);

    } else if (component instanceof ChatComponentText) {
      String formattedText = getFormattedText((ChatComponentText)component);
      int textColour = 16777215 + (opacity << 24);
      this.minecraft.fontRendererObj.drawStringWithShadow(formattedText, x, lineBottom - 8, textColour);
      return this.minecraft.fontRendererObj.getStringWidth(formattedText);

    } else if (component instanceof ImageChatComponent) {
      ImageChatComponent imageComponent = (ImageChatComponent)component;
      Texture texture = imageComponent.getTexture();
      if (texture == null) {
        return 0;
      }

      // if the image is not square, it's possible that it will not be centred in the dedicated space in the chat line
      // due to rounding error. the following adjustment will fix that
      float requiredWidth = imageComponent.getRequiredWidth(this.minecraft.fontRendererObj.FONT_HEIGHT);
      int requiredWidthInt = (int)Math.ceil(requiredWidth);
      float xAdjustment = (requiredWidthInt - requiredWidth) / 2;

      Dim targetHeight = this.dimFactory.fromGui(this.minecraft.fontRendererObj.FONT_HEIGHT);
      Dim currentHeight = this.dimFactory.fromScreen(texture.height);
      float imageX = x + imageComponent.paddingGui + xAdjustment;
      float imageY = lineBottom - targetHeight.getGui();

      float scaleToReachTarget = targetHeight.getGui() / currentHeight.getGui();
      float scaleY = currentHeight.getGui() / 256 * scaleToReachTarget;
      float aspectRatio = imageComponent.getImageWidth(this.minecraft.fontRendererObj.FONT_HEIGHT) / targetHeight.getGui();
      float scaleX = aspectRatio * scaleY;

      GlStateManager.pushMatrix();
      GlStateManager.scale(scaleX, scaleY, 1);
      GlStateManager.enableBlend();

      // The following are required to prevent the rendered context menu from interfering with the status indicator colour..
      GlStateManager.color(1.0f, 1.0f, 1.0f, opacity < 4 ? 0 : opacity / 255.0f);
      GlStateManager.disableLighting();

      // undo the scaling
      float scaledX = imageX / scaleX;
      float scaledY = imageY / scaleY;
      int u = 0, v = 0;
      this.minecraft.getTextureManager().bindTexture(texture.resourceLocation);
      this.drawTexturedModalRect(scaledX, scaledY, u, v, 256, 256);

      GlStateManager.popMatrix();

      return requiredWidthInt;

    } else {
      throw new RuntimeException("Cannot draw chat component of type " + component.getClass().getSimpleName());
    }
  }

  /** Given the lineCount (total lines) and the renderedLines (how many lines are visible on screen), draws the scrollbar to the left of the chat GUI. */
  private void drawScrollBar(int lineCount, int renderedLines) {
    int lineHeight = this.minecraft.fontRendererObj.FONT_HEIGHT;
    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
    int fullHeight = lineCount * lineHeight + lineCount;
    int renderedHeight = renderedLines * lineHeight + renderedLines;
    int j3 = this.scrollPos * renderedHeight / lineCount;
    int k1 = renderedHeight * renderedHeight / fullHeight;

    if (fullHeight != renderedHeight)
    {
      int k3 = j3 > 0 ? 170 : 96;
      int l3 = this.isScrolled ? 13382451 : 3355562;
      drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
      drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
    }
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
    this.pushDrawnComponent(newLine, chatComponent, chatLineId, updateCounter);

    // todo: replace with our own logger
    logger.info("[CHAT] " + chatComponent.getUnformattedText());
  }

  private AbstractChatLine pushFullComponent(IChatComponent chatComponent, int chatLineId, int updateCounter) {
    AbstractChatLine newLine = new AbstractChatLine(updateCounter, chatComponent, chatLineId);
    this.abstractChatLines.add(0, newLine);

    // purge entries at the top
    while (this.abstractChatLines.size() > MAX_LINES) {
      AbstractChatLine lastLine = this.abstractChatLines.get(this.abstractChatLines.size() - 1);
      for (IChatComponent component : lastLine.getChatComponent()) {
        if (component instanceof ImageChatComponent) {
          // avoid memory leaks by unloading textures before removing the line
          ImageChatComponent imageComponent = (ImageChatComponent)component;
          imageComponent.destroy(this.minecraft.getTextureManager());
        }
      }
      this.abstractChatLines.remove(this.abstractChatLines.size() - 1);
    }

    return newLine;
  }

  /** Adds the component to `drawnChatLines` after processing its contents. */
  private void pushDrawnComponent(AbstractChatLine parent, IChatComponent chatComponent, int chatLineId, int updateCounter) {
    boolean processContents = true;
    if (chatComponent instanceof PrecisionChatComponentText) {
      processContents = false;
    } else if (chatComponent instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)chatComponent;
      processContents = !(container.getComponent() instanceof PrecisionChatComponentText);
    }

    if (!processContents) {
      // push as-is
      this.pushDrawnChatLine(new ChatLine(updateCounter, chatComponent, chatLineId, parent));

    } else {
      int lineWidth = this.getLineWidth();
      List<IChatComponent> splitComponents = ComponentHelpers.splitText(chatComponent, lineWidth, this.minecraft.fontRendererObj);
      for (IChatComponent component : splitComponents) {
        this.pushDrawnChatLine(new ChatLine(updateCounter, component, chatLineId, parent));
      }
    }

    this.updateHoveredLine();
  }

  /** Adds the ChatLine to the `drawnChatLines`. */
  private void pushDrawnChatLine(ChatLine line) {
    this.chatLines.add(0, line);

    // make sure we keep the same lines visible even as we push more lines to the bottom of the chat
    if (this.getChatOpen() && this.scrollPos > 0) {
      this.isScrolled = true;
      this.scroll(1);
    }

    // purge entries at the top
    while (this.chatLines.size() > MAX_DRAWN_LINES) {
      this.chatLines.remove(this.chatLines.size() - 1);
    }
  }

  /** Re-draws all `this.chatLines`, generating a new list of `drawnChatLines`. Probably used when the chat width has been changed. */
  @Override
  public void refreshChat()
  {
    this.refreshChat(false);
  }

  public void refreshChat(boolean keepScrollPos) {
    int initialScrollPos = this.scrollPos;
    boolean initialIsScrolled = this.isScrolled;

    this.chatLines.clear();
    if (!keepScrollPos) {
      this.resetScroll();
    }

    for (int i = this.abstractChatLines.size() - 1; i >= 0; --i) {
      AbstractChatLine chatline = this.abstractChatLines.get(i);
      this.pushDrawnComponent(chatline, chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter());
    }

    if (keepScrollPos) {
      this.scrollPos = initialScrollPos;
      this.isScrolled = initialIsScrolled;
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
    this.scrollPos = 0;
    this.isScrolled = false;
  }

  /** Scrolls the chat by the given number of lines. */
  @Override
  public void scroll(int amount) {
    this.scrollPos += amount;
    int i = this.chatLines.size();

    // clamp maximum
    if (this.scrollPos > i - this.getLineCount()) {
      this.scrollPos = i - this.getLineCount();
    }

    // clamp minimum and stop scrolling
    if (this.scrollPos <= 0) {
      this.scrollPos = 0;
      this.isScrolled = false;
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
    int x = coords._1;
    int y = coords._2;
    int maxX = this.getLineWidth();

    ChatLine chatLine = this.getRenderedChatLine(x, y);
    if (chatLine == null) {
      return null;
    }

    // walk from component to component until we first pass our desired x-position
    int lineX = 0;
    for (IChatComponent component : chatLine.getChatComponent()) {
      IChatComponent originalComponent = component;

      // unwrap if required
      if (component instanceof ContainerChatComponent) {
        component = ((ContainerChatComponent)component).getComponent();
      }

      if (component instanceof ChatComponentText) {
        lineX += this.minecraft.fontRendererObj.getStringWidth(getFormattedText((ChatComponentText)component));
        if (lineX > x) {
          return originalComponent;
        }
      } else if (component instanceof PrecisionChatComponentText) {
        PrecisionChatComponentText precisionComponent = (PrecisionChatComponentText)component;

        // there will be no siblings - only one Precision component is supported per line
        return precisionComponent.getComponentAtGuiPosition(x, maxX, false, this.minecraft.fontRendererObj);

      } else if (component instanceof ImageChatComponent) {
        ImageChatComponent imageComponent = (ImageChatComponent)component;
        lineX += imageComponent.paddingGui;

        int width = (int)Math.ceil(imageComponent.getImageWidth(this.minecraft.fontRendererObj.FONT_HEIGHT));
        if (lineX <= x && lineX + width >= x) {
          return originalComponent;
        }
        lineX += width + imageComponent.paddingGui;

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
    int maxY = this.minecraft.fontRendererObj.FONT_HEIGHT * visibleLines + 1;
    if (mappedX > maxX || mappedY > maxY) {
      return null;
    }

    // the line index at the current y-position
    int lineIndex = mappedY / this.minecraft.fontRendererObj.FONT_HEIGHT + this.scrollPos;
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
    this.abstractChatLines.removeIf(line -> line.getChatLineID() == id);
    this.chatLines.removeIf(line -> line.getChatLineID() == id);
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
      if (predicate.test(chatLines.next().getParent())) {
        chatLines.remove();
        removed++;
      }
    }

    // this ensures that the bottom lines will shift upwards to fill the gap, if we are currently scrolled
    this.scroll(-removed);
  }

  @Override
  public int getChatWidth() {
    return calculateChatboxWidth(this.minecraft.gameSettings.chatWidth);
  }

  /** Returns the effective chat width that takes into account scaling. If the font renderer measures text to be at most this width, it will fit onto the chat GUI. */
  public int getChatWidthForText() {
    float scale = this.getChatScale();
    return MathHelper.floor_float((float)this.getChatWidth() / scale);
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
    int j = 20;
    return MathHelper.floor_float(scale * (float)(i - j) + (float)j);
  }

  /** Returns the maximum number of lines that fit into the visible chat window. */
  @Override
  public int getLineCount() {
    return this.getChatHeight() / 9;
  }

  /** Returns the width in GUI space. */
  private int getLineWidth() {
    return MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
  }
}
