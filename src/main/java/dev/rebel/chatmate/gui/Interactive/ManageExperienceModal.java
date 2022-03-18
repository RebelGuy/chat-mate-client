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
import dev.rebel.chatmate.models.api.experience.ModifyExperienceRequest;
import dev.rebel.chatmate.models.api.experience.ModifyExperienceResponse.ModifyExperienceResponseData;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.services.McChatService;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.In;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

import static dev.rebel.chatmate.services.util.TextHelpers.isNullOrEmpty;

public class ManageExperienceModal extends ContainerElement {
  private final PublicUser user;
  private final ExperienceEndpointProxy experienceEndpointProxy;
  private final McChatService mcChatService;

  private final Dim width;
  private final Dim borderSize;
  private final Dim cornerRadius;
  private final Dim shadowDistance;

  private final LabelElement title;
  private final SideBySideElement levelSbs;
  private final SideBySideElement msgSbs;
  private final LabelElement errorLabel;
  private final HorizontalDivider divider;
  private final ButtonElement closeButton;
  private final ButtonElement submitButton;
  private final SideBySideElement footer;

  private @Nullable Float currentLevel = null;
  private String currentMessage = null;

  public ManageExperienceModal(InteractiveScreen.InteractiveContext context, InteractiveScreen parent, PublicUser user, ExperienceEndpointProxy experienceEndpointProxy, McChatService mcChatService) {
    super(context, parent, LayoutMode.INLINE);
    this.user = user;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.mcChatService = mcChatService;

    this.width = gui(200);
    this.borderSize = gui(1);
    this.cornerRadius = gui(5);
    this.shadowDistance = gui(3);
    this.setPadding(new Layout.RectExtension(context.dimFactory.fromGui(10)));
    this.setBorder(new RectExtension(this.borderSize));
    this.setHorizontalAlignment(HorizontalAlignment.CENTRE);
    this.setVerticalAlignment(VerticalAlignment.MIDDLE);
    this.setFocusable(true);

    this.title = (LabelElement)new LabelElement(context, this)
        .setText("Manage Experience for " + user.userInfo.channelName)
        .setAlignment(LabelElement.TextAlignment.CENTRE)
        .setOverflow(LabelElement.TextOverflow.SPLIT)
        .setSizingMode(SizingMode.FILL)
        .setMargin(new Layout.RectExtension(ZERO, ZERO, ZERO, context.dimFactory.fromGui(15)));

    this.levelSbs = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(gui(10))
        .addElement(1,
            new LabelElement(context, this)
                .setText("Add level:")
                .setOverflow(TextOverflow.TRUNCATE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE))
        .addElement(1,
            new TextInputElement(context, this)
                .onTextChange(this::onLevelChange))
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5)));

    this.msgSbs = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(gui(10))
        .addElement(1,
            new LabelElement(context, this)
                .setText("Message:")
                .setOverflow(TextOverflow.TRUNCATE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE))
        .addElement(1,
            new TextInputElement(context, this)
                .onTextChange(this::onMessageChange))
        .setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5)));

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
        .setOnClick(this::onSubmit)
        .setEnabled(false);
    this.footer = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(this.width.over(2))
        .addElement(1, this.closeButton)
        .addElement(1, this.submitButton)
        .setPadding(new Layout.RectExtension(ZERO, ZERO, context.dimFactory.fromGui(10), ZERO));
  }

  private void onLevelChange(String maybeLevel) {
    try {
      float level = Float.parseFloat(maybeLevel);
      if (level != 0 && Math.abs(level) <= 100) {
        this.currentLevel = level;
        this.submitButton.setEnabled(true);
      } else {
        this.currentLevel = null;
        this.submitButton.setEnabled(false);
      }
    } catch (Exception ignored) {
      this.currentLevel = null;
      this.submitButton.setEnabled(false);
    }
  }

  private void onMessageChange(String message) {
    this.currentMessage = message;
  }

  private void onSubmit() {
    if (this.currentLevel == null) {
      this.errorLabel.setText("Invalid level").setVisible(true);
      return;
    }
    this.errorLabel.setVisible(false);
    this.setLoading(true);

    String msg = isNullOrEmpty(this.currentMessage) ? null : this.currentMessage.trim();
    ModifyExperienceRequest request = new ModifyExperienceRequest(this.user.id, this.currentLevel, msg);
    this.experienceEndpointProxy.modifyExperienceAsync(request, this::onModifyExperienceResponse, this::onModifyExperienceError);
  }

  private void onModifyExperienceResponse(ModifyExperienceResponseData response) {
    this.setLoading(false);

    this.mcChatService.printInfo("Successfully modified experience for " + response.updatedUser.userInfo.channelName + ".");
    this.onCloseScreen();
  }

  private void onModifyExperienceError(Throwable error) {
    this.setLoading(false);

    this.mcChatService.printError(error);

    String msg = EndpointProxy.getApiErrorMessage(error);
    this.errorLabel.setText(msg).setVisible(true);
  }

  private void setLoading(boolean loading) {
    this.closeButton.setEnabled(!loading);
    this.submitButton.setEnabled(!loading && this.isFormValid());
  }

  private void onClose() {
    super.onCloseScreen();
  }

  private boolean isFormValid() {
    return this.currentLevel != null;
  }

  @Override
  public void onKeyDown(IEvent<In> e) {
    if (e.getData().eventKey == Keyboard.KEY_RETURN && this.isFormValid()) {
      this.onSubmit();
    }
  }

  @Override
  public void onCreate() {
    this.addElement(this.title);

    this.addElement(this.levelSbs);
    this.addElement(this.msgSbs);
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
    RendererHelpers.drawRect(this.getZIndex(), this.getPaddingBox(), background, this.borderSize, Colour.BLACK, this.cornerRadius, this.shadowDistance);

    super.renderElement();
  }
}
