package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardStore.SettingsPage;
import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;

/** When supplied to the constructor of the DashboardScreen, it will automatically show the screen with prefilled details */
public abstract class DashboardRoute {
  public final SettingsPage page;

  protected DashboardRoute(SettingsPage page) {
    this.page = page;
  }

  public abstract static class GeneralRoute extends DashboardRoute {
    public GeneralRoute() {
      super(SettingsPage.GENERAL);
    }
  }

  public abstract static class HudRoute extends DashboardRoute {
    public HudRoute() {
      super(SettingsPage.HUD);
    }
  }

  public abstract static class ChatRoute extends DashboardRoute {
    public ChatRoute() {
      super(SettingsPage.CHAT);
    }
  }

  public abstract static class DonationRoute extends DashboardRoute {
    public DonationRoute() {
      super(SettingsPage.DONATION);
    }
  }

  public static class LinkDonationRoute extends DonationRoute {
    public final PublicDonationData donation;

    public LinkDonationRoute(PublicDonationData donation) {
      this.donation = donation;
    }
  }

  public abstract static class DebugRoute extends DashboardRoute {
    public DebugRoute() {
      super(SettingsPage.DEBUG);
    }
  }
}
