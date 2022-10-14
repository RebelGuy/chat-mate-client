package dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;

public class TotalDonationsElement extends ContainerElement {
  private final LabelElement totalDonationsElement;
  private final Long startTime;

  public TotalDonationsElement(InteractiveContext context, IElement parent, Long startTime) {
    super(context, parent, LayoutMode.BLOCK);
    this.startTime = startTime;

    this.totalDonationsElement = new LabelElement(context, this)
        .setText(this.getTotalDonationText())
        .setOverflow(LabelElement.TextOverflow.SPLIT);

    super.addElement(this.totalDonationsElement);
  }

  public void setScale(float scale) {
    this.totalDonationsElement.setFontScale(scale);
  }

  public void setTextAlignment(LabelElement.TextAlignment textAlignment) {
    this.totalDonationsElement.setAlignment(textAlignment);
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
    for (PublicDonation donation : super.context.donationApiStore.getDonations()) {
      if (donation.time > this.startTime) {
        totalDonations += donation.amount;
      }
    }

    return String.format("Total donations: A$%.2f", totalDonations);
  }
}
