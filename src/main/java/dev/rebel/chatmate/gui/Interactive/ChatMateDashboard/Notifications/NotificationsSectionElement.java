package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Notifications;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute.NotificationsRoute;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.stores.ApiStore;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.*;

public class NotificationsSectionElement extends ContainerElement implements ChatMateDashboardElement.ISectionElement {
  private final LabelElement nothingToShowLabel;
  private final List<ApiStoreErrorNotification> apiStoreErrorNotificationElements;

  public NotificationsSectionElement(InteractiveScreen.InteractiveContext context, IElement parent, @Nullable NotificationsRoute route) {
    super(context, parent, LayoutMode.BLOCK);

    super.registerAllStores(true);

    this.nothingToShowLabel = INFO_LABEL.create(context, this)
        .setText("There are currently no notifications.");
    this.apiStoreErrorNotificationElements = new ArrayList<>();
    this.apiStoreErrorNotificationElements.add(new ApiStoreErrorNotification(context, this, context.streamerApiStore, "streamers"));
    this.apiStoreErrorNotificationElements.add(new ApiStoreErrorNotification(context, this, context.livestreamApiStore, "livestreams"));
    this.apiStoreErrorNotificationElements.add(new ApiStoreErrorNotification(context, this, context.donationApiStore, "donations"));

    super.addElement(this.nothingToShowLabel);
    this.apiStoreErrorNotificationElements.forEach(super::addElement);
  }

  @Override
  protected void onStoreUpdate() {
    boolean hasErrors = Collections.any(this.apiStoreErrorNotificationElements, ApiStoreErrorNotification::hasError);
    this.nothingToShowLabel.setVisible(!hasErrors);
    this.apiStoreErrorNotificationElements.forEach(ApiStoreErrorNotification::onStoreUpdate);
  }

  @Override
  public void onShow() {
    this.onStoreUpdate();
  }

  @Override
  public void onHide() {

  }

  private static class ApiStoreErrorNotification extends ContainerElement {
    private final ApiStore<?> apiStore;
    private final String storeName;
    private final LabelElement errorLabel;
    private final TextButtonElement retryButton;
    private final TextButtonElement dismissButton;

    public ApiStoreErrorNotification(InteractiveScreen.InteractiveContext context, IElement parent, ApiStore<?> apiStore, String storeName) {
      super(context, parent, LayoutMode.INLINE);
      this.apiStore = apiStore;
      this.storeName = storeName;

      super.registerStore(apiStore, true);
      super.setPadding(RectExtension.fromBottom(gui(4)));
      super.setAllowShrink(true);

      this.errorLabel = ERROR_LABEL.create(context, this)
          .setSizingMode(SizingMode.MINIMISE)
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .setMinWidth(gui(24))
          .cast();
      this.retryButton = TEXT_BUTTON_LIGHT.create(context, this)
          .setText("Retry")
          .setTextScale(SCALE)
          .setOnClick(this::onRetry)
          .setMargin(RectExtension.fromLeft(gui(4)))
          .setHorizontalAlignment(HorizontalAlignment.RIGHT)
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .cast();
      this.dismissButton = TEXT_BUTTON_LIGHT.create(context, this)
          .setText("Dismiss")
          .setTextScale(SCALE)
          .setOnClick(this::onDismissError)
          .setMargin(RectExtension.fromLeft(gui(4)))
          .setHorizontalAlignment(HorizontalAlignment.RIGHT)
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .cast();

      super.addElement(this.errorLabel);
      super.addElement(this.retryButton);
      super.addElement(this.dismissButton);

      this.onStoreUpdate();
    }

    private void onRetry() {
      this.apiStore.retry();
    }

    private void onDismissError() {
      this.apiStore.dismissError();
    }

    public boolean hasError() {
      return this.apiStore.getError(true) != null;
    }

    public void onShow() {
      this.onStoreUpdate();
    }

    @Override
    protected void onStoreUpdate() {
      this.retryButton.setEnabled(this, !this.apiStore.isLoading());

      @Nullable String error = this.apiStore.getError(true);
      if (error == null) {
        this.setVisible(false);
        return;
      }

      this.setVisible(true);
      this.errorLabel.setText(String.format("Failed to load %s: %s", this.storeName, error));
    }
  }
}
