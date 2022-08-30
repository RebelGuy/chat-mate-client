package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class DonationHudElement extends HudElement {
  private final ChatMateHudStore chatMateHudStore;
  private final Runnable onDone;
  private final Consumer<PublicDonationData> onOpenDashboard;
  private final PublicDonationData donation;
  private final DonationElement donationElement;

  public DonationHudElement(InteractiveContext context, IElement parent, ChatMateHudStore chatMateHudStore, Runnable onDone, Consumer<PublicDonationData> onOpenDashboard, PublicDonationData donation) {
    super(context, parent);

    this.chatMateHudStore = chatMateHudStore;
    this.onDone = onDone;
    this.onOpenDashboard = onOpenDashboard;
    this.donation = donation;
    this.donationElement = new DonationElement(context, this, donation, this::onClickLink, this::onDone);
  }

  private void onDone() {
    this.chatMateHudStore.removeElement(this);
    this.onDone.run();
  }

  private void onClickLink() {
    this.onOpenDashboard.accept(this.donation);
  }

  @Override
  public @Nullable List<IElement> onGetChildren() {
    return Collections.list(this.donationElement);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    this.donationElement.setBox(box);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.donationElement.calculateSize(maxContentSize);
  }

  @Override
  public void onRenderElement() {
    this.donationElement.renderElement();
  }
}
