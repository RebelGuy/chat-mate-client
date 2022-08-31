package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DonationHudElement extends HudElement {
  private final static long ANIMATION_TIME = 1000;

  private final ChatMateHudStore chatMateHudStore;
  private final Runnable onDone;
  private final Consumer<PublicDonationData> onOpenDashboard;
  private final PublicDonationData donation;
  private final DonationElement donationElement;
  private final AnimatedBool animation;

  public DonationHudElement(InteractiveContext context, IElement parent, ChatMateHudStore chatMateHudStore, Runnable onDone, Consumer<PublicDonationData> onOpenDashboard, PublicDonationData donation) {
    super(context, parent);

    this.chatMateHudStore = chatMateHudStore;
    this.onDone = onDone;
    this.onOpenDashboard = onOpenDashboard;
    this.donation = donation;
    this.donationElement = new DonationElement(context, this, donation, this::onClickLink, this::onDone);
    this.animation = new AnimatedBool(ANIMATION_TIME, false, this::onAnimationComplete);

    this.animation.set(true);
  }

  private void onAnimationComplete(boolean isShown) {
    if (!isShown) {
      this.chatMateHudStore.removeElement(this);
      this.onDone.run();
    }
  }

  private void onDone() {
    // slide the donation card back up
    this.animation.set(false);
  }

  private void onClickLink() {
    this.onOpenDashboard.accept(this.donation);
  }

  /** Smoothing function. */
  private float getYFrac() {
    float frac = this.animation.getFrac();

    // this one is quite nice - it starts somewhat fast, then fades out slowly
    return 1 - (float)Math.pow(Math.cos(frac * Math.PI / 2), 4);
  }

  @Override
  public @Nullable List<IElement> onGetChildren() {
    return Collections.list(this.donationElement);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    Dim height = box.getHeight();
    Dim newY = height.times(this.getYFrac()).minus(height);
    box = box.withTop(newY);
    super.setBoxUnsafe(box);
    this.donationElement.setBox(box);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.donationElement.calculateSize(maxContentSize);
  }

  @Override
  public void onRenderElement() {
    if (this.animation.getFrac() != 1) {
      this.onInvalidateSize();
    }

    this.donationElement.renderElement();
  }
}
