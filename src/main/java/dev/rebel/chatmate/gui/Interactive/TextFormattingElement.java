package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.KeyboardEventData.KeyModifier;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.util.Collections;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class TextFormattingElement extends InlineElement {
  private static final Map<TextFormat, FormatInfo> FORMAT_INFO;

  static {
    FORMAT_INFO = new HashMap<>();
    FORMAT_INFO.put(TextFormat.WHITE, new FormatInfo('f', "White", Colour.WHITE, null));
    FORMAT_INFO.put(TextFormat.GRAY, new FormatInfo('7', "Gray", Colour.GREY67, null));
    FORMAT_INFO.put(TextFormat.DARK_GRAY, new FormatInfo('8', "Dark Gray", Colour.GREY33, null));
    FORMAT_INFO.put(TextFormat.BLACK, new FormatInfo('0', "Black", Colour.BLACK, null));
    FORMAT_INFO.put(TextFormat.DARK_RED, new FormatInfo('4', "Dark Red", Colour.DARK_RED, null));
    FORMAT_INFO.put(TextFormat.RED, new FormatInfo('c', "Red", Colour.RED, null));
    FORMAT_INFO.put(TextFormat.GOLD, new FormatInfo('6', "Gold", Colour.GOLD, null));
    FORMAT_INFO.put(TextFormat.YELLOW, new FormatInfo('e', "Yellow", Colour.YELLOW, null));
    FORMAT_INFO.put(TextFormat.DARK_GREEN, new FormatInfo('2', "Dark Green", Colour.DARK_GREEN, null));
    FORMAT_INFO.put(TextFormat.GREEN, new FormatInfo('a', "Green", Colour.GREEN, null));
    FORMAT_INFO.put(TextFormat.AQUA, new FormatInfo('b', "Aqua", Colour.AQUA, null));
    FORMAT_INFO.put(TextFormat.DARK_AQUA, new FormatInfo('3', "Dark Aqua", Colour.DARK_AQUA, null));
    FORMAT_INFO.put(TextFormat.DARK_BLUE, new FormatInfo('1', "Dark Blue", Colour.DARK_BLUE, null));
    FORMAT_INFO.put(TextFormat.BLUE, new FormatInfo('9', "Blue", Colour.BLUE, null));
    FORMAT_INFO.put(TextFormat.LIGHT_PURPLE, new FormatInfo('d', "Light Purple", Colour.LIGHT_PURPLE, null));
    FORMAT_INFO.put(TextFormat.DARK_PURPLE, new FormatInfo('5', "Purple", Colour.DARK_PURPLE, null));
    FORMAT_INFO.put(TextFormat.BOLD, new FormatInfo('l', "Bold", Colour.WHITE, Asset.LETTER_B, Keyboard.KEY_B));
    FORMAT_INFO.put(TextFormat.ITALIC, new FormatInfo('o', "Italic", Colour.WHITE, Asset.LETTER_I, Keyboard.KEY_I));
    FORMAT_INFO.put(TextFormat.UNDERLINE, new FormatInfo('n', "Underline", Colour.WHITE, Asset.LETTER_U, Keyboard.KEY_U));
    FORMAT_INFO.put(TextFormat.STRIKETHROUGH, new FormatInfo('m', "Strikethrough", Colour.WHITE, Asset.LETTER_S, Keyboard.KEY_S));
    FORMAT_INFO.put(TextFormat.OBFUSCATED, new FormatInfo('k', "Obfuscated", Colour.WHITE, Asset.LETTER_O, Keyboard.KEY_O));
    FORMAT_INFO.put(TextFormat.RESET, new FormatInfo('r', "Reset Styles", Colour.WHITE, Asset.LETTER_X, Keyboard.KEY_X));
  }

  private final BiConsumer<TextFormat, TextFormatState> onUpdate;
  private final Map<TextFormat, TextFormattingButton> elements;
  private final TextFormatStateBuilder stateBuilder;

  public TextFormattingElement(InteractiveScreen.InteractiveContext context, InputElement parent, Map<TextFormat, TextFormatState> initialState, BiConsumer<TextFormat, TextFormatState> onUpdate) {
    super(context, parent);
    this.onUpdate = onUpdate;

    // add the elements in order
    this.elements = new HashMap<>();
    for (TextFormat format : TextFormat.values()) {
      FormatInfo info = FORMAT_INFO.get(format);
      TextFormattingButton button = new TextFormattingButton(context, this, info)
          .setOnClick(() -> this.onClick(format))
          .cast();
      this.elements.put(format, button);
      super.addElement(button);
    }

    // importantly, this has to be done after initialising the elements
    this.stateBuilder = new TextFormatStateBuilder(initialState, this::onUpdateState);
  }

  private void onUpdateState(TextFormat format, TextFormatState state) {
    this.elements.get(format).setButtonState(state);
    this.onUpdate.accept(format, state);
  }

  private void onClick(TextFormat format) {
    this.toggleFormat(format);
    super.context.onSetFocus((InputElement)super.parent);
  }

  private void toggleFormat(TextFormat format) {
    TextFormatState newState = this.stateBuilder.getState(format) == TextFormatState.INACTIVE ? TextFormatState.ACTIVE : TextFormatState.INACTIVE;
    this.stateBuilder.withState(format, newState);
  }

  public TextFormatStateBuilder getStateBuilder() {
    return this.stateBuilder;
  }

  public @Nullable TextFormat getTextFormatFromChar(char c) {
    for (TextFormat format : TextFormat.values()) {
      FormatInfo info = FORMAT_INFO.get(format);
      if (info.code == c) {
        return format;
      }
    }

    return null;
  }

  @Override
  public void onKeyDown(InteractiveEvent<KeyboardEventData> e) {
    KeyboardEventData data = e.getData();
    if (!data.isKeyModifierActive(KeyModifier.CTRL)) {
      return;
    }

    for (TextFormat format : TextFormat.values()) {
      FormatInfo info = FORMAT_INFO.get(format);
      if (info.shortcut != null && info.shortcut == data.eventKey) {
        this.toggleFormat(format);
        e.stopPropagation();
        return;
      }
    }
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
      super.setBorder(new RectExtension(screen(1)));
      super.setMargin(new RectExtension(screen(1)));

      this.formatInfo = formatInfo;
      this.state = TextFormatState.INACTIVE;
    }

    public TextFormattingButton setButtonState(TextFormatState state) {
      this.state = state;

      RectExtension defaultMargin = new RectExtension(screen(1));
      if (state == TextFormatState.ACTIVE) {
        super.setMargin(defaultMargin.top(gui(1)));
      } else {
        super.setMargin(defaultMargin);
      }

      return this;
    }

    public TextFormatState getButtonState() {
      return this.state;
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
      Colour borderColour;
      if (super.isHovering() && this.getEnabled()) {
        borderColour = Colour.GREY75;
      } else {
        borderColour = Colour.WHITE;
      }

      RendererHelpers.drawRect(0, this.getPaddingBox(), this.formatInfo.colour, super.getBorder().left, borderColour);

      IElement childElement = Collections.first(super.getChildren());
      if (childElement != null) {
        childElement.render(null);
      }
    }
  }

  public static class TextFormatStateBuilder {
    private final @Nullable BiConsumer<TextFormat, TextFormatState> onUpdate;
    private final Map<TextFormat, TextFormatState> state;

    public TextFormatStateBuilder(Map<TextFormat, TextFormatState> initialState, @Nullable BiConsumer<TextFormat, TextFormatState> onUpdate) {
      this.onUpdate = onUpdate;

      this.state = new HashMap<>();
      for (TextFormat format : TextFormat.values()) {
        this.state.put(format, TextFormatState.INACTIVE);
      }

      this.reset();

      for (TextFormat format : initialState.keySet()) {
        this.withState(format, initialState.get(format));
      }
    }

    public void withState(TextFormat format, TextFormatState newState) {
      if (format == TextFormat.RESET) {
        this.reset();
        return;
      }

      if (FORMAT_INFO.get(format).isColour()) {
        if (newState == TextFormatState.ACTIVE) {
          // deactivate all other colours
          for (TextFormat f : TextFormat.values()) {
            if (f == format || !FORMAT_INFO.get(f).isColour()) {
              continue;
            }

            this.setStateUnsafe(f, TextFormatState.INACTIVE);
          }
        } else {
          // deactivating colours directly is not supported
          return;
        }
      }

      this.setStateUnsafe(format, newState);
    }

    public TextFormatState getState(TextFormat format) {
      return this.state.get(format);
    }

    public List<TextFormat> getActiveFormats() {
      List<TextFormat> result = new ArrayList<>();
      for (TextFormat format : TextFormat.values()) {
        if (this.state.getOrDefault(format, TextFormatState.INACTIVE) == TextFormatState.ACTIVE) {
          result.add(format);
        }
      }

      return result;
    }

    public void reset() {
      for (TextFormat format : TextFormat.values()) {
        TextFormatState newState = format == TextFormat.WHITE ? TextFormatState.ACTIVE : TextFormatState.INACTIVE;
        this.setStateUnsafe(format, newState);
      }
    }

    /** Does not check the validity of the state. */
    private void setStateUnsafe(TextFormat format, TextFormatState newState) {
      // notify subscriber of a changed state
      if (this.onUpdate != null && newState != this.state.get(format)) {
        this.onUpdate.accept(format, newState);
      }

      this.state.put(format, newState);
    }
  }

  private static class FormatInfo {
    public final char code;
    public final String tooltip;
    public final Colour colour;
    public final @Nullable Texture texture;
    public final @Nullable Integer shortcut;

    private FormatInfo(char code, String tooltip, Colour colour, @Nullable Texture texture) {
      this(code, tooltip, colour, texture, null);
    }

    private FormatInfo(char code, String tooltip, Colour colour, @Nullable Texture texture, @Nullable Integer shortcut) {
      this.code = code;
      this.tooltip = tooltip;
      this.colour = colour;
      this.texture = texture;
      this.shortcut = shortcut;
    }

    public boolean isColour() {
      return this.code >= '0' && this.code <= 'f';
    }
  }

  // this defines the order
  public enum TextFormat {
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

  public enum TextFormatState {
    ACTIVE, INACTIVE, PARTIAL
  }
}
