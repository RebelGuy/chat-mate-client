package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.StatefulEmitter;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.events.models.ConfigEventData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ViewerTagComponentTests {
  @Mock DimFactory mockDimFactory;
  @Mock Config mockConfig;
  @Mock StatefulEmitter<Boolean> showChatPlatformIconEmitter;

  @Before
  public void Setup() {
    when(this.mockConfig.getShowChatPlatformIconEmitter()).thenReturn(this.showChatPlatformIconEmitter);
  }

  @Test
  public void identifyPlatformsChanged_rendersImage_onYoutubePlatform() {
    PlatformViewerTagComponent component = new PlatformViewerTagComponent(this.mockDimFactory, this.mockConfig, ChatPlatform.Youtube, false);

    // initially false
    ArgumentCaptor<Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>>> captor = ArgumentCaptor.forClass(Function.class);
    verify(this.showChatPlatformIconEmitter).onChange(captor.capture(), any(Object.class), eq(true));
    captor.getValue().apply(new ConfigEventData.In<>(false));

    Assert.assertFalse(component.getComponent() instanceof ImageChatComponent);

    // change to true
    captor.getValue().apply(new ConfigEventData.In<>(true));

    Assert.assertTrue(component.getComponent() instanceof ImageChatComponent);

    // change to false
    captor.getValue().apply(new ConfigEventData.In<>(false));

    Assert.assertFalse(component.getComponent() instanceof ImageChatComponent);
  }

  @Test
  public void identifyPlatformsChanged_rendersCorrectText_onTwitchPlatform() {
    PlatformViewerTagComponent component = new PlatformViewerTagComponent(this.mockDimFactory, this.mockConfig, ChatPlatform.Twitch, false);

    // initially true
    ArgumentCaptor<Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>>> captor = ArgumentCaptor.forClass(Function.class);
    verify(this.showChatPlatformIconEmitter).onChange(captor.capture(), any(Object.class), eq(true));
    captor.getValue().apply(new ConfigEventData.In<>(true));

    Assert.assertTrue(component.getComponent() instanceof ImageChatComponent);

    // change to false
    captor.getValue().apply(new ConfigEventData.In<>(false));

    Assert.assertFalse(component.getComponent() instanceof ImageChatComponent);

    // change to true
    captor.getValue().apply(new ConfigEventData.In<>(true));

    Assert.assertTrue(component.getComponent() instanceof ImageChatComponent);
  }

  private static void verifyText(PlatformViewerTagComponent component, boolean expectImage) {
    if (expectImage) {

    }
    Assert.assertEquals(component, component.getUnformattedText());
  }
}
