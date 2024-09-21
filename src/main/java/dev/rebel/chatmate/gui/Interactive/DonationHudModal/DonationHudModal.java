package dev.rebel.chatmate.gui.Interactive.DonationHudModal;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud.DonationHudElement;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.AppearanceElement.AppearanceModel;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.ContentSelectionElement.ContentModel;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.TimeframeSelectionElement.TimeframeModel;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DonationHudModal extends ModalElement {
  private final ChatMateHudStore chatMateHudStore;
  private final StatusService statusService;

  private final ContainerElement container;

  private final TimeframeSelectionElement timeframeSelectionElement;
  private final ContentSelectionElement contentSelectionElement;
  private final AppearanceElement appearanceElement;
  private final TextButtonElement deleteButton;

  public DonationHudModal(InteractiveContext context, InteractiveScreen parent, ChatMateHudStore chatMateHudStore, StatusService statusService) {
    super(context, parent);
    this.chatMateHudStore = chatMateHudStore;
    this.statusService = statusService;

    super.setTitle("Create Donation Element");
    super.setSubmitText("Create");
    super.setCloseText("Cancel");
    super.setAutoScrollBody(true);

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
    this.deleteButton = new TextButtonElement(context, this)
        .setText("Delete existing element")
        .setOnClick(this::onDeleteExistingElement)
        .setVisible(this.hasExistingElement())
        .setMargin(new RectExtension(ZERO, gui(5)))
        .setHorizontalAlignment(Layout.HorizontalAlignment.CENTRE)
        .cast();

    this.container.addElement(this.timeframeSelectionElement);
    this.container.addElement(this.contentSelectionElement);
    this.container.addElement(this.appearanceElement);
    this.container.addElement(this.deleteButton);
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

    this.chatMateHudStore.addElement((context, parent) -> new DonationHudElement(context, parent, this.statusService, timeframeModel, contentModel, appearanceModel));
    onSuccess.run();
    super.onCloseScreen();
  }

  private boolean hasExistingElement() {
    return Collections.any(this.chatMateHudStore.getElements(), el -> el instanceof DonationHudElement);
  }

  private void onDeleteExistingElement() {
    Collections.filter(this.chatMateHudStore.getElements(), el -> el instanceof DonationHudElement).forEach(this.chatMateHudStore::removeElement);
    this.deleteButton.setVisible(false);
  }
}
