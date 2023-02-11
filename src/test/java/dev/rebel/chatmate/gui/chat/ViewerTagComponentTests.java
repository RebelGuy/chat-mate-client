package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.StatefulEmitter;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.events.models.ConfigEventOptions;
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
    ArgumentCaptor<EventCallback<Boolean>> captor = ArgumentCaptor.forClass(EventCallback.class);
    verify(this.showChatPlatformIconEmitter).onChange(captor.capture(), any(Object.class), eq(true));
    captor.getValue().dispatch(new Event<>(false));

    Assert.assertFalse(component.getComponent() instanceof ImageChatComponent);

    // change to true
    captor.getValue().dispatch(new Event<>(true));

    Assert.assertTrue(component.getComponent() instanceof ImageChatComponent);

    // change to false
    captor.getValue().dispatch(new Event<>(false));

    Assert.assertFalse(component.getComponent() instanceof ImageChatComponent);
  }

  @Test
  public void identifyPlatformsChanged_rendersCorrectText_onTwitchPlatform() {
    PlatformViewerTagComponent component = new PlatformViewerTagComponent(this.mockDimFactory, this.mockConfig, ChatPlatform.Twitch, false);

    // initially true
    ArgumentCaptor<EventCallback<Boolean>> captor = ArgumentCaptor.forClass(EventCallback.class);
    verify(this.showChatPlatformIconEmitter).onChange(captor.capture(), any(Object.class), eq(true));
    captor.getValue().dispatch(new Event<>(true));

    Assert.assertTrue(component.getComponent() instanceof ImageChatComponent);

    // change to false
    captor.getValue().dispatch(new Event<>(false));

    Assert.assertFalse(component.getComponent() instanceof ImageChatComponent);

    // change to true
    captor.getValue().dispatch(new Event<>(true));

    Assert.assertTrue(component.getComponent() instanceof ImageChatComponent);
  }
}
