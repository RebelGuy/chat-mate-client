package dev.rebel.chatmate.gui;

import com.google.common.collect.Lists;
import dev.rebel.chatmate.gui.chat.ContainerChatComponent;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.ForgeEventService;
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

import java.util.Iterator;
import java.util.List;

// Note: for maintainability reasons, please do not remove any of the @Override methods, even if they replicate the super method.
/** Responsible for drawing lines of chat and exposing the location of components. See also: `CustomGuiChat`, which is
 * the screen shown when opening the chat. */
public class CustomGuiNewChat extends GuiNewChat {
  private static final Logger logger = LogManager.getLogger();
  private static final Integer MAX_DRAWN_LINES = 100; // limits drawnChatLines
  private static final Integer MAX_LINES = 100; // limits chatLines

  private final Minecraft minecraft;
  private final LogService logService;
  private final Config config;
  private final ForgeEventService forgeEventService;
  private final List<String> sentMessages = Lists.newArrayList();
  private final List<ChatLine> chatLines = Lists.newArrayList(); // unrendered chat lines
  private final List<ChatLine> drawnChatLines = Lists.newArrayList(); // rendered chat lines, where components may be broken up into multiple lines to fit into the GUI.
  private int scrollPos; // number of scrolled lines. 0 means we have scrolled to the bottom (most recent chat).
  private boolean isScrolled;

  public CustomGuiNewChat(Minecraft minecraft, LogService logService, Config config, ForgeEventService forgeEventService) {
    super(minecraft);
    this.minecraft = minecraft;
    this.logService = logService;
    this.config = config;
    this.forgeEventService = forgeEventService;

    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
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

    int lineCount = this.drawnChatLines.size();
    if (lineCount == 0) {
      return;
    }

    int maxLines = this.getLineCount();
    float opacity = this.minecraft.gameSettings.chatOpacity * 0.9F + 0.1F;

    float scale = this.getChatScale();
    int width = MathHelper.ceiling_float_int((float)this.getChatWidth() / scale);
    GlStateManager.pushMatrix();
    GlStateManager.translate(2.0F, 20.0F, 0.0F);
    GlStateManager.scale(scale, scale, 1.0F);

    // for every line that is between the scroll position and
    int renderedLines = 0; // how many lines we have actually rendered
    for (int i = 0; i + this.scrollPos < this.drawnChatLines.size() && i < maxLines; ++i) {
      ChatLine line = this.drawnChatLines.get(i + this.scrollPos);
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
    int lineLeft = 0;
    int lineBottom = -index * 9; // negative because we iterate from the bottom line to the top line
    drawRect(lineLeft, lineBottom - 9, lineLeft + width + 4, lineBottom, opacity / 2 << 24);
    GlStateManager.enableBlend();

    IChatComponent chatComponent = line.getChatComponent();
    if (chatComponent instanceof PrecisionChatComponentText) {
      PrecisionChatComponentText component = (PrecisionChatComponentText)chatComponent;
      for (Tuple2<PrecisionChatComponentText.PrecisionLayout, ChatComponentText> pair : component.getComponentsForLine(this.minecraft.fontRendererObj, width)) {
        int left = lineLeft + pair._1.position.getGuiValue(width);
        this.drawChatComponent(pair._2, left, lineBottom, opacity);
      }
    } else {
      this.drawChatComponent(line.getChatComponent(), lineLeft, lineBottom, opacity);
    }
    GlStateManager.disableAlpha();
    GlStateManager.disableBlend();
  }

  private void drawChatComponent(IChatComponent component, int x, int lineBottom, int opacity) {
    int textColour = 16777215 + (opacity << 24);
    String formattedText = component.getFormattedText();
    this.minecraft.fontRendererObj.drawStringWithShadow(formattedText, x, lineBottom - 8, textColour);
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
    this.drawnChatLines.clear();
    this.chatLines.clear();
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
    this.pushFullComponent(chatComponent, chatLineId, updateCounter);
    this.pushDrawnComponent(chatComponent, chatLineId, updateCounter);

    // todo: replace with our own logger
    logger.info("[CHAT] " + chatComponent.getUnformattedText());
  }

  private void pushFullComponent(IChatComponent chatComponent, int chatLineId, int updateCounter) {
    // indiscriminately add all component types
    this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

    // purge entries at the top
    while (this.chatLines.size() > MAX_LINES) {
      this.chatLines.remove(this.chatLines.size() - 1);
    }
  }

  /** Adds the component to `drawnChatLines` after processing its contents. */
  private void pushDrawnComponent(IChatComponent chatComponent, int chatLineId, int updateCounter) {
    if (chatComponent instanceof ContainerChatComponent) {
      // note that the container is stored in `this.chatLines`, but the contents are stored in `this.drawnChatLines`.
      ContainerChatComponent container = (ContainerChatComponent)chatComponent;
      this.pushDrawnComponent(container.component, chatLineId, updateCounter);

    } else if (chatComponent instanceof PrecisionChatComponentText) {
      this.pushDrawnChatLine(new ChatLine(updateCounter, chatComponent, chatLineId));

    } else {
      int lineWidth = this.getLineWidth();
      List<IChatComponent> splitComponents = GuiUtilRenderComponents.splitText(chatComponent, lineWidth, this.minecraft.fontRendererObj, false, false);
      for (IChatComponent component : splitComponents) {
        this.pushDrawnChatLine(new ChatLine(updateCounter, component, chatLineId));
      }
    }
  }

  /** Adds the ChatLine to the `drawnChatLines`. */
  private void pushDrawnChatLine(ChatLine line) {
    this.drawnChatLines.add(0, line);

    // make sure we keep the same lines visible even as we push more lines to the bottom of the chat
    if (this.getChatOpen() && this.scrollPos > 0) {
      this.isScrolled = true;
      this.scroll(1);
    }

    // purge entries at the top
    while (this.drawnChatLines.size() > MAX_DRAWN_LINES) {
      this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
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

    this.drawnChatLines.clear();
    if (!keepScrollPos) {
      this.resetScroll();
    }

    for (int i = this.chatLines.size() - 1; i >= 0; --i) {
      ChatLine chatline = this.chatLines.get(i);
      this.pushDrawnComponent(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter());
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
    int i = this.drawnChatLines.size();

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
    return this.getChatComponent((int)x.getScreen(), (int)y.getScreen());
  }

  /** Gets the chat component at the screen position. */
  @Override
  public IChatComponent getChatComponent(int mouseX, int mouseY) {
    if (!this.getChatOpen()) {
      return null;
    }

    // 27 is a magic number
    int bottom = 27 + this.config.getChatVerticalDisplacementEmitter().get();
    int scaleFactor = new ScaledResolution(this.minecraft).getScaleFactor();
    float chatScale = this.getChatScale();
    int x = mouseX / scaleFactor - 3; // 3 is a magic number
    int y = mouseY / scaleFactor - bottom;
    x = MathHelper.floor_float((float)x / chatScale);
    y = MathHelper.floor_float((float)y / chatScale);

    if (x < 0 || y < 0) {
      return null;
    }

    int visibleLines = Math.min(this.getLineCount(), this.drawnChatLines.size());

    int maxX = this.getLineWidth();
    int maxY = this.minecraft.fontRendererObj.FONT_HEIGHT * visibleLines + visibleLines + 1;
    if (x > maxX || y > maxY) {
      return null;
    }

    // the line index at the current y-position
    int lineIndex = y / this.minecraft.fontRendererObj.FONT_HEIGHT + this.scrollPos;
    if (lineIndex < 0 || lineIndex >= this.drawnChatLines.size()) {
      return null;
    }

    ChatLine chatline = this.drawnChatLines.get(lineIndex);

    // walk from component to component until we first pass our desired x-position
    int lineX = 0;
    for (IChatComponent component : chatline.getChatComponent()) {
      // unwrap if required
      while (component instanceof ContainerChatComponent) {
        component = ((ContainerChatComponent)component).component;
      }

      if (component instanceof ChatComponentText) {
        lineX += this.minecraft.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)component).getChatComponentText_TextValue(), false));
        if (lineX > x) {
          return component;
        }
      } else if (component instanceof PrecisionChatComponentText) {
        PrecisionChatComponentText precisionComponent = (PrecisionChatComponentText)component;

        // there will be no siblings - only one Precision component is supported per line
        return precisionComponent.getComponentAtGuiPosition(x, maxX, false, this.minecraft.fontRendererObj);
      }
    }

    return null;
  }

  @Override
  public boolean getChatOpen() {
    return this.minecraft.currentScreen instanceof GuiChat;
  }

  @Override
  public void deleteChatLine(int id) {
    this.chatLines.removeIf(line -> line.getChatLineID() == id);
    this.drawnChatLines.removeIf(line -> line.getChatLineID() == id);
  }

  /** Removes the component. Note that you must call `refreshChat` for the changes to come into effect. */
  public void deleteComponent(IChatComponent component) {
    int removed = 0;
    Iterator<ChatLine> chatLines = this.chatLines.iterator();
    while (chatLines.hasNext()) {
      if (chatLines.next().getChatComponent() == component) {
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
