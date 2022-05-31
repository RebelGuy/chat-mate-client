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

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.TextHelpers.isNullOrEmpty;

public abstract class ModalElement extends ContainerElement {
  protected Dim width;
  private final Dim borderSize;
  private final Dim cornerRadius;
  private final Dim shadowDistance;

  private LabelElement title;
  private ElementReference bodyElement;
  private LabelElement errorLabel;
  private HorizontalDivider divider;
  private ButtonElement closeButton;
  private ButtonElement submitButton;
  private SideBySideElement footer;

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
  }

  /** Do not call this from the constructor. */
  public ModalElement setBody(IElement bodyElement) {
    this.bodyElement.setUnderlyingElement(bodyElement);
    return this;
  }

  /** Do not call this from the constructor. */
  public ModalElement setTitle(String title) {
    this.title.setText(title);
    return this;
  }

  /** Do not call this from the constructor. */
  public ModalElement setSubmitText(String submitText) {
    this.submitButton.setText(submitText);
    return this;
  }

  /** Do not call this from the constructor. */
  public ModalElement setCloseText(String closeText) {
    this.closeButton.setText(closeText);
    return this;
  }

  /** Should return true/false if the modal's submit button can be pressed, or null to hide the submit button. */
  protected abstract @Nullable Boolean validate();

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
    this.context.renderer.runSideEffect(() -> {
      this.setLoading(false);
      this.errorLabel.setText(isNullOrEmpty(errorMessage) ? "Something went wrong." : errorMessage).setVisible(true);
    });
  }

  private void onSubmitSuccess() {
    this.context.renderer.runSideEffect(() -> this.setLoading(false));
  }

  private void setLoading(boolean loading) {
    this.loading = loading;
    this.errorLabel.setVisible(false);
    this.closeButton.setEnabled(this, !this.loading);
    this.setSubmitButton();
  }

  private void setSubmitButton() {
    @Nullable Boolean valid = this.validate();
    if (valid == null) {
      this.submitButton.setVisible(false);
    } else {
      this.submitButton.setVisible(true);
      this.submitButton.setEnabled(this, !loading && valid);
    }
  }

  private void onClose() {
    this.errorLabel.setVisible(false);
    this.close();
  }

  @Override
  public void onKeyDown(IEvent<In> e) {
    if (e.getData().eventKey == Keyboard.KEY_RETURN && this.validate()) {
      this.onSubmit();
    }
  }

  @Override
  public void onInitialise() {
    super.onInitialise();

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
        .setAlignment(TextAlignment.CENTRE)
        .setMaxLines(5)
        .setSizingMode(SizingMode.FILL)
        .setVisible(false)
        .setPadding(new RectExtension(ZERO, gui(4)));

    this.divider = new HorizontalDivider(context, this)
        .setMode(FillMode.PARENT_FULL);

    this.closeButton = new ButtonElement(context, this)
        .setText("Close")
        .setOnClick(this::onClose);
    this.submitButton = (ButtonElement)new ButtonElement(context, this)
        .setText("Submit")
        .setOnClick(this::onSubmit)
        .setVisible(this.validate() != null);
    this.footer = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(this.width.over(2))
        .addElement(1, this.closeButton)
        .addElement(1, this.submitButton)
        .setPadding(new Layout.RectExtension(ZERO, ZERO, context.dimFactory.fromGui(10), ZERO));

    this.addElement(this.title);

    this.addElement(this.bodyElement);
    this.addElement(this.errorLabel);

    this.addElement(this.divider);
    this.addElement(this.footer);
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    return super.calculateThisSize(Dim.min(maxContentSize, this.width));
  }

  @Override
  public void renderElement() {
    Colour background = new Colour(0, 0, 0, 127); // dark gray
    RendererHelpers.drawRect(this.getZIndex(), this.getPaddingBox(), background, this.borderSize, Colour.BLACK, this.cornerRadius, this.shadowDistance, Colour.BLACK);

    this.closeButton.setEnabled(this, !this.loading);
    this.setSubmitButton();

    super.renderElement();
  }
}
