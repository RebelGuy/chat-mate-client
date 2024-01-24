package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.style.Font;

import java.net.URI;

import static dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements.SCALE;

public class UrlElement extends LabelElement {
  private final String url;

  public UrlElement(InteractiveScreen.InteractiveContext context, IElement parent, String text, String url) {
    super(context, parent);
    this.url = url;

    super.setText(text);
    super.setColour(Colour.BLUE);
    super.setFontScale(SCALE);
    super.setHoverFont(new Font().withColour(new Colour(64, 64, 180)).withUnderlined(true));
    super.setOnClick(this::onOpenUrl);
  }

  private void onOpenUrl() {
    try {
      super.context.urlService.openUrl(new URI(this.url));
    } catch (Exception e) {
      super.context.logService.logError(this, String.format("Unable to open URL %s in the web browser", this.url), e);
    }
  }
}
