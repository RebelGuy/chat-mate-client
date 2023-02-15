package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.api.models.chat.GetCommandStatusResponse;
import dev.rebel.chatmate.api.models.chat.GetCommandStatusResponse.CommandStatus;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.style.Colour;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;

public class ChatCommandChatComponent extends InteractiveElementChatComponent {
  private final InteractiveContext context;
  private final Integer commandId;

  private final LoadingSpinnerElement loadingSpinnerElement;
  private final ImageElement successElement;
  private final ImageElement failureElement;

  private @Nullable CommandStatus lastStatus = null;

  public ChatCommandChatComponent(InteractiveContext context, Integer commandId) {
    super(context);

    this.context = context;
    this.commandId = commandId;

    this.loadingSpinnerElement = new LoadingSpinnerElement(context, super.screen)
        .setLineWidth(context.dimFactory.fromGui(1.5f));
    this.successElement = new ImageElement(context, super.screen)
        .setImage(Asset.GUI_TICK_ICON)
        .setColour(Colour.GREEN)
        .setVisible(false)
        .cast();
    this.failureElement = new ImageElement(context, super.screen)
        .setImage(Asset.GUI_CLEAR_ICON)
        .setColour(Colour.RED)
        .setVisible(false)
        .cast();

    super.screen.setMainElement(new InlineElement(context, super.screen)
        .addElement(this.loadingSpinnerElement)
        .addElement(this.successElement)
        .addElement(this.failureElement)
    );
  }

  @Override
  public String getUnformattedTextForChat() {
    return "";
  }

  @Override
  public IChatComponent createCopy() {
    return this;
  }

  @Override
  public void onPreRender(Dim x, Dim y) {
    // todo poll status here - if changed, perform side effect

    super.screen.getMainElement().setMargin(new RectExtension(x, this.context.dimFactory.zeroGui(), y, this.context.dimFactory.zeroGui()));
  }
}
