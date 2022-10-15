package dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Memoiser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// todo: can make generic element, ListDonationsElement, and just provide the conditions for the list.
public class HighestDonationsElement extends ContainerElement {
  private final LabelElement header;
  private final List<LabelElement> listedElements;
  private final Supplier<Long> startTime;
  private final int count;
  private final Memoiser memoiser;

  public HighestDonationsElement(InteractiveContext context, IElement parent, Supplier<Long> startTime, int count) {
    super(context, parent, LayoutMode.BLOCK);
    this.startTime = startTime;

    this.header = new LabelElement(context, this)
        .setText("Latest donations:")
        .setSizingMode(SizingMode.FILL)
        .cast();
    this.listedElements = new ArrayList<>();
    this.count = count;
    this.memoiser = new Memoiser();
  }

  public void setScale(float scale) {
    this.header.setFontScale(scale);
    this.listedElements.forEach(el -> el.setFontScale(scale));
  }

  public HighestDonationsElement setTextAlignment(LabelElement.TextAlignment textAlignment) {
    this.header.setAlignment(textAlignment);
    this.listedElements.forEach(el -> el.setAlignment(textAlignment));
    return this;
  }

  private LabelElement donationToElement(PublicDonation donation, int index) {
    String name = donation.linkedUser != null ? donation.linkedUser.userInfo.channelName : donation.name;
    return new LabelElement(super.context, this)
        .setText(String.format("%d: %s by %s", index + 1, donation.formattedAmount, name))
        .setOverflow(LabelElement.TextOverflow.SPLIT)
        .setMargin(new RectExtension(gui(10), ZERO, ZERO, ZERO))
        .setSizingMode(SizingMode.FILL)
        .cast();
  }

  private void setDonationElements() {
    // we are not using the return type - we only care about running the function
    this.memoiser.memoise("setDonationElements", () -> {
      super.context.renderer.runSideEffect(() -> {
        long startTime = this.startTime.get();
        List<PublicDonation> donations = Collections.filter(super.context.donationApiStore.getDonations(), d -> d.time >= startTime);
        donations = Collections.reverse(Collections.orderBy(donations, d -> d.amount));
        donations = donations.subList(0, Math.min(donations.size(), this.count));

        super.clear();
        this.listedElements.clear();
        if (donations.size() > 0) {
          super.addElement(this.header);
          Collections.map(donations, this::donationToElement).forEach(el -> { super.addElement(el); this.listedElements.add(el); });
        }
      });

      return null;
    }, super.context.donationApiStore.getDonations(), this.startTime.get());
  }

  @Override
  protected void renderElement() {
    // this is not particularly efficient - see CHAT-143
    this.setDonationElements();
    super.renderElement();
  }
}
