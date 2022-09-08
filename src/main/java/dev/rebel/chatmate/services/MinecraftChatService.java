package dev.rebel.chatmate.services;

import dev.rebel.chatmate.gui.chat.ContainerChatComponent;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import net.minecraft.util.IChatComponent;

import java.util.Objects;

public class MinecraftChatService {
  private final MinecraftProxyService minecraftProxyService;

  public MinecraftChatService(MinecraftProxyService minecraftProxyService) {
    this.minecraftProxyService = minecraftProxyService;
  }

  public void clearChatMessagesByUser(PublicUser user) {
    this.minecraftProxyService.getChatGUI().deleteLine(line -> {
      for (IChatComponent component : line.getChatComponent()) {
        // thanks, Java
        if (!(component instanceof ContainerChatComponent)) {
          continue;
        }
        ContainerChatComponent container = (ContainerChatComponent) component;
        if (!(container.getData() instanceof PublicUser)) {
          continue;
        }

        // as it stands, this is a bit hacky because it doesn't necessarily remove ONLY stream messages, but for now it works.
        // to make this more future-proof, we would need to add some kind of type/tag to Abstract Lines for categorising them.
        PublicUser thisUser = (PublicUser) container.getData();
        return Objects.equals(thisUser.id, user.id);
      }

      return false;
    });
  }
}
