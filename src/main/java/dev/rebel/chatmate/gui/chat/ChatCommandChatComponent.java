package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.api.models.chat.GetCommandStatusResponse.CommandStatus;
import dev.rebel.chatmate.api.models.chat.GetCommandStatusResponse.GetCommandStatusResponseData;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.style.Colour;
import net.minecraft.util.IChatComponent;
import org.lwjgl.Sys;

import javax.annotation.Nullable;

public class ChatCommandChatComponent extends InteractiveElementChatComponent {
  private final InteractiveContext context;
  private final int commandId;

  private final LoadingSpinnerElement loadingSpinnerElement;
  private final ImageElement successElement;
  private final ImageElement errorElement;

  private @Nullable CommandStatus lastStatus = null;

  public ChatCommandChatComponent(InteractiveContext context, int commandId) {
    super(context);

    this.context = context;
    this.commandId = commandId;

    this.loadingSpinnerElement = new LoadingSpinnerElement(context, super.screen)
        .setLineWidth(context.dimFactory.fromGui(1.2f));
    this.successElement = new ImageElement(context, super.screen)
        .setImage(Asset.GUI_TICK_ICON)
        .setVisible(false)
        .setTooltip("The command has not yet completed.")
        .setSizingMode(SizingMode.MINIMISE)
        .cast();
    this.errorElement = new ImageElement(context, super.screen)
        .setImage(Asset.GUI_CLEAR_ICON)
        .setVisible(false)
        .setSizingMode(SizingMode.MINIMISE)
        .cast();

    super.screen.setMainElement(
        new WrapperElement(context, super.screen,
            new InlineElement(context, super.screen)
              .addElement(this.loadingSpinnerElement)
              .addElement(this.successElement)
              .addElement(this.errorElement)
              .setOnClick(() -> System.out.println("CLICKED"))
        )
    );
  }

  @Override
  public void dispose() {
    this.context.commandApiStore.clearCommand(this.commandId);
    super.dispose();
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
  public void onPreRender(Dim x, Dim y, int opacity) {
    @Nullable GetCommandStatusResponseData data = this.context.commandApiStore.getCommandStatus(this.commandId);
    @Nullable CommandStatus newStatus = data == null ? null : data.status;
    if (newStatus != this.lastStatus) {
      this.lastStatus = newStatus;
      this.loadingSpinnerElement
          .setVisible(newStatus == null || newStatus == CommandStatus.PENDING);
      this.successElement
          .setVisible(newStatus == CommandStatus.SUCCESS)
          .setTooltip(data.message);
      this.errorElement
          .setVisible(newStatus == CommandStatus.ERROR)
          .setTooltip(data.message);
    }

    this.loadingSpinnerElement.setColour(c -> c.withAlpha(opacity));
    this.successElement.setColour(Colour.GREEN.withAlpha(opacity));
    this.errorElement.setColour(Colour.RED.withAlpha(opacity));

    super.screen.getMainElement().setMargin(new RectExtension(x, this.context.dimFactory.zeroGui(), y, this.context.dimFactory.zeroGui()));
  }
}
