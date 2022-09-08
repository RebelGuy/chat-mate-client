package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.FontEngine;
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
  @Mock FontEngine fontEngine;

  @Test
  public void splitText_ShortTextComponent_NotSplit() {
    when(this.fontEngine.getStringWidth(anyString())).thenReturn(1);
    IChatComponent component = new ChatComponentText("Test");

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontEngine);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals("Test", result.get(0).getUnformattedTextForChat());
  }

  @Test
  public void splitText_LongTextComponent_RetainsStyleOnSplit() {
    when(this.fontEngine.getStringWidth("§0Test1234")).thenReturn(10);
    when(this.fontEngine.getStringWidth("§0Test1")).thenReturn(7);
    when(this.fontEngine.getStringWidth("§0234")).thenReturn(3);
    when(this.fontEngine.trimStringToWidth("§0Test1234", 5)).thenReturn("§0Test1");
    IChatComponent component = new ChatComponentText("Test1234");
    ChatStyle style = new ChatStyle().setColor(EnumChatFormatting.BLACK);
    component.setChatStyle(style);

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontEngine);

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
    when(this.fontEngine.getStringWidth("Word1 Word2 Word3")).thenReturn(10);
    when(this.fontEngine.getStringWidth("Word1")).thenReturn(4);
    when(this.fontEngine.getStringWidth("Word2 Word3")).thenReturn(3);
    when(this.fontEngine.trimStringToWidth("Word1 Word2 Word3", 5)).thenReturn("Word1 Wor");
    IChatComponent component = new ChatComponentText("Word1 Word2 Word3");

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontEngine);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals(0, result.get(1).getSiblings().size());
    Assert.assertEquals("Word1", result.get(0).getUnformattedTextForChat());
    Assert.assertEquals("Word2 Word3", result.get(1).getUnformattedTextForChat());
  }

  @Test
  public void splitText_LongContainerComponent_RetainsDataOnSplit() {
    when(this.fontEngine.getStringWidth("Test1234")).thenReturn(10);
    when(this.fontEngine.getStringWidth("Test1")).thenReturn(7);
    when(this.fontEngine.getStringWidth("234")).thenReturn(3);
    when(this.fontEngine.trimStringToWidth("Test1234", 5)).thenReturn("Test1");
    Object data = new Object();
    IChatComponent component = new ContainerChatComponent(new ChatComponentText("Test1234"), data);

    List<IChatComponent> result = ComponentHelpers.splitText(component, 5, this.fontEngine);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals(0, result.get(1).getSiblings().size());
    Assert.assertEquals("Test1", result.get(0).getUnformattedTextForChat());
    Assert.assertEquals(data, ((ContainerChatComponent)result.get(0)).getData());
    Assert.assertEquals("234", result.get(1).getUnformattedTextForChat());
    Assert.assertEquals(data, ((ContainerChatComponent)result.get(1)).getData());
  }

  @Test
  public void splitText_AdjacentWords_SplitBetweenAndRemovedSpace() {
    IChatComponent emojiComponent = new ChatComponentText(":test1:").appendSibling(new ChatComponentText(" :test2:"));
    when(this.fontEngine.getStringWidth(":test1:")).thenReturn(10);
    when(this.fontEngine.getStringWidth(" :test2:")).thenReturn(11);
    when(this.fontEngine.getStringWidth(":test2:")).thenReturn(10);
    when(this.fontEngine.trimStringToWidth(" :test2:", 5)).thenReturn(" :te");


    List<IChatComponent> result = ComponentHelpers.splitText(emojiComponent, 15, this.fontEngine);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals(0, result.get(1).getSiblings().size());
    Assert.assertEquals(":test1:", result.get(0).getUnformattedTextForChat());
    Assert.assertEquals(":test2:", result.get(1).getUnformattedTextForChat());
  }

  @Test
  public void splitText_EmptyTextComponent_ReturnsSingleLineWithEmptyComponent() {
    IChatComponent component = new ChatComponentText("");
    when(this.fontEngine.getStringWidth("")).thenReturn(0);

    List<IChatComponent> result = ComponentHelpers.splitText(component, 15, this.fontEngine);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals("", result.get(0).getUnformattedTextForChat());
  }

  @Test
  public void splitText_ContainerComponentWithEmptyTextComponent_ReturnsSingleLineWithEmptyComponent() {
    IChatComponent textComponent = new ChatComponentText("");
    IChatComponent containerComponent = new ContainerChatComponent(textComponent);
    when(this.fontEngine.getStringWidth("")).thenReturn(0);

    List<IChatComponent> result = ComponentHelpers.splitText(containerComponent, 15, this.fontEngine);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals(0, result.get(0).getSiblings().size());
    Assert.assertEquals("", result.get(0).getUnformattedTextForChat());
  }
}
