package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud.DonationHudElement;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.AppearanceElement.AppearanceModel;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.ContentSelectionElement.ContentModel;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.TimeframeSelectionElement.TimeframeModel;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DonationHudModal extends ModalElement {
  private final ChatMateHudStore chatMateHudStore;

  private final ContainerElement container;

  private final TimeframeSelectionElement timeframeSelectionElement;
  private final ContentSelectionElement contentSelectionElement;
  private final AppearanceElement appearanceElement;

  public DonationHudModal(InteractiveContext context, InteractiveScreen parent, ChatMateHudStore chatMateHudStore) {
    super(context, parent);
    this.chatMateHudStore = chatMateHudStore;

    super.setTitle("Create Donation Element");
    super.setSubmitText("Create");
    super.setCloseText("Cancel");

    this.container = new BlockElement(context, this);
    super.setBody(this.container);

    this.timeframeSelectionElement = new TimeframeSelectionElement(context, this)
        .setMargin(new RectExtension(ZERO, gui(4)))
        .cast();
    this.contentSelectionElement = new ContentSelectionElement(context, this)
        .setMargin(new RectExtension(ZERO, gui(4)))
        .cast();
    this.appearanceElement = new AppearanceElement(context, this)
        .setMargin(new RectExtension(ZERO, gui(4)))
        .cast();

    this.container.addElement(this.timeframeSelectionElement);
    this.container.addElement(this.contentSelectionElement);
    this.container.addElement(this.appearanceElement);
  }

  @Override
  protected @Nullable Boolean validate() {
    // for some reason the modal calls `validate` immediately, but I don't care enough to fix it so here's a workaround...
    return super.isInitialised() && this.timeframeSelectionElement.validate() && this.contentSelectionElement.validate() && this.appearanceElement.validate();
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    TimeframeModel timeframeModel = this.timeframeSelectionElement.getModel();
    ContentModel contentModel = this.contentSelectionElement.getModel();
    AppearanceModel appearanceModel = this.appearanceElement.getModel();

    this.chatMateHudStore.addElement((context, parent) -> new DonationHudElement(context, parent, 0));
    onSuccess.run();
    super.onCloseScreen();
  }
}
