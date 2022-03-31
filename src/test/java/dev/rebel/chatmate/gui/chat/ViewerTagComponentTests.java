package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.StatefulEmitter;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem.ChatPlatform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ViewerTagComponentTests {
  @Mock Config mockConfig;
  @Mock StatefulEmitter<Boolean> identifyPlatforms;

  @Before
  public void Setup() {
    when(mockConfig.getIdentifyPlatforms()).thenReturn(this.identifyPlatforms);
  }

  @Test
  public void identifyPlatformsChanged_rendersCorrectText_onYoutubePlatform() {
    // initial test
    when(this.identifyPlatforms.get()).thenReturn(false);

    ViewerTagComponent component = new ViewerTagComponent(this.mockConfig, ChatPlatform.Youtube);
    verifyText(component, "VIEWER");

    // change to true
    ArgumentCaptor<Consumer<Boolean>> captor = ArgumentCaptor.forClass(Consumer.class);
    verify(this.identifyPlatforms).onChange(captor.capture(), any(Object.class));
    captor.getValue().accept(true);

    verifyText(component, "YOUTUBE");

    // change to false
    captor.getValue().accept(false);

    verifyText(component, "VIEWER");
  }

  @Test
  public void identifyPlatformsChanged_rendersCorrectText_onTwitchPlatform() {
    // initial test
    when(this.identifyPlatforms.get()).thenReturn(true);

    ViewerTagComponent component = new ViewerTagComponent(this.mockConfig, ChatPlatform.Twitch);
    verifyText(component, "TWITCH");

    // change to false
    ArgumentCaptor<Consumer<Boolean>> captor = ArgumentCaptor.forClass(Consumer.class);
    verify(this.identifyPlatforms).onChange(captor.capture(), any(Object.class));
    captor.getValue().accept(false);

    verifyText(component, "VIEWER");

    // change to true
    captor.getValue().accept(true);

    verifyText(component, "TWITCH");
  }

  private static void verifyText(ViewerTagComponent component, String expectedViewerTag) {
    Assert.assertEquals(expectedViewerTag, component.getUnformattedText());
  }
}
