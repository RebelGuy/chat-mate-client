package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud.DonationHudElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.ModalElement;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DonationHudModal extends ModalElement {
  private final ChatMateHudStore chatMateHudStore;

  public DonationHudModal(InteractiveContext context, InteractiveScreen parent, ChatMateHudStore chatMateHudStore) {
    super(context, parent);
    this.chatMateHudStore = chatMateHudStore;

    super.setTitle("Create Donation Element");
    super.setSubmitText("Create");
    super.setCloseText("Cancel");
  }

  @Override
  protected @Nullable Boolean validate() {
    return true;
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    this.chatMateHudStore.addElement((context, parent) -> new DonationHudElement(context, parent, 0));
    onSuccess.run();
    super.onCloseScreen();
  }
}
