package dev.rebel.chatmate.services;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ImageChatComponent;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.models.publicObjects.user.PublicChannelInfo;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTests {
  @Mock LogService logService;
  @Mock FontEngine fontEngine;
  @Mock DimFactory dimFactory;

  MessageService messageService;

  @Before
  public void setup() {
    this.messageService = new MessageService(this.logService, this.fontEngine, this.dimFactory);

    when(this.fontEngine.getStringWidth(anyString())).thenAnswer(i -> ((String)i.getArgument(0)).length());
  }

  @Test
  public void ensureNonempty_ComponentWithText_ReturnsArgument() {
    IChatComponent component = new ChatComponentText(" ").appendSibling(new ChatComponentText("Not empty"));

    IChatComponent result = this.messageService.ensureNonempty(component, "Test");

    Assert.assertEquals(component, result);
  }

  @Test
  public void ensureNonempty_ComponentWithImage_ReturnsArgument() {
    IChatComponent component = new ImageChatComponent(() -> null, 0, 0);

    IChatComponent result = this.messageService.ensureNonempty(component, "Test");

    Assert.assertEquals(component, result);

  }

  @Test
  public void ensureNonempty_WhitespaceOnly_ReturnsMessage() {
    IChatComponent component = new ChatComponentText("").appendSibling(new ChatComponentText("  "));

    IChatComponent result = this.messageService.ensureNonempty(component, "Test");

    Assert.assertEquals("Test", result.getUnformattedText());
  }

  @Test
  public void getUserComponent_UsesTrimmedName() {
    PublicUser user = new PublicUser() {{
      id = 1;
      userInfo = new PublicChannelInfo() {{
        channelName = " Test channel ! ";
        activeRanks = new PublicUserRank[0];
      }};
    }};

    IChatComponent result = this.messageService.getUserComponent(user);

    Assert.assertEquals("Test channel !", result.getUnformattedText());
  }

  @Test
  public void getUserComponent_UnrenderableName_ReplacesWithPlaceholder() {
    when(this.fontEngine.getStringWidth("x")).thenReturn(0);
    PublicUser user = new PublicUser() {{
      id = 5;
      userInfo = new PublicChannelInfo() {{
        channelName = "x";
        activeRanks = new PublicUserRank[0];
      }};
    }};

    IChatComponent result = this.messageService.getUserComponent(user);

    Assert.assertEquals("User 5", result.getUnformattedText());
  }
}
