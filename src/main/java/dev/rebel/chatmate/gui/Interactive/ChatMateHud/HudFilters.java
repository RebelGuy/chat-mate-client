package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.Objects;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;
import java.util.List;

import static dev.rebel.chatmate.services.util.Objects.casted;

/** For configuring when a particular HUD element should be visible. */
public class HudFilters {
  public interface IHudFilter {
    // if a list of items from a category are whitelisted, then the remainder of the items in that category are not necessarily blacklisted.
    // if a list of items from a category are blacklisted, then the remainder of the items in that category are necessarily whitelisted.

    /** Null means that the filter does not offer an opinion on whether the element should be whitelisted or not. */
    @Nullable <T extends GuiScreen> Boolean isWhitelisted(T currentScreen);
    /** Null means that the filter does not offer an opinion on whether the element should be blacklisted or not. */
    @Nullable <T extends GuiScreen> Boolean isBlacklisted(T currentScreen);
  }

  public static class HudFilterWhitelistNoScreen implements IHudFilter {
    @Override
    public <T extends GuiScreen> Boolean isWhitelisted(T currentScreen) {
      return currentScreen == null;
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isBlacklisted(T currentScreen) {
      return null;
    }
  }

  public static class HudFilterWhitelistAnyScreen implements IHudFilter {
    @Override
    public <T extends GuiScreen> Boolean isWhitelisted(T currentScreen) {
      return currentScreen != null;
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isBlacklisted(T currentScreen) {
      return null;
    }
  }

  public static class HudFilterScreenWhitelist implements IHudFilter {
    private final List<Class<? extends GuiScreen>> screenTypes;

    @SafeVarargs
    public HudFilterScreenWhitelist(Class<? extends GuiScreen>... screenTypes) {
      this.screenTypes = Collections.list(screenTypes);
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isWhitelisted(@Nullable T currentScreen) {
      if (currentScreen == null) {
        return false;
      }

      Class<? extends GuiScreen> clazz = currentScreen.getClass();
      for (Class<? extends GuiScreen> whitelist : this.screenTypes) {
        // if clazz is a superclass, it will NOT match - this is intentional.
        // in the future, we could extend this behaviour by using isAssignableFrom instead
        if (whitelist.isInstance(currentScreen)) {
          return true;
        }
      }

      return false;
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isBlacklisted(T currentScreen) {
      return null;
    }
  }

  public static class HudFilterInteractiveScreenTypeWhitelist implements IHudFilter {
    private final List<InteractiveScreenType> types;

    public HudFilterInteractiveScreenTypeWhitelist(InteractiveScreenType... whitelistedTypes) {
      this.types = Collections.list(whitelistedTypes);
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isWhitelisted(T currentScreen) {
      return casted(InteractiveScreen.class, currentScreen, screen -> this.types.contains(screen.interactiveScreenType));
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isBlacklisted(T currentScreen) {
      return null;
    }
  }

  public static class HudFilterInteractiveScreenTypeBlacklist implements IHudFilter {
    private final List<InteractiveScreenType> types;

    public HudFilterInteractiveScreenTypeBlacklist(InteractiveScreenType... blacklistedTypes) {
      this.types = Collections.list(blacklistedTypes);
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isWhitelisted(T currentScreen) {
      @Nullable Boolean isBlacklisted = this.isBlacklisted(currentScreen);
      return isBlacklisted == null ? null : !isBlacklisted;
    }

    @Override
    public @Nullable <T extends GuiScreen> Boolean isBlacklisted(T currentScreen) {
      return casted(InteractiveScreen.class, currentScreen, screen -> this.types.contains(screen.interactiveScreenType));
    }
  }
}
