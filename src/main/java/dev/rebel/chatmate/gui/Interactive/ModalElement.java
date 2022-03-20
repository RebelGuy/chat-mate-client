package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.HorizontalDivider.FillMode;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextAlignment;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.In;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.TextHelpers.isNullOrEmpty;

public abstract class ModalElement extends ContainerElement {
  private Dim width;
  private final Dim borderSize;
  private final Dim cornerRadius;
  private final Dim shadowDistance;

  private final LabelElement title;
  private final ElementReference bodyElement;
  private final LabelElement errorLabel;
  private final HorizontalDivider divider;
  private final ButtonElement closeButton;
  private final ButtonElement submitButton;
  private final SideBySideElement footer;

  private boolean loading = false;

  public ModalElement(InteractiveScreen.InteractiveContext context, InteractiveScreen parent) {
    super(context, parent, LayoutMode.INLINE);
    this.width = gui(200);
    this.borderSize = gui(1);
    this.cornerRadius = gui(5);
    this.shadowDistance = gui(3);
    this.setPadding(new Layout.RectExtension(context.dimFactory.fromGui(10)));
    this.setBorder(new RectExtension(this.borderSize));
    this.setHorizontalAlignment(HorizontalAlignment.CENTRE);
    this.setVerticalAlignment(VerticalAlignment.MIDDLE);
    this.setSizingMode(SizingMode.FILL);

    this.title = (LabelElement)new LabelElement(context, this)
        .setAlignment(LabelElement.TextAlignment.CENTRE)
        .setOverflow(LabelElement.TextOverflow.SPLIT)
        .setSizingMode(SizingMode.FILL)
        .setMargin(new Layout.RectExtension(ZERO, ZERO, ZERO, context.dimFactory.fromGui(15)))
        .setVisible(true);

    this.bodyElement = new ElementReference(context, this);

    this.errorLabel = (LabelElement)new LabelElement(context, this)
        .setColour(Colour.RED)
        .setFontScale(0.75f)
        .setOverflow(TextOverflow.SPLIT)
        .setSizingMode(SizingMode.FILL)
        .setAlignment(TextAlignment.CENTRE)
        .setMaxLines(5)
        .setVisible(false)
        .setPadding(new RectExtension(ZERO, gui(4)));

    this.divider = new HorizontalDivider(context, this)
        .setMode(FillMode.PARENT_FULL);

    this.closeButton = new ButtonElement(context, this)
        .setText("Close")
        .setOnClick(this::onClose);
    this.submitButton = new ButtonElement(context, this)
        .setText("Submit")
        .setOnClick(this::onSubmit);
    this.footer = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(this.width.over(2))
        .addElement(1, this.closeButton)
        .addElement(1, this.submitButton)
        .setPadding(new Layout.RectExtension(ZERO, ZERO, context.dimFactory.fromGui(10), ZERO));
  }

  public ModalElement setBody(IElement bodyElement) {
    this.bodyElement.setUnderlyingElement(bodyElement);
    return this;
  }

  public ModalElement setTitle(String title) {
    this.title.setText(title);
    return this;
  }

  /** Should return true if the modal's submit button can be pressed. */
  protected abstract boolean validate();

  /** Called when the submit button is pressed and the validator returns true. Allows for async behaviour.
   * The Runnable should be called if submission was successful, the Consumer should be called if submission failed and provide an optional error message. */
  protected abstract void submit(Runnable onSuccess, Consumer<String> onError);

  /** Called after the close button is pressed, but before the screen is closed. If not implemented, the default behaviour is to close the modal screen. */
  protected void close() {
    super.onCloseScreen();
  }

  private void onSubmit() {
    this.setLoading(true);
    this.submit(this::onSubmitSuccess, this::onSubmitError);
  }

  private void onSubmitError(String errorMessage) {
    this.setLoading(false);
    this.errorLabel.setText(isNullOrEmpty(errorMessage) ? "Something went wrong." : errorMessage).setVisible(true);
  }

  private void onSubmitSuccess() {
    this.setLoading(false);
  }

  private void setLoading(boolean loading) {
    this.loading = loading;
    this.errorLabel.setVisible(false);
    this.closeButton.setEnabled(this, !this.loading);
    this.submitButton.setEnabled(this, !loading && this.validate());
  }

  private void onClose() {
    this.close();
    super.onCloseScreen();
  }

  @Override
  public void onKeyDown(IEvent<In> e) {
    if (e.getData().eventKey == Keyboard.KEY_RETURN && this.validate()) {
      this.onSubmit();
    }
  }

  @Override
  public void onCreate() {
    this.addElement(this.title);

    this.addElement(this.bodyElement);
    this.addElement(this.errorLabel);

    this.addElement(this.divider);
    this.addElement(this.footer);

    super.onCreate();
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    return super.calculateThisSize(Dim.min(maxContentSize, this.width));
  }

  @Override
  public void renderElement() {
    Colour background = new Colour(0, 0, 0, 127); // dark gray
    RendererHelpers.drawRect(this.getZIndex(), this.getPaddingBox(), background, this.borderSize, Colour.BLACK, this.cornerRadius, this.shadowDistance);

    this.closeButton.setEnabled(this, !this.loading);
    this.submitButton.setEnabled(this, !loading && this.validate());

    super.renderElement();
  }
}
