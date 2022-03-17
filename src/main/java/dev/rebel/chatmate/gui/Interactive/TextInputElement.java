package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.In.KeyModifier;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TextInputElement extends SingleElement {
  private String placeholderText = "";
  private String text = "";
  private int maxStringLength = 64;

  public TextInputElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.setFocusable(true);
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  @Override
  public void onMouseDown(IEvent<MouseEventData.In> e) {
    this.mouseClicked((int)e.getData().mousePositionData.x.getGui(), (int)e.getData().mousePositionData.y.getGui(), e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON ? 0 : -1);
  }

  @Override
  public void onKeyDown(IEvent<KeyboardEventData.In> e) {
    if (this.textboxKeyTyped(e.getData())) {
      e.stopPropagation();
    }
  }

  @Override
  public DimPoint calculateThisSize(Dim maxFullWidth) {
    return this.getFullBoxSize(new DimPoint(maxFullWidth, this.context.dimFactory.fromGui(this.context.fontRenderer.FONT_HEIGHT)));
  }

  @Override
  public void renderElement() {
    this.drawTextBox();
  }

  private int lineScrollOffset; // if the text doesn't fit on the window, it scrolls to the right
  private int cursorPosition;
  private int selectionEnd;
  private int enabledColor = 14737632;
  private int disabledColor = 7368816;
  private boolean isEnabled = true;
  private Consumer<String> onTextChange;
  private Predicate<String> validator = text -> true;

  public void setText(String newText) {
    if (this.validator.test(newText)) {
      if (newText.length() > this.maxStringLength) {
        this.text = newText.substring(0, this.maxStringLength);
      } else {
        this.text = newText;
      }

      this.setCursorPositionEnd();
    }
  }

  public String getText() {
    return this.text;
  }

  public String getSelectedText() {
    int lvt_1_1_ = Math.min(this.cursorPosition, this.selectionEnd);
    int lvt_2_1_ = Math.max(this.cursorPosition, this.selectionEnd);
    return this.text.substring(lvt_1_1_, lvt_2_1_);
  }

  public void setValidator(Predicate<String> p_setValidator_1_) {
    this.validator = p_setValidator_1_;
  }

  public void writeText(String textToAdd) {
    // todo: adds text at the selection, overwriting the current selection (if any)
    String text = "";
    textToAdd = ChatAllowedCharacters.filterAllowedCharacters(textToAdd);
    int selectionStart = Math.min(this.cursorPosition, this.selectionEnd);
    int selectionEnd = Math.max(this.cursorPosition, this.selectionEnd);
    int maxLengthToAdd = this.maxStringLength - this.text.length() - (selectionStart - selectionEnd);
    if (this.text.length() > 0) {
      text = text + this.text.substring(0, selectionStart);
    }

    int lengthToAdd;
    if (maxLengthToAdd < textToAdd.length()) {
      text = text + textToAdd.substring(0, maxLengthToAdd);
      lengthToAdd = maxLengthToAdd;
    } else {
      text = text + textToAdd;
      lengthToAdd = textToAdd.length();
    }

    if (this.text.length() > 0 && selectionEnd < this.text.length()) {
      text = text + this.text.substring(selectionEnd);
    }

    if (this.validator.test(text)) {
      this.text = text;
      this.moveCursorBy(selectionStart - this.selectionEnd + lengthToAdd);
      this.onTextChange.accept(this.text);
    }
  }

  /** Deletes all the characters between the current cursor position and the given offset (may be negative) */
  public void deleteFromCursor(int offset) {
    if (this.text.length() != 0) {
      if (this.selectionEnd != this.cursorPosition) {
        this.writeText("");
      } else {
        boolean backwards = offset < 0;
        int start = backwards ? this.cursorPosition + offset : this.cursorPosition;
        int end = backwards ? this.cursorPosition : this.cursorPosition + offset;
        String remainingText = "";
        if (start >= 0) {
          remainingText = this.text.substring(0, start);
        }

        if (end < this.text.length()) {
          remainingText = remainingText + this.text.substring(end);
        }

        if (this.validator.test(remainingText)) {
          this.text = remainingText;
          if (backwards) {
            this.moveCursorBy(offset);
          }

          this.onTextChange.accept(this.text);
        }
      }
    }
  }

  public void deleteWords(int wordOffset) {
    if (this.text.length() != 0) {
      if (this.selectionEnd != this.cursorPosition) {
        this.writeText("");
      } else {
        this.deleteFromCursor(this.getNthWordFromCursor(wordOffset) - this.cursorPosition);
      }
    }
  }

  /** Gets the offset of the start of the word. N may be negative. E.g. N = -1 gets the offset from the cursor to the start of the previous word. */
  public int getNthWordFromCursor(int N) {
    return this.getNthWordFromPos(N, this.getCursorPosition());
  }

  /** Returns the offset from the position. */
  public int getNthWordFromPos(int N, int pos) {
    int currentPos = pos;
    boolean forwards = N >= 0;

    for(int i = 0; i < Math.abs(N); ++i) {
      if (forwards) {
        int length = this.text.length();
        currentPos = this.text.indexOf(' ', currentPos);
        if (currentPos == -1) {
          currentPos = length;
        } else {
          while(currentPos < length && this.text.charAt(currentPos) == ' ') {
            ++currentPos;
          }
        }
      } else {
        while(currentPos > 0 && this.text.charAt(currentPos - 1) == ' ') {
          --currentPos;
        }

        while(currentPos > 0 && this.text.charAt(currentPos - 1) != ' ') {
          --currentPos;
        }
      }
    }

    return currentPos;
  }

  public void moveCursorBy(int delta) {
    this.setCursorPosition(this.selectionEnd + delta);
  }

  public void setCursorPosition(int newPosition) {
    this.cursorPosition = newPosition;
    int N = this.text.length();
    this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, N);
    this.setSelectionPos(this.cursorPosition);
  }

  public void setCursorPositionZero() {
    this.setCursorPosition(0);
  }

  public void setCursorPositionEnd() {
    this.setCursorPosition(this.text.length());
  }

  public boolean textboxKeyTyped(KeyboardEventData.In data) {
    if (data.isKeyModifierActive(KeyModifier.CTRL) && data.isPressed(Keyboard.KEY_A)) {
      this.setCursorPositionEnd();
      this.setSelectionPos(0);
      return true;
    } else if (data.isKeyModifierActive(KeyModifier.CTRL) && data.isPressed(Keyboard.KEY_C)) {
      this.context.clipboardService.setClipboardString(this.getSelectedText());
      return true;
    } else if (data.isKeyModifierActive(KeyModifier.CTRL) && data.isPressed(Keyboard.KEY_V)) {
      String text = this.context.clipboardService.getClipboardString();
      if (text != null) {
        this.writeText(text);
      }
      return true;
    } else if (data.isKeyModifierActive(KeyModifier.CTRL) && data.isPressed(Keyboard.KEY_X)) {
      this.context.clipboardService.setClipboardString(this.getSelectedText());
      this.writeText("");

      return true;
    } else {
      switch(data.eventKey) {
        case Keyboard.KEY_BACK:
          if (GuiScreen.isCtrlKeyDown()) {
            if (this.isEnabled) {
              this.deleteWords(-1);
            }
          } else if (this.isEnabled) {
            this.deleteFromCursor(-1);
          }

          return true;
        case Keyboard.KEY_HOME:
          if (GuiScreen.isShiftKeyDown()) {
            this.setSelectionPos(0);
          } else {
            this.setCursorPositionZero();
          }

          return true;
        case Keyboard.KEY_LEFT:
          if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
              this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
            } else {
              this.setSelectionPos(this.getSelectionEnd() - 1);
            }
          } else if (GuiScreen.isCtrlKeyDown()) {
            this.setCursorPosition(this.getNthWordFromCursor(-1));
          } else {
            this.moveCursorBy(-1);
          }

          return true;
        case Keyboard.KEY_RIGHT:
          if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
              this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
            } else {
              this.setSelectionPos(this.getSelectionEnd() + 1);
            }
          } else if (GuiScreen.isCtrlKeyDown()) {
            this.setCursorPosition(this.getNthWordFromCursor(1));
          } else {
            this.moveCursorBy(1);
          }

          return true;
        case Keyboard.KEY_END:
          if (GuiScreen.isShiftKeyDown()) {
            this.setSelectionPos(this.text.length());
          } else {
            this.setCursorPositionEnd();
          }

          return true;
        case Keyboard.KEY_DELETE:
          if (GuiScreen.isCtrlKeyDown()) {
            if (this.isEnabled) {
              this.deleteWords(1);
            }
          } else if (this.isEnabled) {
            this.deleteFromCursor(1);
          }

          return true;
        default:
          if (ChatAllowedCharacters.isAllowedCharacter(data.eventCharacter)) {
            this.writeText(Character.toString(data.eventCharacter));

            return true;
          } else {
            return false;
          }
      }
    }
  }

  public void mouseClicked(int mouseX, int mouseY, int button) {
    if (this.context.focusedElement == this && button == 0) {
      int relX = mouseX - (int)this.getContentBox().getX().getGui();
      boolean enableBackground = true;
      if (enableBackground) {
        relX -= 4;
      }

      String visibleText = this.context.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
      this.setCursorPosition(this.context.fontRenderer.trimStringToWidth(visibleText, relX).length() + this.lineScrollOffset);
    }
  }

  public void drawTextBox() {
    if (this.getVisible()) {
      boolean enableBackground = true;
      int x = (int)this.getContentBox().getX().getGui();
      int y = (int)this.getContentBox().getY().getGui();
      int width = (int)this.getContentBox().getWidth().getGui();
      int height = (int)this.getContentBox().getHeight().getGui();

      // background
      if (enableBackground) {
        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, -6250336);
        Gui.drawRect(x, y, x + width, y + height, -16777216);
      }

      int color = this.isEnabled ? this.enabledColor : this.disabledColor;
      int cursorStartIndex = this.cursorPosition - this.lineScrollOffset;
      int cursorEndIndex = this.selectionEnd - this.lineScrollOffset;
      String string = this.context.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
      boolean cursorIsWithinRange = cursorStartIndex >= 0 && cursorStartIndex <= string.length();
      boolean drawCursor = this.context.focusedElement == this && (new Date().getTime() % 1000 < 500) && cursorIsWithinRange;
      int left = enableBackground ? x + 4 : x;
      int y2 = enableBackground ? y + (height - 8) / 2 : y;
      int stringX = left;
      if (cursorEndIndex > string.length()) {
        cursorEndIndex = string.length();
      }

      // draw part before cursor
      if (string.length() > 0) {
        String lvt_10_1_ = cursorIsWithinRange ? string.substring(0, cursorStartIndex) : string;
        stringX = this.context.fontRenderer.drawStringWithShadow(lvt_10_1_, (float)left, (float)y2, color);
      }

      boolean interiorCursor = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
      int x1 = stringX;
      if (!cursorIsWithinRange) {
        x1 = cursorStartIndex > 0 ? left + width : left;
      } else if (interiorCursor) {
        x1 = stringX - 1;
        --stringX;
      }

      // draw part after cursor
      if (string.length() > 0 && cursorIsWithinRange && cursorStartIndex < string.length()) {
        this.context.fontRenderer.drawStringWithShadow(string.substring(cursorStartIndex), (float)stringX, (float)y2, color);
      }

      if (drawCursor) {
        if (interiorCursor) {
          // vertical bar
          Gui.drawRect(x1, y2 - 1, x1 + 1, y2 + 1 + this.context.fontRenderer.FONT_HEIGHT, -3092272);
        } else {
          this.context.fontRenderer.drawStringWithShadow("_", (float)x1, (float)y2, color);
        }
      }

      // if there is a selection, invert the colours
      if (cursorEndIndex != cursorStartIndex) {
        int x2 = left + this.context.fontRenderer.getStringWidth(string.substring(0, cursorEndIndex));
        int cursorHeight = this.context.fontRenderer.FONT_HEIGHT;
        this.invertRegionColours(x1, y2 - 1, x2 - 1, y2 + 1 + cursorHeight);
      }
    }
  }

  private void invertRegionColours(int x1, int y1, int x2, int y2) {
    int temp;
    if (x1 < x2) {
      temp = x1;
      x1 = x2;
      x2 = temp;
    }

    if (y1 < y2) {
      temp = y1;
      y1 = y2;
      y2 = temp;
    }

    int x = (int)this.getContentBox().getX().getGui();
    int width = (int)this.getContentBox().getX().getGui();
    if (x2 > x + width) {
      x2 = x + width;
    }

    if (x1 > x + width) {
      x1 = x + width;
    }

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
    GlStateManager.disableTexture2D();
    GlStateManager.enableColorLogic();
    GlStateManager.colorLogicOp(5387);
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(x1, y2, 0.0D).endVertex();
    worldRenderer.pos(x2, y2, 0.0D).endVertex();
    worldRenderer.pos(x2, y1, 0.0D).endVertex();
    worldRenderer.pos(x1, y1, 0.0D).endVertex();
    tessellator.draw();
    GlStateManager.disableColorLogic();
    GlStateManager.enableTexture2D();
  }

  public void setMaxStringLength(int p_setMaxStringLength_1_) {
    this.maxStringLength = p_setMaxStringLength_1_;
    if (this.text.length() > p_setMaxStringLength_1_) {
      this.text = this.text.substring(0, p_setMaxStringLength_1_);
    }
  }

  public int getMaxStringLength() {
    return this.maxStringLength;
  }

  public int getCursorPosition() {
    return this.cursorPosition;
  }

  public void setTextColor(int p_setTextColor_1_) {
    this.enabledColor = p_setTextColor_1_;
  }

  public void setDisabledTextColour(int p_setDisabledTextColour_1_) {
    this.disabledColor = p_setDisabledTextColour_1_;
  }

  public int getSelectionEnd() {
    return this.selectionEnd;
  }

  public int getWidth() {
    // todo: it appears that enabling the background adds a 4-wide padding around the actual input area
    //return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
    return (int)this.getContentBox().getWidth().getGui();
  }

  public void setSelectionPos(int position) {
    int length = this.text.length();
    if (position > length) {
      position = length;
    }

    if (position < 0) {
      position = 0;
    }

    this.selectionEnd = position;
    if (this.lineScrollOffset > length) {
      this.lineScrollOffset = length;
    }

    int width = this.getWidth();
    String visibleText = this.context.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), width);
    int maxIndex = visibleText.length() + this.lineScrollOffset;
    if (position == this.lineScrollOffset) {
      this.lineScrollOffset -= this.context.fontRenderer.trimStringToWidth(this.text, width, true).length();
    }

    if (position > maxIndex) {
      this.lineScrollOffset += position - maxIndex;
    } else if (position <= this.lineScrollOffset) {
      this.lineScrollOffset -= this.lineScrollOffset - position;
    }

    this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, length);
  }
}
