package dev.rebel.chatmate.gui.Interactive;

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
//  private FooterElement footer;

  public ManageExperienceModal(InteractiveScreen.InteractiveContext context, InteractiveScreen parent, PublicUser user, ExperienceEndpointProxy experienceEndpointProxy, McChatService mcChatService) {
    super(context, parent, LayoutMode.INLINE);
    this.user = user;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.mcChatService = mcChatService;

    this.width = context.dimFactory.fromGui(200);
    this.setPadding(new Layout.RectExtension(context.dimFactory.fromGui(10)));

    this.title = (LabelElement)new LabelElement(context, this)
        .setText("Manage Experience for " + user.userInfo.channelName)
        .setAlignment(LabelElement.TextAlignment.CENTRE)
        .setOverflow(LabelElement.TextOverflow.SPLIT)
        .setLayoutMode(LabelElement.LayoutMode.FULL_WIDTH)
        .setMargin(new Layout.RectExtension(ZERO, ZERO, ZERO, context.dimFactory.fromGui(30)));

//    this.footer = new FooterElement(context, this)
  }

  @Override
  public void onCreate() {
    this.addElement(this.title);
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    return super.calculateSize(Dim.min(maxWidth, this.getContentBoxWidth(this.width)));
  }

  @Override
  public void render() {
    Colour backgroundTop = new Colour(-1072689136);
    Colour backgroundBottom = new Colour(-804253680);
    RendererHelpers.renderRect(this.getBox(), backgroundTop, backgroundBottom, this.context.dimFactory.fromGui(1), null, null);

    super.render();
  }
}
