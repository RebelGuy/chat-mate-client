package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.EnumHelpers;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.Date;

public class UserNameChatComponent extends ChatComponentBase {
  private static final char CHAR_SECTION_SIGN = 167;

  private final FontEngine fontEngine;
  private final DimFactory dimFactory;
  public final PublicUser user;
  private final Font baseFont;
  private final boolean useEffects;
  private final boolean isDonator;
  private final double tOffset;

  private String displayName;

  public UserNameChatComponent(FontEngine fontEngine, DimFactory dimFactory, PublicUser user, Font baseFont, String displayName, boolean useEffects) {
    super();
    this.fontEngine = fontEngine;
    this.dimFactory = dimFactory;
    this.user = user;
    this.baseFont = baseFont;
    this.displayName = displayName;
    this.useEffects = useEffects;
    this.tOffset = user.id.hashCode();

    this.isDonator = EnumHelpers.getFirst(Collections.map(Collections.list(this.user.activeRanks), r -> r.rank.name), RankName.DONATOR, RankName.SUPPORTER, RankName.MEMBER) != null;
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

  public int getWidth() {
    return (int)this.fontEngine.getStringWidthDim(this.user.userInfo.channelName, this.baseFont).getGui();
  }

  /** Returns the width. */
  public int renderComponent(Dim x, Dim y, int opacityInt) {
    float opacity = opacityInt / 255f;
    Dim newX;
    if (this.useEffects && this.isDonator) {
      // is donator
      newX = this.renderRainbow(x, y, opacity);
    } else {
      newX = this.renderNormal(x, y, opacity);
    }

    return (int)newX.minus(x).getGui();
  }

  @Override
  public String getUnformattedTextForChat() {
    return this.displayName;
  }

  @Override
  public IChatComponent createCopy() {
    return new UserNameChatComponent(this.fontEngine, this.dimFactory, this.user, this.baseFont, this.displayName, this.useEffects);
  }

  private Dim renderNormal(Dim x, Dim y, float opacity) {
    return this.fontEngine.drawString(this.displayName, x, y, this.baseFont);
  }

  private Dim renderRainbow(Dim x, Dim y, float opacity) {
    double t = ((double)new Date().getTime() / 1000) + this.tOffset;

    String fullString = this.displayName;
    for (int i = 0; i < fullString.length(); i++) {
      String c = fullString.substring(i, i + 1);
      if (c.charAt(0) == CHAR_SECTION_SIGN) {
        i++;
        continue;
      }

      // make sure we don't get noticeable repeats
      float r = ((float)Math.sin(t / 2) + 1) / 2;
      float g = ((float)Math.sin(t / Math.E) + 1) / 2;
      float b = ((float)Math.sin(t / Math.PI) + 1) / 2;
      Font font = new Font(this.baseFont).withColour(new Colour(r, g, b, opacity));
      x = this.fontEngine.drawString(c, x, y, font);
      t -= 0.15; // animates the colours from left to right
    }

    return x;
  }
}
