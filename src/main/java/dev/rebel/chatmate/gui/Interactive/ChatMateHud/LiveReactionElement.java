package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.ImageElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.ResolvableTexture;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LiveReactionElement extends HudElement {
  private final static Random RANDOM = new Random();

  private final static long ANIMATION_TIME = 1000;
  private final AnimatedBool animation;
  private final ResolvableTexture image;
  private final ImageElement imageElement;

  // these values are all relative to the screen rect
  private final Dim xStart;
  private final Dim xEnd;

  private final Dim yEnd;

  private final Dim sizeStart;
  private final Dim sizeEnd;

  private final float lifetime;

  // this should only be updated in the render method to ensure that all sizes are kept consistent during box calculations
  private float lastFrac = 0;

  public LiveReactionElement(InteractiveContext context, IElement parent, ResolvableTexture image, Consumer<LiveReactionElement> onFinishAnimation) {
    super(context, parent);
    super.setCanDrag(false);
    super.setCanScale(false);

    this.xStart = gui(new RandomNumberGenerator(10, 10, 0f, null).getNumber());
    this.xEnd = gui(new RandomNumberGenerator(40, 10, this.xStart.getGui(), null).getNumber());

    this.yEnd = gui(new RandomNumberGenerator(150, 50, 50f, null).getNumber());

    this.sizeStart = gui(new RandomNumberGenerator(7, 5, 5f, null).getNumber());
    this.sizeEnd = gui(new RandomNumberGenerator(15, 5, 10f, null).getNumber());

    this.lifetime = new RandomNumberGenerator(2000, 500, 1500f, null).getNumber();

    this.animation = new AnimatedBool((long)this.lifetime, false, value -> onFinishAnimation.accept(this));
    this.animation.set(true);
    this.imageElement = new ImageElement(context, this);
    this.image = image;
  }

  @Override
  public void onInitialise() {
    super.onInitialise();

    // don't do this in the constructor because it may not run on the Minecraft thread
    this.imageElement.setImage(image);
  }

  private float interpolate(float from, float to, @Nullable Supplier<Float> easingFunction) {
    float frac = easingFunction == null ? this.lastFrac : easingFunction.get();
    return frac * (to - from) + from;
  }

  private Dim interpolate(Dim from, Dim to, @Nullable Supplier<Float> easingFunction) {
    float frac = easingFunction == null ? this.lastFrac : easingFunction.get();
    return to.minus(from).times(frac).plus(from);
  }

  private float getYFrac_easeInCubic() {
    return this.lastFrac * this.lastFrac * this.lastFrac;
  }

  private float getYFrac_easeOutCubic() {
    return 1 - (float)Math.pow(1 - this.lastFrac, 3);
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return Collections.list(this.imageElement);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    Dim size = this.interpolate(sizeStart, sizeEnd, null);
    this.imageElement.setMaxWidth(size);

    return this.imageElement.calculateSize(maxContentSize);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    DimPoint bottomLeft = super.context.dimFactory.getMinecraftRect().getBottomLeft();
    Dim boxTop = this.interpolate(bottomLeft.getY(), bottomLeft.getY().minus(this.yEnd), this::getYFrac_easeInCubic);
    Dim boxRight = this.interpolate(this.xStart, this.xEnd, this::getYFrac_easeOutCubic).minus(this.sizeStart);

    Dim size = this.interpolate(this.sizeStart, this.sizeEnd, null);
    box = box.withSize(new DimPoint(size, size)).withTop(boxTop).withLeft(boxRight);
    this.imageElement.setBox(box);

    // since we are completely customising the box, we must override the internal state
    super.setBoxUnsafe(box);
  }

  @Override
  public void onRenderElement() {
    this.lastFrac = this.animation.getFrac();

    if (lastFrac < 1) {
      super.context.renderer.runSideEffect(super::onInvalidateSize);
    }

    float alpha = this.interpolate(2, 0, this::getYFrac_easeInCubic);
    this.imageElement.setColour(Colour.WHITE.withAlpha(Math.min(1, alpha)));

    this.imageElement.render(null);
  }

  private static class RandomNumberGenerator {
    private final float mean;
    private final float std;
    private final @Nullable Float min;
    private final @Nullable Float max;

    public RandomNumberGenerator(float mean, float std) {
      this(mean, std, null, null);
    }

    public RandomNumberGenerator(float mean, float std, @Nullable Float min, @Nullable Float max) {
      this.mean = mean;
      this.std = std;
      this.min = min;
      this.max = max;

      if (this.min != null && this.max != null && this.min > this.max) {
        throw new RuntimeException("Min cannot be greater than max");
      }
    }

    public float getNumber() {
      float number = (float)LiveReactionElement.RANDOM.nextGaussian() * this.std + this.mean;

      if (this.min != null && number < this.min) {
        return this.min;
      } else if (this.max != null && number > this.max) {
        return this.max;
      } else {
        return number;
      }
    }
  }
}
