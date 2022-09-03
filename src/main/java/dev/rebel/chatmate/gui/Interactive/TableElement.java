package dev.rebel.chatmate.gui.Interactive;

import com.google.common.base.Objects;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.HorizontalDivider.FillMode;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.StateManagement.State;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TableElement<T> extends ContainerElement {
  private final RectExtension cellPadding;
  private List<T> items;
  private final List<LabelElement> headerLabels;
  private final RowElement headerRow;
  private final Function<T, List<IElement>> rowGetter;
  private @Nonnull List<RowElement> rows;
  /** Normalised sizings. */
  private final List<Column> columns;
  /** Evaluated while calculating size. */
  private List<Dim> columnWidths;

  private Dim minHeight;
  private @Nullable Consumer<T> onClickItem;

  public TableElement(InteractiveContext context, IElement parent, List<T> items, List<Column> columns, Function<T, List<IElement>> rowGetter) {
    super(context, parent, LayoutMode.BLOCK);
    this.cellPadding = new RectExtension(context.dimFactory.fromGui(2));
    this.rows = new ArrayList<>();

    this.headerLabels = Collections.map(columns, c -> new LabelElement(context, this)
        .setText(c.header)
        .setFontScale(c.fontScale)
        .setOverflow(TextOverflow.SPLIT)
        .setAlignment(TextAlignment.CENTRE));

    this.headerRow = new RowElement(context, this, this.headerLabels);
    super.addElement(this.headerRow);

    this.rowGetter = rowGetter;
    this.setItems(items);

    float sum = Collections.sumFloat(columns, c -> c.width);
    this.columns = Collections.map(columns, c -> new Column(c.header, c.fontScale, c.width / sum, c.fitWidth));

    this.minHeight = ZERO;
  }

  public TableElement<T> setMinHeight(Dim minHeight) {
    this.minHeight = minHeight;
    return this;
  }

  public TableElement<T> setOnClickItem(Consumer<T> onClickItem) {
    this.onClickItem = onClickItem;
    return this;
  }

  public TableElement<T> setItems(@Nullable List<T> items) {
    if (items == null) {
      items = new ArrayList<>();
    }

    this.rows.forEach(super::removeElement);
    this.items = items;
    this.rows = Collections.map(items, item -> new RowElement(context, this, item, this.rowGetter.apply(item)));
    this.rows.forEach(super::addElement);
    super.onInvalidateSize();
    return this;
  }

  /** Refreshes the row for the given item, if it exists in the list. Accepts the list of cells to render or, if not provided, uses the default row getter. */
  public void updateItem(T item, @Nullable List<IElement> newRow) {
    int index = this.items.indexOf(item);
    if (index == -1) {
      return;
    }

    if (newRow == null) {
      newRow = this.rowGetter.apply(item);
    }

    // have to replace all rows in the container element so that the order of the new row element is correct.
    this.rows.forEach(super::removeElement);
    this.rows.remove(index);
    this.rows.add(index, new RowElement(context, this, item, newRow));
    this.rows.forEach(super::addElement);
    super.onInvalidateSize();
  }

  private List<Dim> getColumnWidths(Dim maxRowWidth) {
    List<Dim> minimisedWidths = Collections.map(this.columns, (c, i) -> {
      Dim maybeWidth = maxRowWidth.times(c.width);
      if (c.fitWidth) {
        // if all elements need less space, contract the column
        List<Dim> requestedWidths = Collections.map(this.getColumn(i), el -> el.calculateSize(maybeWidth).getX());
        Dim desiredHeaderWidth = this.calculateDesiredHeaderWidth(i);
        Dim largestRequestedWidth = Dim.max(Objects.firstNonNull(Dim.max(requestedWidths), ZERO), desiredHeaderWidth);
        if (largestRequestedWidth.lt(maybeWidth)) {
          return largestRequestedWidth;
        } else {
          return maybeWidth;
        }
      } else {
        return maybeWidth;
      }
    });

    if (this.getSizingMode() == SizingMode.MINIMISE) {
      return minimisedWidths;

    } else {
      // expand the columns that allow it until we fill the maxRowWidth
      Dim expandableWidth = maxRowWidth.minus(Dim.sum(minimisedWidths));
      float expandableRelWidth = Collections.sumFloat(this.columns, c -> c.fitWidth ? 0 : c.width);
      if (expandableWidth.getGui() <= 0 || expandableRelWidth == 0) {
        // columns will not expand - table width will either overflow or underflow
        return minimisedWidths;
      }

      List<Dim> expandedWidths = Collections.map(this.columns, (cs, i) -> {
        if (cs.fitWidth) {
          return minimisedWidths.get(i);
        } else {
          return minimisedWidths.get(i).plus(expandableWidth.times(cs.width / expandableRelWidth));
        }
      });
      return expandedWidths;
    }
  }

  private Dim calculateDesiredHeaderWidth(int i) {
    LabelElement label = this.headerLabels.get(i);
    Dim labelWidth = label.calculateWidthToFitLongestWord(); // inner width
    Dim labelElementWidth = label.getFullBoxWidth(labelWidth);
    return this.headerRow.getCell(i).getFullBoxWidth(labelElementWidth); // full outer width
  }

  private List<WrapperElement> getColumn(int colIndex) {
    return Collections.map(this.rows, r -> r.getCell(colIndex));
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxWidth) {
    this.columnWidths = this.getColumnWidths(maxWidth);
    DimPoint calculatedSize = super.calculateThisSize(maxWidth);
    return new DimPoint(calculatedSize.getX(), Dim.max(this.minHeight, calculatedSize.getY()));
  }

  public static class Column {
    private final String header;
    public final float fontScale;
    /** The proportional width to aim for. */
    public final float width;
    /** If true, column will not expand if there is extra room in the table. */
    public final boolean fitWidth;

    public Column(String header, float fontScale, float width, boolean fitWidth) {
      this.header = header;
      this.fontScale = fontScale;
      this.width = width;
      this.fitWidth = fitWidth;
    }
  }
  
  private class RowElement extends ContainerElement {
    private final boolean isHeader;
    private final @Nullable T item; // for non-headers
    private final @Nullable HorizontalDivider headerDivider; // for headers
    private final List<WrapperElement> cells;

    private final State<RowState> state = new State<>(RowState.initialState());

    /** Create a new content row. */
    public RowElement(InteractiveContext context, IElement parent, T item, List<IElement> rawContents) {
      super(context, parent, LayoutMode.INLINE);
      super.setCursor(CursorType.CLICK);

      this.isHeader = false;
      this.item = item;
      this.headerDivider = null;
      this.cells = Collections.map(rawContents, cell ->
          new WrapperElement(context, this, cell)
              .setVerticalAlignment(VerticalAlignment.MIDDLE)
              .setHorizontalAlignment(HorizontalAlignment.CENTRE)
              .setPadding(TableElement.this.cellPadding)
              .cast()
      );
    }

    /** Create a new header row. */
    public RowElement(InteractiveContext context, IElement parent, List<LabelElement> headers) {
      super(context, parent, LayoutMode.INLINE);
      super.setCursor(CursorType.CLICK);

      this.isHeader = true;
      this.item = null;
      this.headerDivider = new HorizontalDivider(context, this)
          .setMode(FillMode.PARENT_CONTENT)
          .setColour(Colour.WHITE);
      this.cells = Collections.map(headers, h ->
          new WrapperElement(context, this, h)
              .setVerticalAlignment(VerticalAlignment.MIDDLE)
              .setHorizontalAlignment(HorizontalAlignment.CENTRE)
              .setPadding(TableElement.this.cellPadding)
              .cast()
      );
    }

    @Override
    public void onInitialise() {
      super.onInitialise();

      this.cells.forEach(super::addElement);

      if (this.isHeader) {
        super.addElement(this.headerDivider);
      }
    }

    @Override
    protected DimPoint calculateThisSize(Dim maxWidth) {
      List<Dim> columnWidths = TableElement.this.columnWidths;
      Dim height = Dim.max(Collections.map(columnWidths, (cWidth, i) -> this.getCell(i).calculateSize(cWidth).getY()));

      Dim x = ZERO;
      for (int c = 0; c < columnWidths.size(); c++) {
        WrapperElement cell = this.getCell(c);
        DimPoint boxSize = new DimPoint(columnWidths.get(c), height);
        DimRect box = new DimRect(new DimPoint(ZERO, ZERO), boxSize);
        DimPoint actualSize = cell.calculateSize(boxSize.getX());
        DimRect relBox = ElementHelpers.alignElementInBox(actualSize, box, cell.getHorizontalAlignment(), cell.getVerticalAlignment());
        this.childrenRelBoxes.put(cell, relBox.withTranslation(new DimPoint(x, ZERO)));

        x = x.plus(box.getWidth());
      }

      Dim tableWidth = Dim.sum(columnWidths);
      if (this.isHeader) {
        assert this.headerDivider != null;
        DimPoint dividerSize = this.headerDivider.calculateSize(tableWidth);
        this.childrenRelBoxes.put(this.headerDivider, new DimRect(new DimPoint(ZERO, height), dividerSize));
        height = height.plus(dividerSize.getY());
      }

      return new DimPoint(tableWidth, height);
    }

    public WrapperElement getCell(int i) {
      return this.cells.get(i);
    }

    @Override
    public void onMouseDown(IEvent<In> e) {
      if (this.item != null && TableElement.this.onClickItem != null) {
        TableElement.this.onClickItem.accept(this.item);
      }
    }

    @Override
    public void onMouseEnter(IEvent<In> e) {
      if (!this.isHeader) {
        this.state.accessState(s -> s.hovering.set(true));
      }
    }

    @Override
    public void onMouseExit(IEvent<In> e) {
      this.state.accessState(s -> s.hovering.set(false));
    }

    @Override
    protected boolean shouldUseCursor() {
      return super.shouldUseCursor() && this.item != null && TableElement.this.onClickItem != null;
    }

    @Override
    protected void renderElement() {
      float hoveringIntensity = this.state.getState().hovering.getFrac();
      if (hoveringIntensity > 0) {
        RendererHelpers.drawRect(0, this.getBox(), Colour.LTGREY.withAlpha(0.1f * hoveringIntensity), null, null, gui(2));
      }

      super.renderElement();
    }
  }

  private static class RowState {
    public AnimatedBool hovering;

    public static RowState initialState() {
      return new RowState() {{
        hovering = new AnimatedBool(100L, false);
      }};
    }
  }
}
