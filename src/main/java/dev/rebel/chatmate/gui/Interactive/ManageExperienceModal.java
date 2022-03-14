package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.HorizontalDivider.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
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

    this.divider = new HorizontalDivider(context, this)
        .setMode(SizingMode.PARENT_FULL);

    this.closeButton = new ButtonElement(context, this)
        .setText("Close");
    this.submitButton = new ButtonElement(context, this)
        .setText("Submit");

    this.footer = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(this.width.over(2))
        .addElement(this.closeButton, 1)
        .addElement(this.submitButton, 1)
        .setPadding(new Layout.RectExtension(ZERO, context.dimFactory.fromGui(4)));

  }

  @Override
  public void onCreate() {
    this.addElement(this.title);



    this.addElement(this.divider);
    this.addElement(this.footer);
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    return this.setLastCalculatedSize(super.calculateSize(Dim.min(maxWidth, this.width)));
  }

  @Override
  protected DimPoint setLastCalculatedSize(DimPoint size) {
    this.lastCalculatedSize = size;
    return size;
  }

  @Override
  public void renderElement() {
    Colour backgroundTop = new Colour(64, 64, 64, 127); // light gray
    Colour backgroundBottom = new Colour(32, 32, 32, 127); // dark gray
    RendererHelpers.renderRect(this.getZIndex(), this.getBox(), backgroundTop, backgroundBottom, this.context.dimFactory.fromGui(1), null, null);

    super.renderElement();
  }
}
