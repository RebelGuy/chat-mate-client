package dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.util.Collections;

import java.util.List;
import java.util.function.Supplier;

public class TotalDonationsElement extends ContainerElement {
  private final LabelElement totalDonationsElement;
  private final Supplier<Long> startTime;

  public TotalDonationsElement(InteractiveContext context, IElement parent, Supplier<Long> startTime) {
    super(context, parent, LayoutMode.BLOCK);
    super.setSizingMode(SizingMode.FILL);
    this.startTime = startTime;

    this.totalDonationsElement = new LabelElement(context, this)
        .setText(this.getTotalDonationText())
        .setFont(new Font().withShadow(new Shadow(context.dimFactory)))
        .setOverflow(LabelElement.TextOverflow.SPLIT)
        .setSizingMode(SizingMode.FILL)
        .cast();

    super.addElement(this.totalDonationsElement);
  }

  public void setScale(float scale) {
    this.totalDonationsElement.setFontScale(scale);
  }

  public TotalDonationsElement setTextAlignment(LabelElement.TextAlignment textAlignment) {
    this.totalDonationsElement.setAlignment(textAlignment);
    return this;
  }

  @Override
  protected void renderElement() {
    // this is not particularly efficient - see CHAT-143
    String totalDonationText = this.getTotalDonationText();
    this.totalDonationsElement.setText(totalDonationText);

    super.renderElement();
  }

  private String getTotalDonationText() {
    float totalDonations = 0;
    List<PublicDonation> donations = Collections.filter(super.context.donationApiStore.getData(), d -> d.refundedAt == null);
    for (PublicDonation donation : donations) {
      if (donation.time >= this.startTime.get()) {
        totalDonations += donation.amount;
      }
    }

    return String.format("Total donations: A$%.2f", totalDonations);
  }
}
