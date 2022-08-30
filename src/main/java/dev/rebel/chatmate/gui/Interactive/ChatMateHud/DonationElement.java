package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DonationElement extends ContainerElement {
  private final static Colour BACKGROUND = new Colour(32, 32, 32);
  private final static Colour BORDER_COLOUR = BACKGROUND.withBrightness(4);
  private final static long ANIMATION_TIME = 1000;
  private final static long STATIC_TIME = 8000;
  private final static float MAX_WIDTH_GUI = 100;
  private final static float CORNER_RADIUS = 6;

  private final PublicDonationData donation;
  private final Runnable onDone;
  private final Runnable onLink;
  private final long start;

  public DonationElement(InteractiveContext context, IElement parent, PublicDonationData donation, Runnable onClickLink, Runnable onDone) {
    super(context, parent, LayoutMode.BLOCK);
    super.setPadding(new RectExtension(gui(8)));
    super.setBorder(new RectExtension(gui(2)));

    this.donation = donation;
    this.onLink = onClickLink;
    this.onDone = onDone;
    this.start = new Date().getTime();

    String formattedAmount = String.format("$%.2f", this.donation.amount);
    String title = String.format("%s has donated %s!", this.donation.name, formattedAmount);
    Font titleFont = new Font().withBold(true).withColour(Colour.YELLOW).withShadow(new Shadow(super.context.dimFactory));
    super.addElement(new LabelElement(context, this)
        .setText(title)
        .setFont(titleFont)
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(4)))
    );
  }

  // todo: move animation outside - this element should only be the static donation card
  private float getYFrac() {
    // todo: for every body line, add another second?
    float distanceFromCentre = Math.abs((STATIC_TIME + ANIMATION_TIME * 2) / 2f - (float)(new Date().getTime() - this.start));
    return Math.min(distanceFromCentre / ANIMATION_TIME, 1);
  }

  // todo: implement timer bar (pauses on hover), the close button, and the link button (only shown on hover)

  @Override
  protected void renderElement() {
    if (new Date().getTime() - this.start > STATIC_TIME + ANIMATION_TIME * 2) {
      this.onDone.run();
      return;
    }

    // constantly invalidate size during the animation phase
    if (this.getYFrac() < 1) {
      super.onInvalidateSize();
    }

    DimFactory dimFactory = super.context.dimFactory;
    Dim cornerRadius = dimFactory.fromGui(CORNER_RADIUS);
    // todo: drawRect should accept a RectExtension border (while you're at it, also refactor those method overloads...)
    RendererHelpers.drawRect(0, super.getPaddingBox(), BACKGROUND, super.getBorder().left, BORDER_COLOUR, cornerRadius);
    super.renderElement();
  }
}
