package dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.api.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.gui.CustomGuiChat;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudFilters;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.AppearanceElement;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.AppearanceElement.AppearanceModel;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.ContentSelectionElement.ContentModel;
import dev.rebel.chatmate.gui.Interactive.DonationHudModal.TimeframeSelectionElement.TimeframeModel;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

public class DonationHudElement extends HudElement {
  private final @Nullable TotalDonationsElement totalDonationsElement;
  private final @Nullable DonationListElement highestDonationsElement;
  private final @Nullable DonationListElement latestDonationsElement;
  private final IElement container;
  private final TimeframeModel timeframeModel;
  private final ContentModel contentModel;
  private final AppearanceModel appearanceModel;
  private final StatusService statusService;

  public DonationHudElement(InteractiveContext context, IElement parent, StatusService statusService, TimeframeModel timeframeModel, ContentModel contentModel, AppearanceModel appearanceModel) {
    super(context, parent);
    super.setMaxWidth(gui(130));
    super.setCanDrag(true);
    super.setCanScale(true);
    super.setHudElementFilter(
        new HudFilters.HudFilterWhitelistNoScreen(),
        new HudFilters.HudFilterScreenWhitelist(CustomGuiChat.class),
        new HudFilters.HudFilterInteractiveScreenTypeBlacklist(InteractiveScreenType.DASHBOARD, InteractiveScreenType.MODAL)
    );
    super.setDefaultPosition(context.dimFactory.getMinecraftRect().getCentre(), HudElement.Anchor.MIDDLE);

    // set anchors to middle because the box size might change significantly when content changes, and this keeps it somewhat uniform.
    super.setContentResizeAnchor(Anchor.MIDDLE);
    super.setScrollResizeAnchor(Anchor.MIDDLE);

    this.statusService = statusService;
    this.timeframeModel = timeframeModel;
    this.contentModel = contentModel;
    this.appearanceModel = appearanceModel;

    this.totalDonationsElement = contentModel.showTotal
        ? new TotalDonationsElement(context, this, this::getStartTime)
            .setTextAlignment(this.getTextAlignment())
            .setMargin(new RectExtension(ZERO, gui(4)))
            .cast()
        : null;
    this.highestDonationsElement = contentModel.showHighest
        ? new DonationListElement(context, this, this::getStartTime, () -> super.currentScale, contentModel.highestN, "Highest donations:", this::sortDonationsByAmount)
        .setTextAlignment(this.getTextAlignment())
        .setMargin(new RectExtension(ZERO, gui(4)))
        .cast()
        : null;
    this.latestDonationsElement = contentModel.showLatest
        ? new DonationListElement(context, this, this::getStartTime, () -> super.currentScale, contentModel.latestN, "Latest donations:", this::sortDonationsByTime)
        .setTextAlignment(this.getTextAlignment())
        .setMargin(new RectExtension(ZERO, gui(4)))
        .cast()
        : null;

    this.container = new BlockElement(context, this)
        .addElement(this.totalDonationsElement)
        .addElement(this.highestDonationsElement)
        .addElement(this.latestDonationsElement);
  }

  private List<PublicDonation> sortDonationsByAmount(List<PublicDonation> donations) {
    return Collections.reverse(Collections.orderBy(donations, d -> d.amount));
  }

  private List<PublicDonation> sortDonationsByTime(List<PublicDonation> donations) {
    return Collections.reverse(Collections.orderBy(donations, d -> d.time));
  }

  private long getStartTime() {
    if (this.timeframeModel.thisStreamOnly) {
      @Nullable PublicLivestreamStatus status = this.statusService.getLivestreamStatus();
      if (status == null) {
        return Long.MAX_VALUE;
      } else {
        return Math.min(
            status.getYoutubeStartTime() == null ? Long.MAX_VALUE : status.getYoutubeStartTime(),
            status.getTwitchStartTime() == null ? Long.MAX_VALUE : status.getTwitchStartTime()
        );
      }
    } else {
      return this.timeframeModel.since;
    }
  }

  private LabelElement.TextAlignment getTextAlignment() {
    if (this.appearanceModel.textAlignment == AppearanceElement.TextAlignment.LEFT) {
      return LabelElement.TextAlignment.LEFT;
    } else if (this.appearanceModel.textAlignment == AppearanceElement.TextAlignment.CENTRE) {
      return LabelElement.TextAlignment.CENTRE;
    } else if (this.appearanceModel.textAlignment == AppearanceElement.TextAlignment.RIGHT) {
      return LabelElement.TextAlignment.RIGHT;
    }

    return LabelElement.TextAlignment.LEFT;
  }

  @Override
  protected void onElementRescaled(float oldScale, float newScale) {
    if (this.totalDonationsElement != null) {
      this.totalDonationsElement.setScale(newScale);
    }
    if (this.highestDonationsElement != null) {
      this.highestDonationsElement.setScale(newScale);
    }
    if (this.latestDonationsElement != null) {
      this.latestDonationsElement.setScale(newScale);
    }
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return Collections.list(this.container);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.container.calculateSize(maxContentSize);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    this.container.setBox(box);

    LabelElement.TextAlignment textAlignment;
    if (this.appearanceModel.textAlignment == AppearanceElement.TextAlignment.AUTO) {
      if (super.autoAnchor == Anchor.TOP_LEFT || super.autoAnchor == Anchor.LEFT_CENTRE || super.autoAnchor == Anchor.BOTTOM_LEFT) {
        textAlignment = LabelElement.TextAlignment.LEFT;
      } else if (super.autoAnchor == Anchor.TOP_CENTRE || super.autoAnchor == Anchor.MIDDLE || super.autoAnchor == Anchor.BOTTOM_CENTRE) {
        textAlignment = LabelElement.TextAlignment.CENTRE;
      } else {
        textAlignment = LabelElement.TextAlignment.RIGHT;
      }

      if (this.totalDonationsElement != null) {
        this.totalDonationsElement.setTextAlignment(textAlignment);
      }
      if (this.highestDonationsElement != null) {
        this.highestDonationsElement.setTextAlignment(textAlignment);
      }
      if (this.latestDonationsElement != null) {
        this.latestDonationsElement.setTextAlignment(textAlignment);
      }
    }
  }

  @Override
  public void onRenderElement() {
    this.container.render(null);
  }
}
