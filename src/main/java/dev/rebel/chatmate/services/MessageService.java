package dev.rebel.chatmate.services;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static dev.rebel.chatmate.models.Styles.*;
import static dev.rebel.chatmate.services.util.ChatHelpers.joinComponents;

public class MessageService {
  private static final IChatComponent INFO_PREFIX = styledText("ChatMate>", INFO_MSG_PREFIX_STYLE);

  private final Random random = new Random();

  public MessageService() {

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
