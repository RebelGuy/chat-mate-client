package dev.rebel.chatmate.services;

import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;
import dev.rebel.chatmate.gui.ChatComponentRenderer;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ImageChatComponent;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.stores.RankApiStore;
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
  @Mock DonationService donationService;
  @Mock RankApiStore rankApiStore;
  @Mock ChatComponentRenderer chatComponentRenderer;
  @Mock DateTimeService dateTimeService;

  MessageService messageService;

  @Before
  public void setup() {
    this.messageService = new MessageService(this.logService, this.fontEngine, this.dimFactory, this.donationService, this.rankApiStore, this.chatComponentRenderer, this.dateTimeService);

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
    Dim padding = new Dim(() -> 1, Dim.DimAnchor.GUI);
    IChatComponent component = new ImageChatComponent(() -> null, padding, padding, false);

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
      primaryUserId = 1;
      firstSeen = 0L;
      channel = new PublicChannel() {{
        displayName = " Test channel ! ";
        activeRanks = new PublicUserRank[0];
        registeredUser = null;
      }};
    }};

    IChatComponent result = this.messageService.getUserComponent(user);

    Assert.assertEquals("Test channel !", result.getUnformattedText());
  }

  @Test
  public void getUserComponent_UnrenderableName_ReplacesWithPlaceholder() {
    when(this.fontEngine.getStringWidth("x")).thenReturn(0);
    PublicUser user = new PublicUser() {{
      primaryUserId = 5;
      firstSeen = 0L;
      channel = new PublicChannel() {{
        displayName = "x";
        activeRanks = new PublicUserRank[0];
        registeredUser = null;
      }};
    }};

    IChatComponent result = this.messageService.getUserComponent(user);

    Assert.assertEquals("User 5", result.getUnformattedText());
  }
}
