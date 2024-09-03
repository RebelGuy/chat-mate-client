package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TextFormattingElement extends InlineElement {
  private final Map<TextFormat, TextFormattingButton> elements;

  public TextFormattingElement(InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    Map<TextFormat, FormatInfo> formatInfo = new HashMap<>();
    formatInfo.put(TextFormat.WHITE, new FormatInfo('f', "White", Colour.WHITE, null));
    formatInfo.put(TextFormat.GRAY, new FormatInfo('7', "Gray", Colour.GREY67, null));
    formatInfo.put(TextFormat.DARK_GRAY, new FormatInfo('8', "Dark Gray",Colour.GREY33, null));
    formatInfo.put(TextFormat.BLACK, new FormatInfo('0', "Black", Colour.BLACK, null));
    formatInfo.put(TextFormat.DARK_RED, new FormatInfo('4', "Dark Red",Colour.DARK_RED, null));
    formatInfo.put(TextFormat.RED, new FormatInfo('c', "Red", Colour.RED, null));
    formatInfo.put(TextFormat.GOLD, new FormatInfo('6', "Gold", Colour.GOLD, null));
    formatInfo.put(TextFormat.YELLOW, new FormatInfo('e', "Yellow", Colour.YELLOW, null));
    formatInfo.put(TextFormat.DARK_GREEN, new FormatInfo('2', "Dark Green",Colour.DARK_GREEN, null));
    formatInfo.put(TextFormat.GREEN, new FormatInfo('a', "Green", Colour.GREEN, null));
    formatInfo.put(TextFormat.AQUA, new FormatInfo('b', "Aqua", Colour.AQUA, null));
    formatInfo.put(TextFormat.DARK_AQUA, new FormatInfo('3', "Dark Aqua",Colour.DARK_AQUA, null));
    formatInfo.put(TextFormat.DARK_BLUE, new FormatInfo('1', "Dark Blue",Colour.DARK_BLUE, null));
    formatInfo.put(TextFormat.BLUE, new FormatInfo('9', "Blue", Colour.BLUE, null));
    formatInfo.put(TextFormat.LIGHT_PURPLE, new FormatInfo('d', "Light Purple",Colour.LIGHT_PURPLE, null));
    formatInfo.put(TextFormat.DARK_PURPLE, new FormatInfo('5', "Purple", Colour.DARK_PURPLE, null));
    formatInfo.put(TextFormat.BOLD, new FormatInfo('l', "Bold", Colour.WHITE, Asset.LETTER_B));
    formatInfo.put(TextFormat.ITALIC, new FormatInfo('o', "Italic", Colour.WHITE, Asset.LETTER_I));
    formatInfo.put(TextFormat.UNDERLINE, new FormatInfo('n', "Underline", Colour.WHITE, Asset.LETTER_U));
    formatInfo.put(TextFormat.STRIKETHROUGH, new FormatInfo('m', "Strikethrough", Colour.WHITE, Asset.LETTER_S));
    formatInfo.put(TextFormat.OBFUSCATED, new FormatInfo('k', "Obfuscated", Colour.WHITE, Asset.LETTER_O));
    formatInfo.put(TextFormat.RESET, new FormatInfo('r', "Reset Styles",Colour.WHITE, Asset.LETTER_X));

    // add the elements in order
    this.elements = new HashMap<>();
    for (TextFormat format : TextFormat.values()) {
      FormatInfo info = formatInfo.get(format);
      TextFormattingButton button = new TextFormattingButton(context, this, info);
      this.elements.put(format, button);
      super.addElement(button);
    }
  }

  public TextFormattingElement setState(TextFormat format, TextFormatState state) {
    this.elements.get(format).setButtonState(state);
    return this;
  }

  /** This should be called with the parent's box - we manage our own box internally. */
  @Override
  public void setBox(DimRect box) {
    // tack on our box right underneath the parent element
    super.setBox(new DimRect(box.getBottomLeft(), super.lastCalculatedSize));
  }

  private static class TextFormattingButton extends IconButtonElement {
    private final FormatInfo formatInfo;
    private TextFormatState state;

    public TextFormattingButton(InteractiveScreen.InteractiveContext context, IElement parent, FormatInfo formatInfo) {
      super(context, parent);
      super.setBorderColour(Colour.WHITE);
      super.setBorderCornerRadius(gui(0));
      super.setPadding(new RectExtension(gui(0)));
      super.setTooltip(formatInfo.tooltip);
      super.setTargetHeight(gui(8));
      super.setImage(formatInfo.texture);

      this.formatInfo = formatInfo;
      this.state = TextFormatState.INACTIVE;
    }

    public TextFormattingButton setButtonState(TextFormatState state) {
      this.state = state;
      return this;
    }

    @Override
    protected DimPoint calculateThisSize(Dim maxContentSize) {
      IElement childElement = Collections.first(super.getChildren());
      if (childElement != null) {
        childElement.calculateSize(maxContentSize);
      }

      Dim size = gui(4);
      return new DimPoint(size, size);
    }

    @Override
    protected void renderElement() {
      Dim borderWidth = super.getBorder().left.over(2);
      Colour borderColour = super.isHovering() && this.getEnabled() ? Colour.GREY75 : Colour.WHITE;
      RendererHelpers.drawRect(0, this.getPaddingBox(), this.formatInfo.colour, borderWidth, borderColour);

      IElement childElement = Collections.first(super.getChildren());
      if (childElement != null) {
        childElement.render(null);
      }
    }
  }

  private static class FormatInfo {
    public final char code;
    public final String tooltip;
    public final Colour colour;
    public final @Nullable Texture texture;

    private FormatInfo(char code, String tooltip, Colour colour, @Nullable Texture texture) {
      this.code = code;
      this.tooltip = tooltip;
      this.colour = colour;
      this.texture = texture;
    }
  }

  // this defines the order
  private enum TextFormat {
    WHITE,
    GRAY,
    DARK_GRAY,
    BLACK,
    DARK_RED,
    RED,
    GOLD,
    YELLOW,
    DARK_GREEN,
    GREEN,
    AQUA,
    DARK_AQUA,
    DARK_BLUE,
    BLUE,
    LIGHT_PURPLE,
    DARK_PURPLE,
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    OBFUSCATED,
    RESET
  }

  private enum TextFormatState {
    ACTIVE, INACTIVE, PARTIAL
  }
}
