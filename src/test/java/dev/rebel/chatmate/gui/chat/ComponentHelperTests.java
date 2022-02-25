package dev.rebel.chatmate.gui.chat;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentHelperTests {
  @Mock FontRenderer fontRenderer;

  @Test
  public void splitText_ShortTextComponent_NotSplit() {
    when(this.fontRenderer.getStringWidth(anyString())).thenReturn(1);
    IChatComponent component = new ChatComponentText("Test");

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontRenderer);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals("Test", result.get(0).getUnformattedTextForChat());
  }

  @Test
  public void splitText_LongTextComponent_RetainsStyleOnSplit() {
    when(this.fontRenderer.getStringWidth("Test1234")).thenReturn(10);
    when(this.fontRenderer.getStringWidth("234")).thenReturn(3);
    when(this.fontRenderer.trimStringToWidth("Test1234", 5)).thenReturn("Test1");
    IChatComponent component = new ChatComponentText("Test1234");
    ChatStyle style = new ChatStyle().setColor(EnumChatFormatting.BLACK);
    component.setChatStyle(style);

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontRenderer);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals(0, result.get(1).getSiblings().size());
    Assert.assertEquals("Test1", result.get(0).getUnformattedTextForChat());
    Assert.assertEquals(style.getFormattingCode(), result.get(0).getChatStyle().getFormattingCode());
    Assert.assertEquals("234", result.get(1).getUnformattedTextForChat());
    Assert.assertEquals(style.getFormattingCode(), result.get(1).getChatStyle().getFormattingCode());
  }

  @Test
  public void splitText_LongTextComponent_SplitBetweenWords() {
    when(this.fontRenderer.getStringWidth("Word1 Word2 Word3")).thenReturn(10);
    when(this.fontRenderer.getStringWidth("Word1")).thenReturn(4);
    when(this.fontRenderer.getStringWidth("Word2 Word3")).thenReturn(3);
    when(this.fontRenderer.trimStringToWidth("Word1 Word2 Word3", 5)).thenReturn("Word1 Wor");
    IChatComponent component = new ChatComponentText("Word1 Word2 Word3");

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontRenderer);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals(0, result.get(1).getSiblings().size());
    Assert.assertEquals("Word1", result.get(0).getUnformattedTextForChat());
    Assert.assertEquals("Word2 Word3", result.get(1).getUnformattedTextForChat());
  }

  @Test
  public void splitText_LongContainerComponent_RetainsDataOnSplit() {
    when(this.fontRenderer.getStringWidth("Test1234")).thenReturn(10);
    when(this.fontRenderer.getStringWidth("234")).thenReturn(3);
    when(this.fontRenderer.trimStringToWidth("Test1234", 5)).thenReturn("Test1");
    Object data = new Object();
    IChatComponent component = new ContainerChatComponent(new ChatComponentText("Test1234"), data);

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontRenderer);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals(0, result.get(1).getSiblings().size());
    Assert.assertEquals("Test1", result.get(0).getUnformattedTextForChat());
    Assert.assertEquals(data, ((ContainerChatComponent)result.get(0)).data);
    Assert.assertEquals("234", result.get(1).getUnformattedTextForChat());
    Assert.assertEquals(data, ((ContainerChatComponent)result.get(1)).data);

  }
}