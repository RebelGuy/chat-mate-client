package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.gui.CustomGuiNewChat;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponent.PrecisionAlignment;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponent.PrecisionLayout;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.services.MinecraftProxyService;
import dev.rebel.chatmate.events.MinecraftChatEventService;
import dev.rebel.chatmate.util.ChatHelpers.ClickEventWithCallback;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.rebel.chatmate.gui.chat.Styles.*;

public class ChatPagination<T> {
  private final LogService logService;
  private final MinecraftProxyService minecraftProxyService;
  private final CustomGuiNewChat customGuiNewChat;
  private final DimFactory dimFactory;
  private MessageService messageService;
  private final MinecraftChatEventService minecraftChatEventService;
  private FontEngine fontEngine;
  private final List<T> items;
  private final int itemsPerPage;
  private final PaginationRowRenderer<T> renderer;
  private final int maxPage;
  private final EventCallback<?> _onChatDimensionsUpdate = this::onChatDimensionsUpdate;

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
                        CustomGuiNewChat customGuiNewChat,
                        DimFactory dimFactory,
                        MessageService messageService,
                        MinecraftChatEventService minecraftChatEventService,
                        FontEngine fontEngine,
                        PaginationRowRenderer<T> renderer,
                        List<T> items,
                        int itemsPerPage,
                        @Nullable String headerText) {
    this.logService = logService;
    this.minecraftProxyService = minecraftProxyService;
    this.customGuiNewChat = customGuiNewChat;
    this.dimFactory = dimFactory;
    this.messageService = messageService;
    this.minecraftChatEventService = minecraftChatEventService;
    this.fontEngine = fontEngine;
    this.renderer = renderer;
    this.items = items;
    this.itemsPerPage = itemsPerPage;
    this.headerText = headerText;

    this.maxPage = Math.max(0, (this.getTotalRowsToRender() - 1)) / this.itemsPerPage;

    this.initialised = false;
    this.currentPage = 0;
    this.renderedHeader1 = new ContainerChatComponent();
    this.renderedHeader2 = new ContainerChatComponent();
    this.renderedComponents = new ContainerChatComponent[Math.min(this.getTotalRowsToRender(), itemsPerPage)];
    for (int i = 0; i < this.renderedComponents.length; i++) {
      this.renderedComponents[i] = new ContainerChatComponent();
    }
    this.renderedFooter = new ContainerChatComponent();
    this.emptyLine = new ChatComponentText("");

    this.minecraftChatEventService.onUpdateChatDimensions(this._onChatDimensionsUpdate, this);
  }

  private void onChatDimensionsUpdate(Event<?> event) {
    this.render();
  }

  /** Renders the paginated entries to chat. */
  public void render() {
    this.renderHeader();

    Dim chatWidth = this.customGuiNewChat.getChatWidthDim();
    Dim effectiveChatWidth = this.customGuiNewChat.getChatWidthForTextDim();
    List<PartiallyVisibleItem<T>> visibleItems = this.getVisibleItems();
    int rowsWithContent = Collections.sum(visibleItems, x -> x.visibleToIndex - x.visibleFromIndex + 1);

    for (int i = 0; i < renderedComponents.length; i++) {
      Tuple2<T, Integer> thisItem = this.getVisibleItemAtIndex(i);
      if (i < rowsWithContent) {
        this.renderedComponents[i].setComponent(this.renderer.renderItem(thisItem._1, thisItem._2, visibleItems, this.fontEngine, chatWidth, effectiveChatWidth));
      } else {
        // empty padding on the last page
        this.renderedComponents[i].setComponent(emptyLine);
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
      for (ContainerChatComponent renderedComponent : this.renderedComponents) {
        this.minecraftProxyService.printChatMessage("ChatPagination", renderedComponent);
      }
      this.minecraftProxyService.printChatMessage("ChatPagination", this.renderedFooter);
      this.minecraftProxyService.printChatMessage("ChatPagination", this.emptyLine);
    }
  }

  private void renderHeader() {
    if (this.headerText == null) {
      this.renderedHeader1.setComponent(new ChatComponentText(""));
    } else {
      PrecisionLayout layout = new PrecisionLayout(this.dimFactory.zeroGui(), this.customGuiNewChat.getChatWidthForTextDim(), PrecisionAlignment.CENTRE);
      ChatComponentText component = new ChatComponentText(this.headerText);

      PrecisionLayout closeLayout = new PrecisionLayout(this.dimFactory.zeroGui(), this.customGuiNewChat.getChatWidthForTextDim(), PrecisionAlignment.RIGHT);
      ClickEventWithCallback onClose = new ClickEventWithCallback(this.logService, this::delete, true);
      ChatComponentText closeComponent = styledText("[x]", onClose.bind(INTERACTIVE_STYLE_DE_EMPHASISE.get()));

      this.renderedHeader1.setComponent(new PrecisionChatComponent(Arrays.asList(new Tuple2<>(closeLayout, closeComponent))));
      this.renderedHeader2.setComponent(new PrecisionChatComponent(Arrays.asList(new Tuple2<>(layout, component))));
    }
  }

  private void renderFooter() {
    this.renderedFooter.setComponent(this.messageService.getPaginationFooterMessage(
        this.customGuiNewChat.getChatWidthForTextDim(),
        this.currentPage + 1,
        this.maxPage + 1,
        this.enablePreviousPage() ? this::onPreviousPage : null,
        this.enableNextPage() ? this::onNextPage : null
    ));
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

  // todo: if performance ever becomes a problem (rendering the last page requires all other pages to be calculated),
  // we could pre-load or cache the content on each page
  private List<PartiallyVisibleItem<T>> getVisibleItems() {
    int from = this.getVisibleStartIndex();
    int to = this.getVisibleEndIndex();
    List<PartiallyVisibleItem<T>> result = new ArrayList<>();

    int i = 0;
    for (T item : this.items) {
      int size = this.renderer.getItemRows(item);
      if (i + size <= from) {
        i += size;
        continue;
      }

      int itemStartIndex = 0;
      int itemEndIndex = size - 1;
      boolean isLastItem = false;
      if (i + size > from) {
        itemStartIndex = Math.max(0, from - i);
      }
      if (i + size > to) {
        itemEndIndex = Math.min(size - 1, to - i);
        isLastItem = true;
      }

      result.add(new PartiallyVisibleItem<>(item, itemStartIndex, itemEndIndex));
      i += size;

      if (isLastItem) {
        break;
      }
    }

    return result;
  }

  /** Returns the item and the sub-index of the item at the given index.
   * Returns null if querying a blank row on the last page. */
  private @Nullable Tuple2<T, Integer> getVisibleItemAtIndex(int indexInCurrentPage) {
    int index = this.getVisibleStartIndex() + indexInCurrentPage;

    int checkedUpToIndex = 0;
    for (T item : this.items) {
      int size = this.renderer.getItemRows(item);
      if (checkedUpToIndex + size > index) {
        // we are reading from top to bottom (i.e. the index increases in that direction), but
        // rendering from bottom to top. therefore, the sub-index has to be inverted
        int subIndex = checkedUpToIndex + size - index - 1;
        int invertedSubIndex = size - 1 - subIndex;
        return new Tuple2<>(item, invertedSubIndex);
      } else {
        checkedUpToIndex += size;
      }
    }

    return null;
  }

  /** Maps the item index into the current page, where the top row has index 0 and the bottom row has index `this.itemsPerPage` - 1. */
  public @Nullable Integer mapIndex(int itemIndex) {
    if (itemIndex < 0 || itemIndex >= this.getTotalRowsToRender()) {
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
    if (to >= this.getTotalRowsToRender()) {
      to = this.getTotalRowsToRender() - 1;
    }
    return to;
  }

  private int getTotalRowsToRender() {
    return Collections.sum(Collections.list(items), renderer::getItemRows);
  }

  public abstract static class PaginationRowRenderer<T> {
    /** Should return the number of rows required to render this item. */
    public abstract int getItemRows(T item);

    /** Should return one row of the chat component rendering the given item. To help with layout, all items that are visible on the current page are also provided.
     * @param item The item we are rendering for the current row.
     * @param subIndex The current row index of the item we are rendering (from top to bottom). The maximum value is `getItemRows()` - 1. Can be ignored if `getItemRows()` returns 1 for all items.
     * @param allItemsOnPage The (possibly partial) items that are visible on the current page. If `getItemRows()` returns 1 for all items, this can be treated as the array of completely visible items.
     * @param chatWidth The actual chat width in GUI units.
     * @param effectiveChatWidth The effective chat width for text rendering considerations. If you limit your text to this width, it will fit onto a single line. */
    public abstract IChatComponent renderItem(T item, int subIndex, List<PartiallyVisibleItem<T>> allItemsOnPage, FontEngine fontEngine, Dim chatWidth, Dim effectiveChatWidth);
  }

  public static class PartiallyVisibleItem<T> {
    public final T item;
    public final int visibleFromIndex;
    /** Inclusive. */
    public final int visibleToIndex;

    public PartiallyVisibleItem(T item, int visibleFromIndex, int visibleToIndex) {
      this.item = item;
      this.visibleFromIndex = visibleFromIndex;
      this.visibleToIndex = visibleToIndex;
    }
  }
}
