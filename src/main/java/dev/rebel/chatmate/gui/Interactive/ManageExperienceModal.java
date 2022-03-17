package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.HorizontalDivider.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.services.McChatService;

public class ManageExperienceModal extends ContainerElement {
  private final PublicUser user;
  private final ExperienceEndpointProxy experienceEndpointProxy;
  private final McChatService mcChatService;

  private final Dim width;

  private LabelElement title;
  private SideBySideElement levelSbs;
  private TextInputElement msgSbs;
  private HorizontalDivider divider;
  private ButtonElement closeButton;
  private ButtonElement submitButton;
  private SideBySideElement footer;

  public ManageExperienceModal(InteractiveScreen.InteractiveContext context, InteractiveScreen parent, PublicUser user, ExperienceEndpointProxy experienceEndpointProxy, McChatService mcChatService) {
    super(context, parent, LayoutMode.INLINE);
    this.user = user;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.mcChatService = mcChatService;

    this.width = context.dimFactory.fromGui(200);
    this.setPadding(new Layout.RectExtension(context.dimFactory.fromGui(10)));
    this.setHorizontalAlignment(HorizontalAlignment.CENTRE);
    this.setVerticalAlignment(VerticalAlignment.MIDDLE);

    this.title = (LabelElement)new LabelElement(context, this)
        .setText("Manage Experience for " + user.userInfo.channelName)
        .setAlignment(LabelElement.TextAlignment.CENTRE)
        .setOverflow(LabelElement.TextOverflow.SPLIT)
        .setLayoutMode(LabelElement.LayoutMode.FULL_WIDTH)
        .setMargin(new Layout.RectExtension(ZERO, ZERO, ZERO, context.dimFactory.fromGui(30)));

    this.msgSbs = (TextInputElement)new TextInputElement(context, this)
        .setMargin(new RectExtension(ZERO, context.dimFactory.fromGui(10)));


    this.divider = new HorizontalDivider(context, this)
        .setMode(SizingMode.PARENT_FULL);

    this.closeButton = new ButtonElement(context, this)
        .setText("Close")
        .setOnClick(this::onClose);
    this.submitButton = new ButtonElement(context, this)
        .setText("Submit")
        .setOnClick(this::onSubmit);
    this.footer = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(this.width.over(2))
        .addElement(this.closeButton, 1)
        .addElement(this.submitButton, 1)
        .setPadding(new Layout.RectExtension(ZERO, ZERO, context.dimFactory.fromGui(10), ZERO));
  }

  private void onSubmit() {

  }

  private void onClose() {
    super.onCloseScreen();
  }

  @Override
  public void onCreate() {
    this.addElement(this.title);

//    this.addElement(this.levelSbs);
    this.addElement(this.msgSbs);

    this.addElement(this.divider);
    this.addElement(this.footer);
  }

  @Override
  public DimPoint calculateThisSize(Dim maxFullWidth) {
    return super.calculateThisSize(Dim.min(maxFullWidth, this.width));
  }

  @Override
  public void renderElement() {
    Colour backgroundTop = new Colour(64, 64, 64, 127); // light gray
    Colour backgroundBottom = new Colour(32, 32, 32, 127); // dark gray
    RendererHelpers.renderRect(this.getZIndex(), this.getBox(), backgroundTop, backgroundBottom, this.context.dimFactory.fromGui(1), null, null);

    super.renderElement();
  }
}
