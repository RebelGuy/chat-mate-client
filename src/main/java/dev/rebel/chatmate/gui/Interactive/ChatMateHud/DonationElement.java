package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.util.TextHelpers;

import javax.annotation.Nullable;
import java.util.Date;

public class DonationElement extends ContainerElement {
  private final static Colour BACKGROUND = new Colour(32, 32, 32);
  private final static Colour BORDER_COLOUR = BACKGROUND.withBrightness(4);

  private final static long LIFETIME = 10000;
  private final static float MAX_WIDTH_GUI = 220;
  private final static float CORNER_RADIUS = 6;

  private final PublicDonationData donation;
  private @Nullable Runnable onDone;
  private final Runnable onLink;
  private final long start;

  public DonationElement(InteractiveContext context, IElement parent, PublicDonationData donation, Runnable onClickLink, Runnable onDone) {
    super(context, parent, LayoutMode.BLOCK);
    super.setPadding(new RectExtension(gui(8)));
    super.setBorder(new RectExtension(gui(2)));
    super.setMaxWidth(gui(MAX_WIDTH_GUI));

    this.donation = donation;
    this.onLink = onClickLink;
    this.onDone = onDone;
    this.start = new Date().getTime();

    String formattedAmount = String.format("$%.2f", this.donation.amount);
    String title = String.format("%s has donated %s!", this.donation.name, formattedAmount);
    Font titleFont = new Font().withBold(true).withColour(Colour.YELLOW).withShadow(new Shadow(context.dimFactory));
    super.addElement(new LabelElement(context, this)
        .setText(title)
        .setFont(titleFont)
        .setOverflow(TextOverflow.SPLIT)
        .setAlignment(TextAlignment.CENTRE)
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, this.donation.message == null ? ZERO : gui(4)))
    );

    Font messageFont = new Font()
        .withShadow(new Shadow(context.dimFactory))
        .withItalic(this.donation.message == null);
    super.addElement(new LabelElement(context, this)
        .setText(TextHelpers.isNullOrEmpty(this.donation.message) ? "No message" : this.donation.message)
        .setFont(messageFont)
        .setFontScale(0.75f)
        .setOverflow(TextOverflow.SPLIT)
    );
  }

  // todo: implement timer bar (pauses on hover), the close button, and the link button (only shown on hover)

  @Override
  protected void renderElement() {
    if (new Date().getTime() - this.start > LIFETIME && this.onDone != null) {
      this.onDone.run();
      this.onDone = null;
    }

    DimFactory dimFactory = super.context.dimFactory;
    Dim cornerRadius = dimFactory.fromGui(CORNER_RADIUS);
    // todo: drawRect should accept a RectExtension border (while you're at it, also refactor those method overloads...)
    RendererHelpers.drawRect(0, super.getPaddingBox(), BACKGROUND, super.getBorder().left, BORDER_COLOUR, cornerRadius);
    super.renderElement();
  }
}
