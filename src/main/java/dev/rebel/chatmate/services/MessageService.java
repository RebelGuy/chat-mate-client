package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.experience.RankedEntry;
import dev.rebel.chatmate.services.util.ChatHelpers.ClickEventWithCallback;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static dev.rebel.chatmate.models.Styles.*;
import static dev.rebel.chatmate.services.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.services.util.ChatHelpers.stringWithWidth;

public class MessageService {
  private static final IChatComponent INFO_PREFIX = styledText("ChatMate>", INFO_MSG_PREFIX_STYLE);

  private final Random random = new Random();
  private final LogService logService;
  private final MinecraftProxyService minecraftProxyService;

  public MessageService(LogService logService, MinecraftProxyService minecraftProxyService) {
    this.logService = logService;
    this.minecraftProxyService = minecraftProxyService;
  }

  public IChatComponent getErrorMessage(String msg) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(msg, ERROR_MSG_STYLE));
    return joinComponents(" ", list);
  }

  public IChatComponent getInfoMessage(String msg) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(msg, INFO_MSG_STYLE));
    return joinComponents(" ", list);
  }

  public IChatComponent getSmallLevelUpMessage(String name, int newLevel) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(" ", INFO_MSG_STYLE));
    list.add(styledText(name, VIEWER_NAME_STYLE));
    list.add(styledText(" has just reached level " + newLevel + "!", INFO_MSG_STYLE));
    return joinComponents("", list);
  }

  public IChatComponent getLargeLevelUpMessage(String name, int newLevel) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(this.getLargeLevelUpIntro(name, newLevel));
    list.add(this.getLargeLevelUpBody(name, newLevel));
    list.add(this.getLargeLevelUpOutro(name, newLevel));
    return joinComponents(" ", list);
  }

  public IChatComponent getRankedEntryMessage(RankedEntry entry, boolean deEmphasise, int rankDigits, int levelDigits, int nameWidth, int messageWidth) {
    // example:
    // #24 ShiroTheS... 41 <⣿⣿⣿⣿⣿     > 42
    // rank name levelStart barStart barFilled barBlank barEnd levelEnd

    FontRenderer fontRenderer = this.minecraftProxyService.getChatFontRenderer();
    assert fontRenderer != null;

    String separator = " ";

    int rankNumberWidth = fontRenderer.getStringWidth(String.join("", Collections.nCopies(rankDigits, "4")) + separator);
    String rank = "#" + stringWithWidth(fontRenderer, String.valueOf(entry.rank), "", ' ', rankNumberWidth) + separator;

    String name = stringWithWidth(fontRenderer, entry.channelName, "…", ' ', nameWidth) + separator;

    int levelNumberWidth = fontRenderer.getStringWidth(String.join("", Collections.nCopies(levelDigits, "4")) + separator);
    String levelStart = stringWithWidth(fontRenderer, String.valueOf(entry.level), "", ' ', levelNumberWidth) + separator;
    String levelEnd = stringWithWidth(fontRenderer, String.valueOf(entry.level + 1), "", ' ', levelNumberWidth);

    String barStart = "<";
    String barEnd = ">" + separator;

    int barBodyWidth = Math.max(0, messageWidth - fontRenderer.getStringWidth(rank + name + levelStart)
        - fontRenderer.getStringWidth(barStart) - fontRenderer.getStringWidth(barEnd + levelEnd));
    int fillWidth = Math.round(barBodyWidth * entry.levelProgress);
    String progressBar = stringWithWidth(fontRenderer, "", "", '⣿', fillWidth);
    String emptyBar = stringWithWidth(fontRenderer, "", "", ' ', barBodyWidth - fontRenderer.getStringWidth(progressBar));

    List<IChatComponent> list = new ArrayList<>();
    list.add(styledText(rank, deEmphasise ? INFO_MSG_STYLE : GOOD_MSG_STYLE));
    list.add(styledText(name, deEmphasise ? INFO_MSG_STYLE : VIEWER_NAME_STYLE));
    list.add(styledText(levelStart, deEmphasise ? INFO_MSG_STYLE : getLevelStyle(entry.level)));
    list.add(styledText(barStart, INFO_MSG_STYLE));
    list.add(styledText(progressBar, INFO_MSG_STYLE));
    list.add(styledText(emptyBar, INFO_MSG_STYLE));
    list.add(styledText(barEnd, INFO_MSG_STYLE));
    list.add(styledText(levelEnd, deEmphasise ? INFO_MSG_STYLE : getLevelStyle(entry.level + 1)));
    return joinComponents("", list);
  }

  public IChatComponent getLeaderboardFooterMessage(int messageWidth, @Nullable Runnable onPrevPage, @Nullable Runnable onNextPage) {
    FontRenderer fontRenderer = this.minecraftProxyService.getChatFontRenderer();
    assert fontRenderer != null;

    if (onPrevPage == null && onNextPage == null) {
      return styledText(stringWithWidth(fontRenderer, "", "", '-', messageWidth), INFO_MSG_STYLE);
    }

    String padding = "  ";
    String prevPageMsg = "<< Previous";
    String nextPageMsg = "Next >>";
    int interiorWidth = messageWidth - fontRenderer.getStringWidth(padding + prevPageMsg + padding + padding + nextPageMsg + padding);
    String interior = stringWithWidth(fontRenderer, "", "", '-', interiorWidth);

    ClickEventWithCallback onPrevClick = new ClickEventWithCallback(this.logService, onPrevPage, true);
    ClickEventWithCallback onNextClick = new ClickEventWithCallback(this.logService, onNextPage, true);

    List<IChatComponent> list = new ArrayList<>();
    list.add(styledText(padding, INFO_MSG_STYLE));
    list.add(styledText(prevPageMsg, onPrevClick.bind(onPrevPage == null ? INTERACTIVE_STYLE_DISABLED.get() : INTERACTIVE_STYLE.get())));
    list.add(styledText(padding, INFO_MSG_STYLE));
    list.add(styledText(interior, INFO_MSG_STYLE));
    list.add(styledText(padding, INFO_MSG_STYLE));
    list.add(styledText(nextPageMsg, onNextClick.bind(onNextPage == null ? INTERACTIVE_STYLE_DISABLED.get() : INTERACTIVE_STYLE.get())));
    list.add(styledText(padding, INFO_MSG_STYLE));
    return joinComponents("", list);
  }

  private IChatComponent getLargeLevelUpIntro(String name, int newLevel) {
    return pickRandom(INFO_MSG_STYLE,
        "My little rebels... I have news for you.",
        "You won't believe what just happened.",
        "Unbelievable!",
        "And they said it couldn't be done.",
        "Wow!",
        "Would you look at that.",
        "Also in the news:"
      );
  }

  private IChatComponent getLargeLevelUpBody(String name, int newLevel) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(styledText(name, VIEWER_NAME_STYLE));
    list.add(styledText(" has reached level ", INFO_MSG_STYLE));
    list.add(styledText(String.valueOf(newLevel), getLevelStyle(newLevel)));
    list.add(styledText("!", INFO_MSG_STYLE));
    return joinComponents("", list);
  }

  private IChatComponent getLargeLevelUpOutro(String name, int newLevel) {
    return pickRandom(INFO_MSG_STYLE,
        "Let's celebrate this incredible achievement!",
        "No one would believe it if it wasn't livestreamed!",
        "We respect you.",
        "When level 100?",
        "At this point we can just quit. We have reached the peak. It can't get better than this.",
        "Now maybe it's time to go outside for a bit?",
        "I can't even count that high... " + String.join(", ", IntStream.range(1, newLevel).mapToObj(String::valueOf).toArray(String[]::new)) + "... uhh I give up.",
        "A true rebel among us.",
        "Level 100 is within reach!",
        "Level " + (newLevel + 1) + " is within reach!",
        "Congratulations!",
        "Say 123 if you respect " + styledText(name, VIEWER_NAME_STYLE).getFormattedText() + styledText(".", INFO_MSG_STYLE).getFormattedText(),
        "Subscribe to " + styledText(name, VIEWER_NAME_STYLE).getFormattedText() + styledText((name.endsWith("s") ? "'" : "'s") + " YouTube channel for daily let's play videos!", INFO_MSG_STYLE).getFormattedText()
      );
  }

  private IChatComponent pickRandom(ChatStyle style, String... message) {
    int r = random.nextInt(message.length);
    return styledText(message[r], style);
  }
}
