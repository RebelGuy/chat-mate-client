package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceRequest;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse.ModifyExperienceResponseData;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.services.McChatService;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.TextHelpers.isNullOrEmpty;

public class ManageExperienceModal extends ModalElement {
  private final PublicUser user;
  private final ExperienceEndpointProxy experienceEndpointProxy;
  private final McChatService mcChatService;

  private SideBySideElement levelSbs;
  private SideBySideElement msgSbs;

  private @Nullable Float currentLevel = null;
  private String currentMessage = null;

  public ManageExperienceModal(InteractiveScreen.InteractiveContext context, InteractiveScreen parent, PublicUser user, ExperienceEndpointProxy experienceEndpointProxy, McChatService mcChatService) {
    super(context, parent);
    this.user = user;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.mcChatService = mcChatService;
  }

  @Override
  public void onInitialise() {
    super.onInitialise();

    this.levelSbs = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(gui(10))
        .addElement(1,
            new LabelElement(context, this)
                .setText("Add level:")
                .setOverflow(TextOverflow.TRUNCATE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(1,
            new TextInputElement(context, this)
                .onTextChange(this::onLevelChange)
                .setTabIndex(0)
                .setAutoFocus(true)
        ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
        );

    this.msgSbs = (SideBySideElement)new SideBySideElement(context, this)
        .setElementPadding(gui(10))
        .addElement(1,
            new LabelElement(context, this)
                .setText("Message:")
                .setOverflow(TextOverflow.TRUNCATE)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(1,
            new TextInputElement(context, this)
                .onTextChange(this::onMessageChange)
                .setTabIndex(1)
        ).setPadding(new RectExtension(ZERO, ZERO, ZERO, gui(5))
    );

    super.setBody(new BlockElement(context, this).addElement(this.levelSbs).addElement(this.msgSbs));
    super.setTitle("Manage Experience for " + user.channel.displayName);
  }

  private void onLevelChange(String maybeLevel) {
    try {
      float level = Float.parseFloat(maybeLevel);
      if (level != 0 && Math.abs(level) <= 100) {
        this.currentLevel = level;
      } else {
        this.currentLevel = null;
      }
    } catch (Exception ignored) {
      this.currentLevel = null;
    }
  }

  private void onMessageChange(String message) {
    this.currentMessage = message;
  }

  private void onModifyExperienceResponse(ModifyExperienceResponseData response, Runnable callback) {
    this.mcChatService.printInfo("Successfully modified experience for " + response.updatedUser.channel.displayName + ".");
    callback.run();
    super.onCloseScreen();
  }

  private void onModifyExperienceError(Throwable error, Consumer<String> callback) {
    this.mcChatService.printError(error);

    String msg = EndpointProxy.getApiErrorMessage(error);
    callback.accept(msg);
  }

  @Override
  protected @Nullable Boolean validate() {
    return this.currentLevel != null && Math.abs(this.currentLevel) <= 100 && this.currentLevel != 0;
  }

  @Override
  protected void submit(Runnable onSuccess, Consumer<String> onError) {
    String msg = isNullOrEmpty(this.currentMessage) ? null : this.currentMessage.trim();
    ModifyExperienceRequest request = new ModifyExperienceRequest(this.user.primaryUserId, this.currentLevel, msg);
    this.experienceEndpointProxy.modifyExperienceAsync(
        request,
        res -> this.onModifyExperienceResponse(res, onSuccess),
        err -> this.onModifyExperienceError(err, onError)
    );
  }
}
