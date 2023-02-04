package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData;
import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData.FocusReason;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.StateManagement.State;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.KeyboardEventData.In.KeyModifier;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.EnumHelpers;
import dev.rebel.chatmate.util.TextHelpers;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import scala.Tuple2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static dev.rebel.chatmate.util.TextHelpers.isNullOrEmpty;

/** Border: actually draws a border. Padding: space between text and border. */
public class TextInputElement extends InputElement {
  private String placeholderText = "";
  private String text;
  private int maxStringLength = 64;

  private int scrollOffsetIndex; // if the text doesn't fit on the window, it scrolls to the right
  private int cursorIndex;
  private int selectionEndIndex; // this may be before or after the cursor
  private Colour enabledColour = new Colour(224, 224, 224);
  private boolean warning = false;
  private Colour disabledColour = new Colour(112, 112, 112);
  private @Nullable Consumer<String> onTextChange;
  private Predicate<String> validator = text -> true;
  private @Nullable Function<String, List<Tuple2<String, Font>>> textFormatter = null;
  private @Nullable String suffix = null;
  private @Nullable String placeholder = null;
  private @Nullable Runnable onSubmit = null;
  private InputType inputType = InputType.TEXT;

  private Dim textHeight;
  private float textScale = 1;

  public TextInputElement(InteractiveContext context, IElement parent) {
    this(context, parent, "");
  }

  public TextInputElement(InteractiveContext context, IElement parent, @Nonnull String initialText) {
    super(context, parent);

    super.setCursor(CursorType.TEXT);
    super.setFocusable(true);
    super.setBorder(new Layout.RectExtension(gui(1)));
    super.setPadding(new Layout.RectExtension(gui(4), gui(2)));

    this.text = initialText;
    this.textHeight = super.fontEngine.FONT_HEIGHT_DIM;
  }

  @Override
  public List<IElement> getChildren() {
    return null;
  }

  public TextInputElement setTextScale(float textScale) {
    if (this.textScale != textScale) {
      this.textScale = textScale;
      this.textHeight = super.fontEngine.FONT_HEIGHT_DIM.times(this.textScale);
      super.onInvalidateSize();
    }

    return this;
  }

  @Override
  public void onMouseDown(InteractiveEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      Dim relX = e.getData().mousePositionData.x.minus(this.getContentBox().getX());
      String textBeforeCursor = this.fontEngine.trimStringToWidth(this.getVisibleText(), (int)(relX.getGui() / this.textScale)); // i.e. do this operation at 100% scale
      this.setCursorIndex(this.scrollOffsetIndex + textBeforeCursor.length());
      e.stopPropagation();
    }
  }

  @Override
  public void onKeyDown(InteractiveEvent<KeyboardEventData.In> e) {
    if (this.textboxKeyTyped(e.getData())) {
      e.stopPropagation();
    }
  }

  @Override
  public void onFocus(InteractiveEvent<FocusEventData> e) {
    if (e.getData().reason == FocusReason.TAB) {
      // select all when we tab into the field
      this.cursorIndex = this.text.length();
      this.selectionEndIndex = 0;
    } else if (e.getData().reason == FocusReason.CODE) {
      this.cursorIndex = this.text.length();
      this.selectionEndIndex = this.text.length();
    }
  }

  @Override
  public void onBlur(InteractiveEvent<FocusEventData> e) {
    this.cursorIndex = 0;
    this.selectionEndIndex = 0;
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return new DimPoint(maxContentSize, this.textHeight);
  }

  @Override
  protected void renderElement() {
    this.drawTextBox();
  }

  public TextInputElement onTextChange(Consumer<String> onTextChange) {
    this.onTextChange = onTextChange;
    return this;
  }

  /** Careful - not validated. Does not call the `onTextChange` callback. */
  public TextInputElement setTextUnsafe(String text) {
    this.text = text;
    if (super.hasFocus()) {
      this.setCursorPositionToEnd();
    } else if (super.isInitialised()) {
      this.setCursorIndex(0);
    }
    return this;
  }

  public TextInputElement setType(InputType type) {
    this.inputType = type;
    return this;
  }

  public String getText() {
    return this.text;
  }

  /** Validates the text, then calls the `onTextChange` callback. */
  public TextInputElement setText(String newText) {
    if (this.validator.test(newText)) {
      if (newText.length() > this.maxStringLength) {
        this.text = newText.substring(0, this.maxStringLength);
      } else {
        this.text = newText;
      }

      this.setCursorPositionToEnd();

      if (this.onTextChange != null) {
        this.onTextChange.accept(this.text);
      }
    }

    return this;
  }

  public TextInputElement setValidator(Predicate<String> validator) {
    this.validator = validator;
    return this;
  }

  /** Called when the user presses the enter key. */
  public TextInputElement setOnSubmit(Runnable onSubmit) {
    this.onSubmit = onSubmit;
    return this;
  }

  /** Allows formatting sections of the string. It is required that the total string obtained by concatenating the entries in the returned list is equivalent to the input string. */
  public TextInputElement setTextFormatter(Function<String, List<Tuple2<String, Font>>> textFormatter) {
    this.textFormatter = textFormatter;
    return this;
  }

  public TextInputElement setSuffix(String suffix) {
    this.suffix = suffix;
    return this;
  }

  public TextInputElement setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
    return this;
  }

  /** Adds text at the current cursor position, overwriting any selected text. */
  private void writeText(String textToAdd) {
    String text = "";
    textToAdd = ChatAllowedCharacters.filterAllowedCharacters(textToAdd);
    int selectionStart = Math.min(this.cursorIndex, this.selectionEndIndex);
    int selectionEnd = Math.max(this.cursorIndex, this.selectionEndIndex);
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
      this.moveCursorBy(selectionStart - this.selectionEndIndex + lengthToAdd);

      if (this.onTextChange != null) {
        this.onTextChange.accept(this.text);
      }
    }
  }

  /** Deletes all the characters between the current cursor position and the given offset (may be negative) */
  public void deleteFromCursor(int offset) {
    if (this.text.length() != 0) {
      if (this.selectionEndIndex != this.cursorIndex) {
        this.writeText("");
      } else {
        boolean backwards = offset < 0;
        int start = backwards ? this.cursorIndex + offset : this.cursorIndex;
        int end = backwards ? this.cursorIndex : this.cursorIndex + offset;
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

          if (this.onTextChange != null) {
            this.onTextChange.accept(this.text);
          }
        }
      }
    }
  }

  private void deleteWords(int wordOffset) {
    if (this.text.length() != 0) {
      if (this.selectionEndIndex != this.cursorIndex) {
        this.writeText("");
      } else {
        this.deleteFromCursor(this.getNthWordFromCursor(wordOffset) - this.cursorIndex);
      }
    }
  }

  /** Gets the offset of the start of the word. N may be negative. E.g. N = -1 gets the offset from the cursor to the start of the previous word. */
  private int getNthWordFromCursor(int N) {
    return this.getNthWordFromPos(N, this.cursorIndex);
  }

  /** Returns the offset from the position. */
  private int getNthWordFromPos(int N, int pos) {
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

  public boolean textboxKeyTyped(KeyboardEventData.In data) {
    if (data.isKeyModifierActive(KeyModifier.CTRL) && data.isPressed(Keyboard.KEY_A)) {
      this.setCursorPositionToEnd();
      this.setSelectionIndex(0);
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
            if (super.getEnabled()) {
              this.deleteWords(-1);
            }
          } else if (super.getEnabled()) {
            this.deleteFromCursor(-1);
          }

          return true;
        case Keyboard.KEY_HOME:
          if (GuiScreen.isShiftKeyDown()) {
            this.setSelectionIndex(0);
          } else {
            this.setCursorPositionToStart();
          }

          return true;
        case Keyboard.KEY_LEFT:
          if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
              this.setSelectionIndex(this.getNthWordFromPos(-1, this.selectionEndIndex));
            } else {
              this.setSelectionIndex(this.selectionEndIndex - 1);
            }
          } else if (GuiScreen.isCtrlKeyDown()) {
            this.setCursorIndex(this.getNthWordFromCursor(-1));
          } else {
            this.moveCursorBy(-1);
          }

          return true;
        case Keyboard.KEY_RIGHT:
          if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
              this.setSelectionIndex(this.getNthWordFromPos(1, this.selectionEndIndex));
            } else {
              this.setSelectionIndex(this.selectionEndIndex + 1);
            }
          } else if (GuiScreen.isCtrlKeyDown()) {
            this.setCursorIndex(this.getNthWordFromCursor(1));
          } else {
            this.moveCursorBy(1);
          }

          return true;
        case Keyboard.KEY_END:
          if (GuiScreen.isShiftKeyDown()) {
            this.setSelectionIndex(this.text.length());
          } else {
            this.setCursorPositionToEnd();
          }

          return true;
        case Keyboard.KEY_DELETE:
          if (GuiScreen.isCtrlKeyDown()) {
            if (super.getEnabled()) {
              this.deleteWords(1);
            }
          } else if (super.getEnabled()) {
            this.deleteFromCursor(1);
          }

          return true;

        case Keyboard.KEY_RETURN:
        case Keyboard.KEY_NUMPADENTER:
          if (this.onSubmit == null) {
            return false;
          } else {
            this.onSubmit.run();
            return true;
          }

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

  public void drawTextBox() {
    if (this.getVisible()) {
      this.drawBorder();
      this.drawBackground();
      this.drawSuffix();
      this.drawEditableText();

      if (this.context.focusedElement != this && isNullOrEmpty(this.text)) {
        this.drawPlaceholder();
      }
    }
  }

  private void drawBorder() {
    Colour borderColour = this.warning ? Colour.RED : new Colour(-6250336);
    if (!this.getEnabled()) {
      borderColour = borderColour.withBrightness(0.5f);
    }
    RendererHelpers.drawRect(this.getZIndex(), this.getBorderBox(), borderColour);
  }

  private void drawBackground() {
    Colour backgroundColour = new Colour(-16777216);
    RendererHelpers.drawRect(this.getZIndex(), this.getPaddingBox(), backgroundColour);
  }

  private void drawSuffix() {
    if (isNullOrEmpty(this.suffix)) {
      return;
    }

    Dim suffixWidth = this.fontEngine.getStringWidthDim(this.suffix).times(this.textScale);
    Dim left = this.getContentBox().getRight().minus(suffixWidth);
    Dim top = this.getContentBox().getY();
    RendererHelpers.withMapping(new DimPoint(left, top), this.textScale, () -> {
      this.fontEngine.drawString(this.suffix, ZERO, ZERO, new Font().withColour(this.disabledColour));
    });
  }

  private void drawPlaceholder() {
    if (isNullOrEmpty(this.placeholder)) {
      return;
    }

    // make sure the placeholder doesn't clip out of the text box
    DimRect box = super.getPaddingBox();
    if (super.getVisibleBox() != null) {
      box = super.getVisibleBox().clamp(box);
    }

    if (box.getAreaGui() <= 0) {
      return;
    }

    RendererHelpers.withScissor(box, super.context.dimFactory.getMinecraftSize(), () -> {
      RendererHelpers.withMapping(super.getContentBox().getPosition(), this.textScale, () -> {
        this.context.fontEngine.drawString(this.placeholder, ZERO, ZERO, new Font().withColour(this.disabledColour).withItalic(true));
      });
    });
  }

  private String getTextToRender() {
    if (this.inputType == InputType.TEXT) {
      return this.text;
    } else if (this.inputType == InputType.PASSWORD) {
      return TextHelpers.copy("*", this.text.length());
    } else {
      throw EnumHelpers.<InputType>assertUnreachable(this.inputType);
    }
  }

  private void drawEditableText() {
    Dim left = this.getContentBox().getX();
    Dim top = this.getContentBox().getY();
    Dim width = this.getEditableWidth();
    Dim bottom = this.getContentBox().getBottom();
    Dim right = left.plus(width);

    String textToRender = this.getTextToRender();
    String trimmedString = this.fontEngine.trimStringToWidth(textToRender.substring(this.scrollOffsetIndex), (int)(width.getGui() / this.textScale)); // i.e. perform this operation at 100% scale
    int visibleStringLength = trimmedString.length();
    int trimmedStringBegin = this.scrollOffsetIndex;
    int trimmedStringEnd = trimmedStringBegin + visibleStringLength;

    int cursorStartIndex = this.cursorIndex - this.scrollOffsetIndex;
    int cursorEndIndex = this.selectionEndIndex - this.scrollOffsetIndex;
    boolean cursorIsWithinRange = cursorStartIndex >= 0 && cursorStartIndex <= visibleStringLength;
    boolean drawCursor = this.context.focusedElement == this && (new Date().getTime() % 1000 < 500) && cursorIsWithinRange;
    Dim currentX = left;
    if (cursorEndIndex > visibleStringLength) {
      cursorEndIndex = visibleStringLength;
    }

    // draw part before cursor
    if (visibleStringLength > 0) {
      List<Tuple2<String, Font>> visibleString = cursorIsWithinRange ? this.getFormattedString(trimmedStringBegin, trimmedStringBegin + cursorStartIndex) : this.getFormattedString(trimmedStringBegin, trimmedStringEnd);

      // thanks java
      State<Dim> newX = new State<>(ZERO);
      RendererHelpers.withMapping(new DimPoint(left, top), this.textScale, () -> {
        Dim returnedValue = this.fontEngine.drawString(visibleString, ZERO, ZERO);
        newX.setState(returnedValue.times(this.textScale));
      });
      currentX = currentX.plus(newX.getState());
    }

    boolean interiorCursor = this.cursorIndex < this.text.length() || this.text.length() >= this.maxStringLength;
    Dim x1 = currentX;
    if (!cursorIsWithinRange) {
      x1 = cursorStartIndex > 0 ? right : left;
    } else if (interiorCursor) {
      x1 = currentX.minus(gui(1));
    }

    // draw part after cursor
    if (visibleStringLength > 0 && cursorIsWithinRange && cursorStartIndex < visibleStringLength) {
      List<Tuple2<String, Font>> visibleString = this.getFormattedString(trimmedStringBegin + cursorStartIndex, trimmedStringEnd);
      RendererHelpers.withMapping(new DimPoint(currentX, top), this.textScale, () -> {
        this.fontEngine.drawString(visibleString, ZERO, ZERO);
      });
    }

    if (drawCursor) {
      this.drawCursor(interiorCursor, x1, top);
    }

    // if there is a selection, invert the colours
    if (cursorEndIndex != cursorStartIndex) {
      Dim leftPad = this.fontEngine.getStringWidthDim(trimmedString.substring(0, cursorEndIndex)).times(this.textScale);
      Dim x2 = left.plus(leftPad);
      this.invertRegionColours(x1, top.minus(gui(1)), x2.minus(gui(1)), bottom.plus(gui(1)));
    }
  }

  /** endIndex is exclusive. */
  private List<Tuple2<String, Font>> getFormattedString(int beginIndex, @Nullable Integer endIndex) {
    String text = this.getTextToRender();
    if (endIndex == null) {
      endIndex = text.length();
    }

    if (this.textFormatter == null) {
      Colour colour = super.getEnabled() ? this.enabledColour : this.disabledColour;
      Font font = new Font().withColour(colour).withShadow(new Shadow(super.context.dimFactory));
      text = text.substring(beginIndex, endIndex);
      return Collections.list(new Tuple2<>(text, font));
    }

    List<Tuple2<String, Font>> result = new ArrayList<>();
    int i = 0;
    for (Tuple2<String, Font> formattedText : this.textFormatter.apply(text)) {
      String fullChunk = formattedText._1;
      int chunkBegin = i;
      int chunkEnd = i + fullChunk.length();

      // set substring indices
      if (chunkBegin < beginIndex) {
        chunkBegin = beginIndex;
      }
      if (chunkEnd > endIndex) {
        chunkEnd = endIndex;
      }
      if (chunkEnd <= chunkBegin) {
        i += fullChunk.length();
        continue;
      }

      String partialChunk = fullChunk.substring(chunkBegin - i, chunkEnd - i);
      result.add(new Tuple2<>(partialChunk, formattedText._2));
      i += fullChunk.length();
    }

    return result;
  }

  private void drawCursor(boolean interiorCursor, Dim left, Dim top) {
    if (interiorCursor) {
      // vertical bar
      Colour cursorColour = new Colour(-3092272);
      Dim one = gui(1);
      RendererHelpers.drawRect(this.getZIndex(), new DimRect(left, top.minus(one), one, this.textHeight.plus(one)), cursorColour);
    } else {
      Colour colour = super.getEnabled() ? this.enabledColour : this.disabledColour;
      Font font = new Font().withColour(colour).withShadow(new Shadow(super.context.dimFactory));
      RendererHelpers.withMapping(new DimPoint(left, top), this.textScale, () -> {
        this.fontEngine.drawString("_", ZERO, ZERO, font);
      });
    }
  }

  private void invertRegionColours(Dim x1, Dim y1, Dim x2, Dim y2) {
    Dim temp;
    if (x1.lt(x2)) {
      temp = x1;
      x1 = x2;
      x2 = temp;
    }

    if (y1.lt(y2)) {
      temp = y1;
      y1 = y2;
      y2 = temp;
    }

    Dim right = this.getContentBox().getX().plus(this.getEditableWidth());
    if (x2.gt(right)) {
      x2 = right;
    }

    if (x1.gt(right)) {
      x1 = right;
    }

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
    GlStateManager.disableTexture2D();
    GlStateManager.enableColorLogic();
    GlStateManager.colorLogicOp(5387);
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(x1.getGui(), y2.getGui(), 0.0D).endVertex();
    worldRenderer.pos(x2.getGui(), y2.getGui(), 0.0D).endVertex();
    worldRenderer.pos(x2.getGui(), y1.getGui(), 0.0D).endVertex();
    worldRenderer.pos(x1.getGui(), y1.getGui(), 0.0D).endVertex();
    tessellator.draw();
    GlStateManager.disableColorLogic();
    GlStateManager.enableTexture2D();
  }

  public TextInputElement setMaxStringLength(int maxStringLength) {
    this.maxStringLength = maxStringLength;
    if (this.text.length() > maxStringLength) {
      this.text = this.text.substring(0, maxStringLength);
    }
    return this;
  }

  public void setTextColor(Colour enabledColour) {
    this.enabledColour = enabledColour;
  }

  /** Draws a warning outline. */
  public void setWarning(boolean warning) {
    this.warning = warning;
  }

  public void setDisabledTextColour(Colour disabledColour) {
    this.disabledColour = disabledColour;
  }

  public void setSelectionIndex(int newIndex) {
    int length = this.text.length();
    if (newIndex > length) {
      newIndex = length;
    }

    if (newIndex < 0) {
      newIndex = 0;
    }

    this.selectionEndIndex = newIndex;
    if (this.scrollOffsetIndex > length) {
      this.scrollOffsetIndex = length;
    } else if (this.scrollOffsetIndex < 0) {
      this.scrollOffsetIndex = 0;
    }

    String visibleText = this.getVisibleText();
    int maxIndex = visibleText.length() + this.scrollOffsetIndex;
    if (newIndex == this.scrollOffsetIndex) {
      this.scrollOffsetIndex -= this.getVisibleTextReverse().length();
    }

    if (newIndex > maxIndex) {
      this.scrollOffsetIndex += newIndex - maxIndex;
    } else if (newIndex <= this.scrollOffsetIndex) {
      this.scrollOffsetIndex -= this.scrollOffsetIndex - newIndex;
    }

    this.scrollOffsetIndex = MathHelper.clamp_int(this.scrollOffsetIndex, 0, length);
  }

  /** Gets the text that fits into the text box. */
  private String getVisibleText() {
    int width = (int)(this.getEditableWidth().getGui() / this.textScale);
    return this.fontEngine.trimStringToWidth(this.getTextToRender().substring(this.scrollOffsetIndex), width);
  }

  /** Gets the tail-end of the text that fits into the text box. */
  private String getVisibleTextReverse() {
    int width = (int)(this.getContentBox().getWidth().getGui() / this.textScale);
    return this.fontEngine.trimStringToWidth(this.getTextToRender(), width, true);
  }

  /** Empty string if nothing is selected. */
  private String getSelectedText() {
    int indexStart = Math.min(this.cursorIndex, this.selectionEndIndex);
    int indexEnd = Math.max(this.cursorIndex, this.selectionEndIndex);
    return this.getTextToRender().substring(indexStart, indexEnd);
  }

  private void moveCursorBy(int delta) {
    this.setCursorIndex(this.selectionEndIndex + delta);
  }

  private void setCursorIndex(int newPosition) {
    this.cursorIndex = newPosition;
    int N = this.text.length();
    this.cursorIndex = MathHelper.clamp_int(this.cursorIndex, 0, N);
    this.scrollOffsetIndex = MathHelper.clamp_int(this.scrollOffsetIndex, 0, N);
    this.setSelectionIndex(this.cursorIndex);
  }

  private void setCursorPositionToStart() {
    this.setCursorIndex(0);
  }

  private void setCursorPositionToEnd() {
    this.setCursorIndex(this.text.length());
  }

  private Dim getEditableWidth() {
    return this.getContentBox().getWidth().minus(gui(this.fontEngine.getStringWidth(this.suffix) * this.textScale));
  }

  public enum InputType {
      TEXT, PASSWORD
  }
}
