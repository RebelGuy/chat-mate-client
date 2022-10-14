package dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHud;

import dev.rebel.chatmate.gui.Interactive.BlockElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudElement;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

public class DonationHudElement extends HudElement {
  private final TotalDonationsElement totalDonationsElement;
  private final IElement container;

  public DonationHudElement(InteractiveContext context, IElement parent, long startTime) {
    super(context, parent);
    super.setMaxWidth(gui(200));
    super.setCanDrag(true);
    super.setCanScale(true);

    this.totalDonationsElement = new TotalDonationsElement(context, this, startTime);

    this.container = new BlockElement(context, this)
        .addElement(this.totalDonationsElement);
  }

  @Override
  protected void onElementRescaled(float oldScale, float newScale) {
    this.totalDonationsElement.setScale(newScale);
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
    if (super.autoAnchor == Anchor.TOP_LEFT || super.autoAnchor == Anchor.LEFT_CENTRE || super.autoAnchor == Anchor.BOTTOM_LEFT) {
      textAlignment = LabelElement.TextAlignment.LEFT;
    } else if (super.autoAnchor == Anchor.TOP_CENTRE || super.autoAnchor == Anchor.MIDDLE || super.autoAnchor == Anchor.BOTTOM_CENTRE) {
      textAlignment = LabelElement.TextAlignment.CENTRE;
    } else {
      textAlignment = LabelElement.TextAlignment.RIGHT;
    }

    this.totalDonationsElement.setTextAlignment(textAlignment);
  }

  @Override
  public void onRenderElement() {
    this.container.render(null);
  }
}
