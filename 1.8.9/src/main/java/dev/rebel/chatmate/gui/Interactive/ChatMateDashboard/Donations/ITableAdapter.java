package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.models.Dim;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

// can't add to the `TableElement.java` file because java complains about circular inheritance (??)
public interface ITableAdapter<T> {
  Layout.RectExtension getCellPadding();
  List<Dim> getColumnWidths();
  @Nullable
  Consumer<T> getOnClickHandler();
}
