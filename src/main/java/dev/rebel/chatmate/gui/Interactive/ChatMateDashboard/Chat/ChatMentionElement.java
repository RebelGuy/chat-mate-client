package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Chat;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.EditableListElement.EditableListElement;
import dev.rebel.chatmate.gui.Interactive.EditableListElement.IEditableListAdapter;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.SCALE;

public class ChatMentionElement extends BlockElement implements IEditableListAdapter<String, ChatMentionElement.ChatMentionItem> {
  private final EditableListElement<String, ChatMentionItem> listElement;

  public ChatMentionElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    List<String> infoText = Collections.list(
        "Enter words or phrases for the livestream chat that should trigger a notification.",
        "By default, any string of letters containing any of the below values will result in a match.",
        "You can limit the match to the start/end of a word using '[' and ']', respectively.",
        "'*' is a wildcard character that matches any letter.",
        "Example: '[tes*]' will match any 4-letter word starting with 'tes'."
    );
    Collections.forEach(infoText, text -> super.addElement(new LabelElement(context, this)
        .setText(text)
        .setFontScale(SCALE)
        .setColour(Colour.GREY50)
        .setMargin(Layout.RectExtension.fromBottom(gui(0.75f)))
    ));

    List<String> initialValues = context.config.getChatMentionFilter().get();
    if (!Collections.any(initialValues)) {
      initialValues = Collections.list("");
    }
    this.listElement = new EditableListElement<>(context, this, initialValues, this)
        .setPadding(Layout.RectExtension.fromTop(gui(4)))
        .cast();

    super.addElement(this.listElement);
  }

  @Override
  public String onCreateItem(int newIndex) {
    if (this.listElement.getItems().size() > 0) {
      return "";
    }

    @Nullable String username = super.context.config.getLoginInfoEmitter().get().username;
    if (username == null) {
      return "";
    }

    return String.format("[%s]", username);
  }

  @Override
  public ChatMentionItem onCreateContents(String fromItem, int forIndex) {
    return new ChatMentionItem(super.context, this, fromItem, forIndex, this::onItemUpdated);
  }

  @Override
  public void onItemAdded(String addedItem, int addedAtIndex) {
    this.updateConfig();
  }

  @Override
  public void onItemRemoved(String removedItem, int removedAtIndex) {
    this.updateConfig();
  }

  @Override
  public void onIndexUpdated(String value, ChatMentionItem element, int previousIndex, int newIndex) {
    element.index = newIndex;
  }

  private void onItemUpdated(int index, String value) {
    this.listElement.replaceItem(index, value);
    this.updateConfig();
  }

  private void updateConfig() {
    List<String> sanitisedValues = Collections.map(this.listElement.getItems(), x -> x.toLowerCase());
    List<String> nonEmptyValues = Collections.filter(sanitisedValues, x -> x.length() > 0);
    List<String> uniqueValues = Collections.unique(nonEmptyValues);

    super.context.config.getChatMentionFilter().set(uniqueValues);
  }

  protected static class ChatMentionItem extends InlineElement {
    public int index;
    private final TextInputElement inputElement;
    private final BiConsumer<Integer, String> onUpdate;

    public ChatMentionItem(InteractiveScreen.InteractiveContext context, IElement parent, String value, int index, BiConsumer<Integer, String> onUpdate) {
      super(context, parent);

      this.index = index;
      this.onUpdate = onUpdate;
      this.inputElement = new TextInputElement(context, this, value)
          .setTextFormatter(this::onFormatText)
          .onTextChange(this::onTextChange)
          .setTabIndex(index)
          .cast();
      super.addElement(this.inputElement);

      super.setMinWidth(gui(30));
      super.setAllowShrink(true);
      super.setMargin(Layout.RectExtension.fromBottom(gui(1.5f)));
      super.setMaxWidth(gui(250));

      if (index > 0) {
        context.onSetFocus(this.inputElement);
      }
    }

    private void onTextChange(String value) {
      value = value.toLowerCase();
      this.onUpdate.accept(this.index, value);
    }

    // de-emphasise special characters, and use the normal font for everything else
    private List<Tuple2<String, Font>> onFormatText(String text) {
      List<Tuple2<String, Font>> result = new ArrayList<>();

      StringBuilder currentText = new StringBuilder();
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);

        if (c == '[' || c == ']' || c == '*') {
          if (currentText.length() > 0) {
            result.add(new Tuple2<>(currentText.toString(), new Font()));
            currentText = new StringBuilder();
          }

          result.add(new Tuple2<>(String.valueOf(c), new Font().withColour(Colour.GREY50)));
        } else {
          currentText.append(c);
        }
      }

      if (currentText.length() > 0) {
        result.add(new Tuple2<>(currentText.toString(), new Font()));
      }

      return result;
    }
  }
}
