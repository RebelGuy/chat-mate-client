package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

public class WrapperElement extends SingleElement {
  private final IElement contents;

  public WrapperElement(InteractiveContext context, IElement parent, IElement contents) {
    super(context, parent);
    this.contents = contents;
    contents.setParent(this);
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return Collections.list(this.contents);
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    DimPoint fullChildSize = this.contents.calculateSize(maxContentSize);
    if (fullChildSize.getX().gt(maxContentSize) || this.getSizingMode() == SizingMode.FILL) {
      // use max size
      return new DimPoint(maxContentSize, fullChildSize.getY());
    } else {
      // smaller than max size
      return fullChildSize;
    }
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    DimRect labelBox = this.alignChild(this.contents);
    this.contents.setBox(labelBox);
  }

  @Override
  public void renderElement() {
    this.contents.render();
  }
}
