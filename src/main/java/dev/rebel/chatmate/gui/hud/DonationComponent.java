package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.ChatComponentRenderer;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.services.util.Collections;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/** Represents a single donation that will be shown immediately, and handles user interaction. */
public class DonationComponent extends Box implements IHudComponent {
  private final static Colour BACKGROUND = new Colour(32, 32, 32);
  private final static Colour BORDER_COLOUR = BACKGROUND.withBrightness(4);
  private final static long ANIMATION_TIME = 1000;
  private final static long STATIC_TIME = 8000;
  private final static float MAX_WIDTH_GUI = 100;
  private final static float PADDING_GUI = 8;
  private final static float PADDING_SECTION_GUI = 4;
  private final static float CORNER_RADIUS = 6;
  private final static float BORDER_WIDTH = 2;

  private final DimFactory dimFactory;
  private final FontEngine fontEngine;
  private final ChatComponentRenderer chatComponentRenderer;
  private final PublicDonationData donation;
  private final Consumer<DonationComponent> onDone;
  private final Consumer<PublicDonationData> onLink;
  private final long start;

  public DonationComponent(DimFactory dimFactory, FontEngine fontEngine, ChatComponentRenderer chatComponentRenderer, PublicDonationData donation, Consumer<PublicDonationData> onClickLink, Consumer<DonationComponent> onDone) {
    super(dimFactory, dimFactory.zeroGui(), dimFactory.zeroGui(), dimFactory.zeroGui(), dimFactory.zeroGui(), false, false);

    this.dimFactory = dimFactory;
    this.fontEngine = fontEngine;
    this.chatComponentRenderer = chatComponentRenderer;
    this.donation = donation;
    this.onLink = onClickLink;
    this.onDone = onDone;
    this.start = new Date().getTime();
  }

  private float getYFrac() {
    // todo: for every body line, add another second?
    float distanceFromCentre = Math.abs((STATIC_TIME + ANIMATION_TIME * 2) / 2f - (float)(new Date().getTime() - this.start));
    return Math.min(distanceFromCentre / ANIMATION_TIME, 1);
  }

  // todo: implement timer bar, the close button, and the link button
  // probably easiest to port this to the interactive screen in some way...
  // is it time to redesign the HUD so it's an interactive screen? might need to make another type of interactive screen whose interactions can be toggled

  @Override
  public float getContentScale() {
    return 1;
  }

  @Override
  public boolean canRescaleContent() {
    return false;
  }

  @Override
  public void onRescaleContent(float newScale) {
    // no op
  }

  @Override
  public void render(RenderContext context) {
    if (new Date().getTime() - this.start > STATIC_TIME * ANIMATION_TIME * 2) {
      this.onDone.accept(this);
      return;
    }

    // get text dimensions
    Dim maxWidth = this.dimFactory.fromGui(MAX_WIDTH_GUI - 2 * PADDING_GUI);
    String formattedAmount = String.format("$%.2f", this.donation.amount);
    String title = String.format("%s has donated %s!", this.donation.name, formattedAmount);
    List<String> splitTitle = this.fontEngine.listFormattedStringToWidth(title, maxWidth);
    Dim titleWidth = Dim.max(Collections.map(splitTitle, str -> this.fontEngine.getStringWidthDim(str)));

    Dim bodyWidth = this.dimFactory.zeroGui();
    List<String> splitBody = new ArrayList<>();
    if (this.donation.message != null) {
      splitBody = this.fontEngine.listFormattedStringToWidth(this.donation.message, maxWidth);
      bodyWidth = Dim.max(Collections.map(splitBody, str -> this.fontEngine.getStringWidthDim(str)));
    }

    // set rects
    DimRect screen = this.dimFactory.getMinecraftRect();
    Dim lineHeight = this.fontEngine.FONT_HEIGHT_DIM;
    Dim innerWidth = Dim.max(titleWidth, bodyWidth);
    Dim innerHeight = this.dimFactory.fromGui(PADDING_SECTION_GUI).plus(lineHeight.times(splitTitle.size() + splitBody.size()));
    Dim innerX = screen.getCentre().getX().minus(innerWidth.over(2));
    Dim innerY = this.getHeight().times(this.getYFrac() - 1).plus(this.dimFactory.fromGui(PADDING_GUI));
    DimRect innerRect = new DimRect(innerX, innerY, innerWidth, innerHeight);
    DimRect outerRect = new RectExtension(this.dimFactory.fromGui(PADDING_GUI)).applyAdditive(innerRect);

    // render
    Dim borderWidth = this.dimFactory.fromGui(BORDER_WIDTH);
    Dim cornerRadius = this.dimFactory.fromGui(CORNER_RADIUS);
    RendererHelpers.drawRect(0, outerRect, BACKGROUND, borderWidth, BORDER_COLOUR, cornerRadius);

    Font titleFont = new Font().withBold(true).withColour(Colour.YELLOW);
    if (splitTitle.size() == 1) {
      Dim titleX = innerX.plus(innerWidth.minus(titleWidth).over(2));
      this.fontEngine.drawString(title, titleX, innerY, titleFont);
    } else {
      this.fontEngine.drawSplitString(title, innerX, innerY, innerWidth, titleFont);
    }

    if (splitBody.size() > 0) {
      Dim y = innerY.plus(this.dimFactory.fromGui(PADDING_SECTION_GUI).plus(lineHeight.times(splitTitle.size())));
      Font bodyFont = new Font();
      this.fontEngine.drawSplitString(this.donation.message, innerX, y, innerWidth, bodyFont);
    }
  }
}
