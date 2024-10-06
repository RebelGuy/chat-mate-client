package dev.rebel.chatmate.gui.Interactive.BulkSendMessagesModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Objects;
import dev.rebel.chatmate.util.TaskWrapper;
import net.minecraft.util.ChatComponentText;
import scala.tools.nsc.doc.base.comment.Link;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class BulkSendMessagesModal extends ModalElement {
  private @Nullable List<String> loadedText = null;
  private final LabelElement parsedLinesLabel;

  public BulkSendMessagesModal(InteractiveContext context, InteractiveScreen parent) {
    super(context, parent);

    super.setTitle("Bulk-Send Messages");
    super.setSubmitText("Send");
    super.setCloseText("Cancel");

    ContainerElement container = new BlockElement(context, this);
    super.setBody(container);

    IElement readFromClipboardButton = new TextButtonElement(context, this)
        .setText("Read text from clipboard")
        .setOnClick(this::onReadFromClipboard)
        .setPadding(new RectExtension(gui(2)));
    this.parsedLinesLabel = new LabelElement(context, this)
        .setOverflow(TextOverflow.SPLIT)
        .setPadding(new RectExtension(gui(2)))
        .setMargin(new RectExtension(ZERO, gui(4)))
        .cast();

    container.addElement(readFromClipboardButton);
    container.addElement(this.parsedLinesLabel);
  }

  private void onReadFromClipboard() {
    String rawText = super.context.clipboardService.getClipboardString();
    if (rawText == null) {
      this.loadedText = null;
      return;
    }

    this.loadedText = Collections.list(rawText.split("\n"));

    String warning = "";
    if (Collections.any(this.loadedText, line -> line.length() > 100)) {
      warning = " One or more lines are too long and will be truncated.";
    }
    this.parsedLinesLabel.setText(String.format("%d messages will be posted to the server with a delay of 500ms.%s", this.loadedText.size(), warning));
  }

  @Override
  protected @Nullable Boolean validate() {
    return Collections.any(this.loadedText);
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    Queue<String> lines = new LinkedList<>();
    if (this.loadedText != null) {
      lines.addAll(this.loadedText);
    }

    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TaskWrapper(() -> {
      @Nullable String msg = lines.poll();
      if (msg == null) {
        timer.cancel();
      } else {
        super.context.minecraft.thePlayer.sendChatMessage(msg);
      }
    }), 0, 500);

    onSuccess.run();
  }
}
