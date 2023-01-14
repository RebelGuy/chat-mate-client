package dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.Memoiser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DonationListElement extends ContainerElement {
  private final LabelElement header;
  private final List<LabelElement> listedElements;
  private final Supplier<Long> startTime;
  private final Supplier<Float> scale;
  private final Function<List<PublicDonation>, List<PublicDonation>> donationSorter;
  private final int count;
  private final Memoiser memoiser;

  public DonationListElement(InteractiveContext context, IElement parent, Supplier<Long> startTime, Supplier<Float> scale, int count, String header, Function<List<PublicDonation>, List<PublicDonation>> donationSorter) {
    super(context, parent, LayoutMode.BLOCK);
    this.startTime = startTime;
    this.scale = scale;
    this.donationSorter = donationSorter;

    this.header = new LabelElement(context, this)
        .setText(header)
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

  public DonationListElement setTextAlignment(LabelElement.TextAlignment textAlignment) {
    this.header.setAlignment(textAlignment);
    this.listedElements.forEach(el -> el.setAlignment(textAlignment));
    return this;
  }

  private LabelElement donationToElement(PublicDonation donation, int index) {
    String name = donation.linkedUser != null ? donation.linkedUser.channelInfo.channelName : donation.name;
    return new LabelElement(super.context, this)
        .setText(String.format("%d: %s by %s", index + 1, donation.formattedAmount, name))
        .setOverflow(LabelElement.TextOverflow.SPLIT)
        .setFontScale(this.scale.get())
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
        donations = donationSorter.apply(donations);
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
