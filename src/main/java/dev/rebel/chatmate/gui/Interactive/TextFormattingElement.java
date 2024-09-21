package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.KeyboardEventData.KeyModifier;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.IconButtonElement;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.TriConsumer;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class TextFormattingElement extends InlineElement {
  public static final Map<TextFormat, FormatInfo> FORMAT_INFO;

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
    this.stateBuilder = new TextFormatStateBuilder(initialState, this::onUpdateState, true);
  }

  private void onUpdateState(TextFormat format, TextFormatState state, boolean notifyListener) {
    this.elements.get(format).setButtonState(state);
    if (!notifyListener) {
      return;
    }

    try {
      this.onUpdate.accept(format, state);
    } catch (Exception e) {
      super.context.logService.logError(this, "Unable to notify subscriber of state update", e);
    }
  }

  private void onClick(TextFormat format) {
    super.context.onSetFocus((InputElement)super.parent);
    this.toggleFormat(format);
  }

  private void toggleFormat(TextFormat format) {
    TextFormatState newState = this.stateBuilder.getState(format) == TextFormatState.INACTIVE ? TextFormatState.ACTIVE : TextFormatState.INACTIVE;
    this.stateBuilder.withState(format, newState, true);
  }

  public TextFormatStateBuilder getStateBuilder() {
    return this.stateBuilder;
  }

  public static @Nullable TextFormat getTextFormatFromChar(char c) {
    for (TextFormat format : TextFormat.values()) {
      FormatInfo info = FORMAT_INFO.get(format);
      if (info.code == c) {
        return format;
      }
    }

    return null;
  }

  public static String getStringFromTextFormat(TextFormat format) {
    FormatInfo info = FORMAT_INFO.get(format);
    return "§" + info.code;
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
      if (this.formatInfo == FORMAT_INFO.get(TextFormat.RESET)) {
        return this;
      }

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
    private final @Nullable TriConsumer<TextFormat, TextFormatState, Boolean> onUpdate;
    private final boolean enforceWhite;
    private final Map<TextFormat, TextFormatState> state;

    public TextFormatStateBuilder(@Nullable Map<TextFormat, TextFormatState> initialState, @Nullable TriConsumer<TextFormat, TextFormatState, Boolean> onUpdate, boolean enforceWhite) {
      this.onUpdate = onUpdate;
      this.enforceWhite = enforceWhite;

      this.state = new HashMap<>();
      for (TextFormat format : TextFormat.values()) {
        this.state.put(format, TextFormatState.INACTIVE);
      }

      this.reset(false);

      if (initialState != null) {
        for (TextFormat format : initialState.keySet()) {
          this.withState(format, initialState.get(format), false);
        }
      }
    }

    /** Returns true if changes were made to the state. */
    public boolean withState(TextFormat format, TextFormatState newState, boolean notifyListener) {
      if (format == TextFormat.RESET) {
        return this.reset(notifyListener);
      }

      boolean madeChange = false;
      if (FORMAT_INFO.get(format).isColour()) {
        if (newState == TextFormatState.ACTIVE) {
          // deactivate all other colours
          for (TextFormat f : TextFormat.values()) {
            if (f == format || !FORMAT_INFO.get(f).isColour()) {
              continue;
            }

            madeChange = this.setStateUnsafe(f, TextFormatState.INACTIVE, notifyListener);
          }
        } else {
          // deactivating colours directly is not supported
          return false;
        }
      }

      return this.setStateUnsafe(format, newState, notifyListener) || madeChange;
    }

    public TextFormatState getState(TextFormat format) {
      return this.state.get(format);
    }

    public Set<TextFormat> getActiveFormats() {
      Set<TextFormat> result = new HashSet<>();
      for (TextFormat format : TextFormat.values()) {
        if (this.state.getOrDefault(format, TextFormatState.INACTIVE) == TextFormatState.ACTIVE) {
          result.add(format);
        }
      }

      return result;
    }

    /** Only notifies listeners of the RESET state change, not any individual states that were reset as a side effect. */
    public boolean reset(boolean notifyListener) {
      boolean madeChanges = false;
      for (TextFormat format : TextFormat.values()) {
        TextFormatState newState = this.enforceWhite && format == TextFormat.WHITE ? TextFormatState.ACTIVE : TextFormatState.INACTIVE;
        boolean _madeChanges = this.setStateUnsafe(format, newState, false);
        if (_madeChanges) {
          madeChanges = true;
        }
      }

      if (notifyListener && this.onUpdate != null) {
        this.onUpdate.accept(TextFormat.RESET, TextFormatState.ACTIVE, true);
      }
      return madeChanges;
    }

    /** Returns the string that should be added to text to get from the given formatting state to the current one. */
    public String diffString(TextFormatStateBuilder fromState) {
      // if a non-colour format exists in the first state, but not current state, we need to perform a complete reset
      // since non-colour formats cannot be overwritten, unlike colours.
      boolean requiresReset = false;
      Set<TextFormat> initialFormats = fromState.getActiveFormats();
      Set<TextFormat> currentFormats = this.getActiveFormats();
      for (TextFormat initialFormat : initialFormats) {
        if (!FORMAT_INFO.get(initialFormat).isColour() && !currentFormats.contains(initialFormat)) {
          requiresReset = true;
          initialFormats = new HashSet<>();
          initialFormats.add(TextFormat.WHITE);
          break;
        }
      }

      StringBuilder result = new StringBuilder();
      if (requiresReset) {
        result.append(FORMAT_INFO.get(TextFormat.RESET).print());
      }

      for (TextFormat currentFormat : currentFormats) {
        if (!initialFormats.contains(currentFormat)) {
          // new format
          result.append(FORMAT_INFO.get(currentFormat).print());
        } else if (requiresReset && initialFormats.contains(currentFormat)) {
          // in the case where we had to reset, ensure any initial formats are continued
          result.append(FORMAT_INFO.get(currentFormat).print());
        }
      }

      return result.toString();
    }

    /** Does not check the validity of the state. */
    private boolean setStateUnsafe(TextFormat format, TextFormatState newState, boolean notifyListener) {
      TextFormatState oldState = this.state.get(format);
      this.state.put(format, newState);

      // notify subscriber of a changed state
      if (newState != oldState) {
        if (this.onUpdate != null) {
          this.onUpdate.accept(format, newState, notifyListener);
        }

        return true;
      }

      return false;
    }
  }

  public static class FormatInfo {
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

    public String print() {
      return FontEngine.SECTION_SIGN_STRING + this.code;
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
