package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText.PrecisionAlignment;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText.PrecisionLayout;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText.PrecisionValue;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.services.MinecraftProxyService;
import dev.rebel.chatmate.services.util.ChatHelpers.ClickEventWithCallback;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static dev.rebel.chatmate.models.Styles.*;

public class ChatPagination<T> {
  private final LogService logService;
  private MinecraftProxyService minecraftProxyService;
  private MessageService messageService;
  private final T[] items;
  private final int itemsPerPage;
  private final PaginationRenderer<T> renderer;
  private final int maxPage;

  private @Nullable String headerText;
  private boolean initialised;
  private int currentPage;
  private ContainerChatComponent renderedHeader1;
  private ContainerChatComponent renderedHeader2;
  private ContainerChatComponent[] renderedComponents;
  private ContainerChatComponent renderedFooter;
  private IChatComponent emptyLine;

  public ChatPagination(LogService logService,
                        MinecraftProxyService minecraftProxyService,
                        MessageService messageService,
                        PaginationRenderer<T> renderer,
                        T[] items,
                        int itemsPerPage,
                        @Nullable String headerText) {
    this.logService = logService;
    this.minecraftProxyService = minecraftProxyService;
    this.messageService = messageService;
    this.renderer = renderer;
    this.items = items;
    this.itemsPerPage = itemsPerPage;
    this.headerText = headerText;
    this.maxPage = Math.max(0, (this.items.length - 1)) / this.itemsPerPage;

    this.initialised = false;
    this.currentPage = 0;
    this.renderedHeader1 = new ContainerChatComponent();
    this.renderedHeader2 = new ContainerChatComponent();
    this.renderedComponents = new ContainerChatComponent[Math.min(items.length, itemsPerPage)];
    for (int i = 0; i < this.renderedComponents.length; i++) {
      this.renderedComponents[i] = new ContainerChatComponent();
    }
    this.renderedFooter = new ContainerChatComponent();
    this.emptyLine = new ChatComponentText("");
  }

  /** Renders the paginated entries to chat. */
  public void render() {
    this.renderHeader();

    FontRenderer fontRenderer =  this.minecraftProxyService.getChatFontRenderer();
    int chatWidth = this.minecraftProxyService.getChatWidth();
    int effectiveChatWidth = this.minecraftProxyService.getChatWidthForText();
    T[] visibleItems = this.getVisibleItems();
    for (int i = 0; i < renderedComponents.length; i++) {
      if (i < visibleItems.length) {
        this.renderedComponents[i].component = this.renderer.renderItem(visibleItems[i], visibleItems, fontRenderer, chatWidth, effectiveChatWidth);
      } else {
        // empty padding on the last page
        this.renderedComponents[i].component = emptyLine;
      }
    }

    this.renderFooter();

    this.onUpdated();
  }

  /** Deletes the rendered object from chat. */
  public void delete() {
    if (!this.initialised) {
      return;
    }

    this.minecraftProxyService.deleteComponentFromChat(this.renderedHeader1);
    this.renderedHeader1 = new ContainerChatComponent();
    this.minecraftProxyService.deleteComponentFromChat(this.renderedHeader2);
    this.renderedHeader2 = new ContainerChatComponent();

    for (int i = 0; i < this.renderedComponents.length; i++) {
      this.minecraftProxyService.deleteComponentFromChat(this.renderedComponents[i]);
      this.renderedComponents[i] = new ContainerChatComponent();
    }

    this.minecraftProxyService.deleteComponentFromChat(this.renderedFooter);
    this.renderedFooter = new ContainerChatComponent();

    this.minecraftProxyService.deleteComponentFromChat(this.emptyLine);
    this.emptyLine = new ChatComponentText("");

    this.initialised = false;
  }

  /** Updates the header text. */
  public void setHeaderText(@Nullable String headerText) {
    if (Objects.equals(this.headerText, headerText)) {
      return;
    }

    this.headerText = headerText;
    this.renderHeader();
  }

  private void onUpdated() {
    if (this.initialised) {
      this.minecraftProxyService.refreshChat();
    } else {
      this.initialised = true;
      this.minecraftProxyService.printChatMessage("ChatPagination", this.renderedHeader1);
      this.minecraftProxyService.printChatMessage("ChatPagination", this.renderedHeader2);
      for (int i = 0; i < this.renderedComponents.length; i++) {
        this.minecraftProxyService.printChatMessage("ChatPagination", this.renderedComponents[i]);
      }
      this.minecraftProxyService.printChatMessage("ChatPagination", this.renderedFooter);
      this.minecraftProxyService.printChatMessage("ChatPagination", this.emptyLine);
    }
  }

  private void renderHeader() {
    if (this.headerText == null) {
      this.renderedHeader1.component = new ChatComponentText("");
    } else {
      PrecisionLayout layout = new PrecisionLayout(new PrecisionValue(0.0f), new PrecisionValue(1.0f), PrecisionAlignment.CENTRE);
      ChatComponentText component = new ChatComponentText(this.headerText);

      PrecisionLayout closeLayout = new PrecisionLayout(new PrecisionValue(0.0f), new PrecisionValue(1.0f), PrecisionAlignment.RIGHT);
      ClickEventWithCallback onClose = new ClickEventWithCallback(this.logService, this::delete, true);
      ChatComponentText closeComponent = styledText("[x]", onClose.bind(INTERACTIVE_STYLE_DE_EMPHASISE.get()));

      this.renderedHeader1.component = new PrecisionChatComponentText(Arrays.asList(new Tuple2<>(closeLayout, closeComponent)));
      this.renderedHeader2.component = new PrecisionChatComponentText(Arrays.asList(new Tuple2<>(layout, component)));
    }
  }

  private void renderFooter() {
    this.renderedFooter.component = this.messageService.getPaginationFooterMessage(
        this.minecraftProxyService.getChatWidthForText(),
        this.currentPage + 1,
        this.maxPage + 1,
        this.enablePreviousPage() ? this::onPreviousPage : null,
        this.enableNextPage() ? this::onNextPage : null
    );
  }

  private boolean enablePreviousPage() {
    return this.currentPage > 0;
  }

  private boolean enableNextPage() {
    return this.currentPage < this.maxPage;
  }

  private void onNextPage() {
    if (this.enableNextPage()) {
      this.currentPage++;
      this.render();
    }
  }

  private void onPreviousPage() {
    if (this.enablePreviousPage()) {
      this.currentPage--;
      this.render();
    }
  }

  private T[] getVisibleItems() {
    if (items.length <= this.itemsPerPage) {
      return Arrays.copyOf(this.items, this.items.length);
    } else {
      int from = this.getVisibleStartIndex();
      int to = this.getVisibleEndIndex();
      return Arrays.copyOfRange(this.items, from, to + 1);
    }
  }

  /** Maps the item index into the current page, where the top row has index 0 and the bottom row has index `this.itemsPerPage` - 1. */
  public @Nullable Integer mapIndex(int itemIndex) {
    if (itemIndex < 0 || itemIndex >= this.items.length) {
      return null;
    }

    int from = this.getVisibleStartIndex();
    int to = this.getVisibleEndIndex();
    int shiftedHighlighted = itemIndex - from;
    if (shiftedHighlighted >= 0 && shiftedHighlighted <= to) {
      return shiftedHighlighted;
    }

    return null;
  }

  private int getVisibleStartIndex() {
    return this.currentPage * this.itemsPerPage;
  }

  private int getVisibleEndIndex() {
    int to = this.currentPage * this.itemsPerPage + this.itemsPerPage - 1;
    if (to >= this.items.length) {
      to = this.items.length - 1;
    }
    return to;
  }

  public abstract static class PaginationRenderer<T> {
    /** Should return the chat component for rendering the given item. To help with layout, all items that are visible on the current page are also provided.
     * @param chatWidth is the actual chat width in GUI units.
     * @param effectiveChatWidth is the effective chat width for text rendering considerations. If you limit your text to this width, it will fit onto a single line. */
    public abstract IChatComponent renderItem(T item, T[] allItemsOnPage, FontRenderer fontRenderer, int chatWidth, int effectiveChatWidth);
  }
}
