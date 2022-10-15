package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud.DonationHudElement;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.TimeframeSelectionElement.TimeframeModel;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DonationHudModal extends ModalElement {
  private final ChatMateHudStore chatMateHudStore;

  private final ContainerElement container;

  private final TimeframeSelectionElement timeframeSelectionElement;

  public DonationHudModal(InteractiveContext context, InteractiveScreen parent, ChatMateHudStore chatMateHudStore) {
    super(context, parent);
    this.chatMateHudStore = chatMateHudStore;

    super.setTitle("Create Donation Element");
    super.setSubmitText("Create");
    super.setCloseText("Cancel");

    this.container = new BlockElement(context, this);
    super.setBody(this.container);

    this.timeframeSelectionElement = new TimeframeSelectionElement(context, this);

    this.container.addElement(this.timeframeSelectionElement);
  }

  @Override
  protected @Nullable Boolean validate() {
    return true;
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    TimeframeModel timeframeModel = this.timeframeSelectionElement.getModel();

    this.chatMateHudStore.addElement((context, parent) -> new DonationHudElement(context, parent, 0));
    onSuccess.run();
    super.onCloseScreen();
  }
}
