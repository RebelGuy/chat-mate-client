package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimRect;
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
  private long lastTime;
  private long remaining;

  public DonationElement(InteractiveContext context, IElement parent, PublicDonationData donation, Runnable onClickLink, Runnable onDone) {
    super(context, parent, LayoutMode.BLOCK);
    super.setPadding(new RectExtension(gui(8)));
    super.setBorder(new RectExtension(gui(2)));
    super.setMaxWidth(gui(MAX_WIDTH_GUI));

    this.donation = donation;
    this.onLink = onClickLink;
    this.onDone = onDone;
    this.lastTime = new Date().getTime();
    this.remaining = LIFETIME;

    super.addElement(new DonationButtonsElement(context, this, onDone, onClickLink)
        .setMargin(new RectExtension(ZERO, gui(-4), gui(-8), ZERO)) // move above and to the right of the main content
    );

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

  @Override
  protected void renderElement() {
    long now = new Date().getTime();
    if (!super.isHovering()) {
      long diff = now - this.lastTime;

      // a large diff probably means that we weren't rendering this element, so artificially pause the bar
      if (diff < 500) {
        this.remaining -= diff;
      }
    }
    this.lastTime = now;

    if (remaining <= 0 && this.onDone != null) {
      this.onDone.run();
      this.onDone = null;
    }

    DimFactory dimFactory = super.context.dimFactory;
    Dim cornerRadius = dimFactory.fromGui(CORNER_RADIUS);
    // todo: drawRect should accept a RectExtension border (while you're at it, also refactor those method overloads...)
    RendererHelpers.drawRect(0, super.getPaddingBox(), BACKGROUND, super.getBorder().left, BORDER_COLOUR, cornerRadius, gui(4), Colour.BLACK);
    this.drawTimerBar();
    super.renderElement();
  }

  private void drawTimerBar() {
    Dim x = super.getContentBox().getX();
    Dim w = super.getContentBox().getWidth();
    Dim h = gui(2);
    Dim y = super.getPaddingBox().getBottom().minus(h);
    DimRect box = new DimRect(x, y, w, h);

    Dim cornerRadius = gui(1f);
    float frac = (float)Math.max(0, (double)this.remaining / LIFETIME);

    // the min is a circle with radius `cornerRadius`
    Dim barWidth = Dim.max(cornerRadius.times(2), w.times(frac));

    // the empty bar looks weird, so fade it out
    float alpha = Math.min(1, w.times(frac).over(cornerRadius.times(2)));

    Colour backgroundColour = Colour.DKGREY.withAlpha(alpha);
    Colour barColour = Colour.CYAN.withBrightness(0.5f).withAlpha(alpha);
    RendererHelpers.drawRect(0, box, backgroundColour, null, null, cornerRadius);
    RendererHelpers.drawRect(0, box.withWidth(barWidth), barColour, null, null, cornerRadius);
  }

  private class DonationButtonsElement extends ContainerElement {
    public DonationButtonsElement(InteractiveContext context, IElement parent, Runnable onClickClose, Runnable onClickLink) {
      super(context, parent, LayoutMode.INLINE);

      super.setHorizontalAlignment(Layout.HorizontalAlignment.RIGHT);

      ButtonElement.IconButtonElement linkButton = new ButtonElement.IconButtonElement(context, this)
          .setImage(Asset.GUI_LINK_ICON)
          .setMaxContentWidth(gui(6))
          .setOnClick(onClickLink)
          .setBorder(new RectExtension(ZERO))
          .setPadding(new RectExtension(ZERO))
          .setMargin(new RectExtension(gui(2)))
          .cast();
      linkButton.image.setPadding(new RectExtension(ZERO));

      ButtonElement.IconButtonElement closeButton = new ButtonElement.IconButtonElement(context, this)
          .setImage(Asset.GUI_CLEAR_ICON)
          .setMaxContentWidth(gui(6))
          .setOnClick(onClickClose)
          .setBorder(new RectExtension(ZERO))
          .setPadding(new RectExtension(ZERO))
          .setMargin(new RectExtension(gui(2)))
          .cast();
      closeButton.image.setPadding(new RectExtension(ZERO));

      super.addElement(linkButton).addElement(closeButton);
    }
  }
}
