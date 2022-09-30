package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.services.DonationService;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.EnumHelpers;
import dev.rebel.chatmate.store.RankApiStore;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IChatComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dev.rebel.chatmate.gui.Interactive.RendererHelpers.addVertex;
import static org.lwjgl.opengl.GL11.GL_POLYGON;

public class UserNameChatComponent extends ChatComponentBase {
  private static final char CHAR_SECTION_SIGN = 167;

  private final FontEngine fontEngine;
  private final DimFactory dimFactory;
  private final DonationService donationService;
  private final RankApiStore rankApiStore;
  public final int userId;
  private final Font baseFont;
  private final boolean useEffects;
  private final double tOffset;

  private String displayName;
  private double lastT;
  private List<Particle> particles;

  public UserNameChatComponent(FontEngine fontEngine, DimFactory dimFactory, DonationService donationService, RankApiStore rankApiStore, int userId, Font baseFont, String displayName, boolean useEffects) {
    super();
    this.fontEngine = fontEngine;
    this.dimFactory = dimFactory;
    this.donationService = donationService;
    this.rankApiStore = rankApiStore;
    this.userId = userId;
    this.baseFont = baseFont;
    this.displayName = displayName;
    this.useEffects = useEffects;
    this.tOffset = userId * 22 / 7.0;
    this.lastT = 0;
    this.particles = new ArrayList<>();
  }

  public void setDisplayName(String formattedName) {
    // do some sanitising where we remove duplicate formatting codes. there was a problem where callers would tack on a `r` code every frame
    StringBuilder builder = new StringBuilder();
    char lastFormattingChar = 0;
    for (int i = 0; i < formattedName.length(); i++) {
      char c = formattedName.charAt(i);

      if (c == CHAR_SECTION_SIGN && i < formattedName.length()) {
        char nextFormattingChar = formattedName.charAt(i + 1);
        if (nextFormattingChar != lastFormattingChar) {
          builder.append(c).append(nextFormattingChar);
        }
        lastFormattingChar = nextFormattingChar;
        i++;

      } else {
        builder.append(c);
      }
    }

    this.displayName = builder.toString();
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public Dim getWidth() {
    return this.fontEngine.getStringWidthDim(this.displayName, this.baseFont);
  }

  /** Returns the width. */
  public Dim renderComponent(Dim x, Dim y, int alphaInt, DimRect chatRect) {
    float alpha = alphaInt / 255f;
    Dim newX;
    if (this.useEffects && this.donationService.shouldShowDonationEffect(this.userId)) {
      newX = this.renderEffect(x, y, alpha, chatRect);
    } else {
      newX = this.renderDefault(x, y, alpha);
    }

    return newX.minus(x);
  }

  @Override
  public String getUnformattedTextForChat() {
    return this.displayName;
  }

  @Override
  public IChatComponent createCopy() {
    return this;
  }

  private Dim renderDefault(Dim x, Dim y, float alpha) {
    return this.fontEngine.drawString(this.displayName, x, y, this.baseFont.withColour(this.baseFont.getColour().withAlpha(alpha)));
  }

  private Dim renderEffect(Dim x, Dim y, float alpha, DimRect chatRect) {
    double t = ((double)new Date().getTime() / 1000) + this.tOffset;
    double deltaT = t - this.lastT;
    this.lastT = t;

    // particle effect (member)
    if (Collections.map(this.rankApiStore.getCurrentUserRanks(this.userId), r -> r.rank.name).contains(RankName.MEMBER)) {
      // draw this below the text because it looks nicer that way
      DimRect rect = new DimRect(x, y, this.getWidth(), this.fontEngine.FONT_HEIGHT_DIM);
      this.renderParticles(t, deltaT, alpha, rect, chatRect);
    }

    String fullString = this.displayName;
    for (int i = 0; i < fullString.length(); i++) {
      String c = fullString.substring(i, i + 1);
      if (c.charAt(0) == CHAR_SECTION_SIGN) {
        i++;
        continue;
      }

      // rainbow effect (all donators)
      // make sure we don't get noticeable repeats
      float r = ((float)Math.sin(t / 2) + 1) / 2;
      float g = ((float)Math.sin(t / Math.E) + 1) / 2;
      float b = ((float)Math.sin(t / Math.PI) + 1) / 2;

      // wave effect (supporter)
      Dim yOffset = this.getVerticalOffset(t);

      GL11.glPushMatrix();
      GL11.glTranslated(0, 0, 10);
      Font font = new Font(this.baseFont).withColour(new Colour(r, g, b, alpha));
      x = this.fontEngine.drawString(c, x, y.plus(yOffset), font);
      GL11.glPopMatrix();
      t -= 0.15; // animates the colours from left to right
    }

    return x;
  }

  private Dim getVerticalOffset(double t) {
    if (Collections.map(this.rankApiStore.getCurrentUserRanks(this.userId), r -> r.rank.name).contains(RankName.SUPPORTER)) {
      // multiplying the sine waves has the effect of only surpassing the threshold only rarely, as opposed to once per period
      return this.dimFactory.fromGui((float)Math.max(0, (Math.sin(t * 1.5f) * Math.sin(t / 6) - 0.95))).times(-25);
    } else {
      return this.dimFactory.zeroGui();
    }
  }

  private void renderParticles(double t, double deltaT, float alpha, DimRect componentRect, DimRect chatRect) {
    double particlesPerSecond = 2;

    // make sure we don't spawn particles outside the chat area, and scale the chance of spawning a new particle if the component's rect was clipped
    DimRect clippedRect = componentRect.clamp(chatRect);
    float ratio = clippedRect.getAreaGui() / componentRect.getAreaGui();
    boolean spawnParticle = Math.random() < particlesPerSecond * deltaT * ratio;

    if (spawnParticle) {
      this.particles.add(new Particle(this.dimFactory, clippedRect, t));
    }

    this.particles = Collections.filter(this.particles, p -> !p.isComplete(t));
    this.particles.forEach(p -> p.render(t, deltaT, alpha));
  }

  private static Colour getRandomFlameColour() {
    float targetR = 226 / 255f;
    float targetG = 88 / 255f;
    float targetB = 34 / 255f;
    float brightness = Math.min(1, (float)Math.random() * 2);
    float r = Math.min(1, (1 + (float)(Math.random() * 0.4 - 0.15)) * targetR); // tending towards more red
    float g = (1 + (float)(Math.random() * 1 - 0.3)) * targetG; // higher values make it more yellow
    float b = (1 + (float)(Math.random() * 0.4 - 0.2)) * targetB;
    return new Colour(r, g, b).withBrightness(brightness);
  }

  private static class Particle {
    private final DimFactory dimFactory;
    private final DimRect rect;
    private final float mass; // determines size and momentum
    private final double start;
    private final double lifetime;
    private final Colour colour;
    private final int corners;

    private DimPoint velocity; // gui units per second
    private DimPoint position; // absolute gui units

    public Particle(DimFactory dimFactory, DimRect rect, double t) {
      this.dimFactory = dimFactory;
      this.rect = rect;
      this.start = t;

      this.mass = (float)Math.random() / 2 + 0.5f;
      this.lifetime = Math.random() * 7 + 3;

      this.colour = getRandomFlameColour();
      this.corners = (int)(Math.random() * 7 + 3);

      float velocityScale = 3;
      this.velocity = new DimPoint(
          dimFactory.fromGui(((float)Math.random() * 2 - 1) * velocityScale),
          dimFactory.fromGui(((float)Math.random() * 2 - 1) * velocityScale)
      );
      this.position = rect.getRelativePoint((float)Math.random(), (float)Math.random());
    }

    public void render(double t, double deltaT, float alpha) {
      if (deltaT > 0.1) {
        // probably a lag spike - make sure our velocities don't explode
        deltaT = 0.1;
      }

      this.updatePosition(deltaT);
      this.updateVelocity(deltaT);

      // we don't want the particles to get clipped by the
      RendererHelpers.withoutScissor(() -> {
        this.draw(t, alpha);
      });
    }

    public boolean isComplete(double t) {
      return t > this.start + this.lifetime;
    }

    private void updatePosition(double deltaT) {
      float perturbationMultiplier = 20 * (1 - this.mass);
      this.position = this.position
          .plus(this.velocity.scale((float)deltaT))
          .plus(new DimPoint(
              this.dimFactory.fromGui((float)(Math.random() * 2 - 1) * (float)deltaT * perturbationMultiplier),
              this.dimFactory.fromGui((float)(Math.random() * 2 - 1) * (float)deltaT * perturbationMultiplier)
          ));
    }

    private void updateVelocity(double deltaT) {
      // the higher the mass, the more momentum
      float momentumMultiplier = 30 * (1 - this.mass);
      this.velocity = this.velocity.plus(new DimPoint(
          this.dimFactory.fromGui((float)(Math.random() * 2 - 1) * (float)deltaT * momentumMultiplier),
          this.dimFactory.fromGui((float)(Math.random() * 1.8 - 1) * (float)deltaT * momentumMultiplier) // tending upwards
      ));
    }

    private float getAlpha(double t) {
      double relLifeRemaining = Math.max(0, 1 - (t - this.start) / lifetime);
      return (float)(
          -Math.max(0, relLifeRemaining * 4 - 3) + // initial fade in
          Math.min(1, relLifeRemaining * 3) // fade out in last third of life
      );
    }

    private void draw(double t, float alpha) {
      Colour colour = this.colour.withAlpha(this.getAlpha(t) * alpha);

      GlStateManager.pushMatrix();
      GlStateManager.translate(this.position.getX().getGui(), this.position.getY().getGui(), 0);

      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GlStateManager.disableAlpha();
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false); // this ensures we can draw multiple transparent things on top of each other
      GlStateManager.shadeModel(GL11.GL_SMOOTH); // for being able to draw colour gradients

      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldRenderer = tessellator.getWorldRenderer();
      worldRenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION_COLOR); // the final shape is always a convex polygon, so we can draw it in one go

      // draws a circle with limited vertices, aka a polygon
      float r = this.mass * 1.5f;
      float increment = 2 * (float)Math.PI / (corners);
      for (float theta = (float)Math.PI * 2; theta > 0; theta -= increment) { // counterclockwise
        Dim x = this.dimFactory.fromGui((float)Math.cos(theta) * r);
        Dim y = this.dimFactory.fromGui((float)Math.sin(theta) * r);
        addVertex(worldRenderer, -10, new DimPoint(x, y), colour);
      }
      tessellator.draw();

      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.depthMask(true);
      GlStateManager.shadeModel(GL11.GL_FLAT);

      GlStateManager.popMatrix();
    }
  }
}
