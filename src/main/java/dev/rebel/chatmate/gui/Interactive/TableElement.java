package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;

import java.util.List;
import java.util.function.Function;

public class TableElement<T> extends ContainerElement {
  private final RectExtension cellPadding;
  private final List<T> items;
  private final Function<T, List<IElement>> getRow;
  private final List<LabelElement> headerLabels;
  private final List<WrapperElement> headerCells;
  private final List<List<WrapperElement>> rows;
  /** Normalised sizings. */
  private final List<Column> columns;

  private Dim minHeight;

  public TableElement(InteractiveContext context, IElement parent, List<T> items, List<Column> columns, Function<T, List<IElement>> getRow) {
    super(context, parent, LayoutMode.BLOCK);
    this.cellPadding = new RectExtension(context.dimFactory.fromGui(2));
    this.items = items;
    this.getRow = getRow;

    this.headerLabels = Collections.map(columns, c -> new LabelElement(context, this)
        .setText(c.header)
        .setOverflow(TextOverflow.SPLIT)
        .setAlignment(TextAlignment.CENTRE));
    this.headerCells = Collections.map(this.headerLabels, h -> (WrapperElement)new WrapperElement(context, this, h)
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .setHorizontalAlignment(HorizontalAlignment.CENTRE)
        .setPadding(this.cellPadding)
    );

    this.rows = Collections.map(items, item -> Collections.map(getRow.apply(item), cell ->
        (WrapperElement)new WrapperElement(context, this, cell)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setHorizontalAlignment(HorizontalAlignment.CENTRE)
            .setPadding(this.cellPadding)
    ));

    float sum = Collections.sumFloat(columns, c -> c.width);
    this.columns = Collections.map(columns, c -> new Column(c.header, c.width / sum, c.fitWidth));

    this.minHeight = ZERO;
  }

  @Override
  public void onCreate() {
    this.headerCells.forEach(super::addElement);
    this.rows.forEach(row -> row.forEach(super::addElement));

    super.onCreate();
  }

  public TableElement<T> setMinHeight(Dim minHeight) {
    this.minHeight = minHeight;
    return this;
  }

  private List<Dim> getColumnWidths(Dim maxRowWidth) {
    List<Dim> minimisedWidths = Collections.map(this.columns, (c, i) -> {
      Dim maybeWidth = maxRowWidth.times(c.width);
      if (c.fitWidth) {
        // if all elements need less space, contract the column
        List<Dim> requestedWidths = Collections.map(this.getColumn(i), el -> el.calculateSize(maybeWidth).getX());
        Dim desiredHeaderWidth = this.headerLabels.get(i).calculateWidthToFitLongestWord();
        Dim largestRequestedWidth = Dim.max(Dim.max(requestedWidths), desiredHeaderWidth);
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

  private Dim getHeaderRowHeight(List<Dim> columnWidths) {
    return Dim.max(Collections.map(columnWidths, (col, i) -> this.headerCells.get(i).calculateSize(col).getY()));
  }

  private List<Dim> getRowHeights(List<Dim> columnWidths) {
    return Collections.map(this.rows, row -> Dim.max(Collections.map(columnWidths, (col, i) -> row.get(i).calculateSize(col).getY())));
  }

  private List<WrapperElement> getColumn(int colIndex) {
    return Collections.map(this.rows, r -> r.get(colIndex));
  }

  @Override
  public DimPoint calculateThisSize(Dim maxWidth) {
    List<Dim> columnWidths = this.getColumnWidths(maxWidth);
    Dim headerRowHeight = this.getHeaderRowHeight(columnWidths);
    List<Dim> rowHeights = this.getRowHeights(columnWidths);

    Dim y = ZERO;
    for (int r = -1; r < rowHeights.size(); r++) {
      Dim x = ZERO;
      List<WrapperElement> row = r == -1 ? this.headerCells : this.rows.get(r);
      Dim height = r == -1 ? headerRowHeight : rowHeights.get(r);
      for (int c = 0; c < columnWidths.size(); c++) {
        WrapperElement cell = row.get(c);
        DimPoint boxSize = new DimPoint(columnWidths.get(c), height);
        DimRect box = new DimRect(new DimPoint(ZERO, ZERO), boxSize);
        DimPoint actualSize = cell.calculateSize(boxSize.getX());
        DimRect relBox = ElementHelpers.alignElementInBox(actualSize, box, cell.getHorizontalAlignment(), cell.getVerticalAlignment());
        this.childrenRelBoxes.put(cell, relBox.withTranslation(new DimPoint(x, y)));

        x = x.plus(box.getWidth());
      }

      y = y.plus(height);
    }

    Dim height = Dim.max(this.minHeight, Dim.sum(rowHeights).plus(headerRowHeight));
    return new DimPoint(Dim.sum(columnWidths), height);
  }

  public static class Column {
    private final String header;
    /** The proportional width to aim for. */
    public final float width;
    /** If true, column will not expand if there is extra room in the table. */
    public final boolean fitWidth;

    public Column(String header, float width, boolean fitWidth) {
      this.header = header;
      this.width = width;
      this.fitWidth = fitWidth;
    }
  }
}
