package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TextHudElement extends HudElement {
  private LabelElement labelElement;

  public TextHudElement(InteractiveContext context, IElement parent) {
    super(context, parent);
    super.setCanDrag(true);
    super.setCanScale(true);

    this.labelElement = new LabelElement(context, this);
  }

  public @Nonnull String getText() {
    return this.labelElement.getText();
  }

  public void setText(@Nonnull String text) {
    this.labelElement.setText(text);
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return Collections.list(this.labelElement);
  }

  @Override
  protected void onElementRescaled(float oldScale, float newScale) {
    this.labelElement.setFontScale(newScale);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return this.labelElement.calculateSize(maxContentSize);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    this.labelElement.setBox(box);
  }

  @Override
  public void onRenderElement() {
    this.labelElement.render(null);
  }
}
