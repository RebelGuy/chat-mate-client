package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DashboardStore {
  private SettingsPage settingsPage;

  private List<Consumer<SettingsPage>> onSettingsPageChange;

  public DashboardStore(){
    this.settingsPage = SettingsPage.GENERAL;
    this.onSettingsPageChange = new ArrayList<>();
  }

  public void setSettingsPage(SettingsPage settingsPage) {
    if (this.settingsPage != settingsPage) {
      this.settingsPage = settingsPage;
      this.onSettingsPageChange.forEach(cb -> cb.accept(settingsPage));
    }
  }

  public SettingsPage getSettingsPage() {
    return this.settingsPage;
  }

  public void onSettingsPageChange(Consumer<SettingsPage> callback) {
    this.onSettingsPageChange.add(callback);
  }

  public void offSettingsPageChange(Consumer<SettingsPage> callback) {
    this.onSettingsPageChange.remove(callback);
  }

  public enum SettingsPage {
    GENERAL, HUD
  }
}
