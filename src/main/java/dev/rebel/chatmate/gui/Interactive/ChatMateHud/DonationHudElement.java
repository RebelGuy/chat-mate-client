package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.CustomGuiChat;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class DonationHudElement extends HudElement {
  private final static long ANIMATION_TIME = 1000;

  private final ChatMateHudStore chatMateHudStore;
  private final Runnable onDone;
  private final Consumer<PublicDonationData> onOpenDashboard;
  private final PublicDonationData donation;
  private final DonationCardElement donationCardElement;
  private AnimatedBool animation;

  public DonationHudElement(InteractiveContext context, IElement parent, ChatMateHudStore chatMateHudStore, Runnable onDone, Consumer<PublicDonationData> onOpenDashboard, PublicDonationData donation) {
    super(context, parent);
    super.setHudElementFilter(
        new HudFilters.HudFilterWhitelistNoScreen(),
        new HudFilters.HudFilterScreenWhitelist(CustomGuiChat.class),
        new HudFilters.HudFilterInteractiveScreenTypeBlacklist(InteractiveScreenType.DASHBOARD)
    );

    this.chatMateHudStore = chatMateHudStore;
    this.onDone = onDone;
    this.onOpenDashboard = onOpenDashboard;
    this.donation = donation;
    this.donationCardElement = new DonationCardElement(context, this, donation, this::onClickLink, this::onClose);
    this.animation = null; // only start the animation once the object is first shown
  }

  private void onAnimationComplete(boolean isShown) {
    if (!isShown) {
      this.chatMateHudStore.removeElement(this);
      this.onDone.run();
    }
  }

  private void onClose() {
    if (this.animation != null) {
      // slide the donation card back up
      this.animation.set(false);
    } else {
      // this should never happen... but just in case, we don't want an error!
      this.onAnimationComplete(false);
    }
  }

  private void onClickLink() {
    this.onOpenDashboard.accept(this.donation);
  }

  /** Smoothing function. */
  private float getYFrac() {
    float frac = this.animation == null ? 0 : this.animation.getFrac();

    // this one is quite nice - it starts somewhat fast, then fades out slowly
    return 1 - (float)Math.pow(Math.cos(frac * Math.PI / 2), 4);
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return Collections.list(this.donationCardElement);
  }

  @Override
  public void onError() {
    this.chatMateHudStore.removeElement(this);
    super.onError();
    this.onDone.run();
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    Dim height = box.getHeight();
    Dim newY = height.times(this.getYFrac()).minus(height);
    box = box.withTop(newY);
    super.setBoxUnsafe(box);
    this.donationCardElement.setBox(box);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.donationCardElement.calculateSize(maxContentSize);
  }

  @Override
  public void onRenderElement() {
    if (this.animation == null) {
      this.animation = new AnimatedBool(ANIMATION_TIME, false, this::onAnimationComplete);
      this.animation.set(true);
    }

    // we also check the position because it's possible the animation was not rendered to completion
    // (i.e. frac is 1, but box wasn't moved fully down yet)
    if (this.getYFrac() < 1 || super.getBox().getY().lt(super.ZERO)) {
      this.onInvalidateSize();
    }

    this.donationCardElement.renderElement();
  }
}
