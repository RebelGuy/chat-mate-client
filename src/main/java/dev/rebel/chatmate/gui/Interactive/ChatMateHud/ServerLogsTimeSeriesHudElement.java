package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.Interactive.SingleElement;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.events.ServerLogEventService;
import dev.rebel.chatmate.services.util.Collections;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_ORANGE;
import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_RED;

public class ServerLogsTimeSeriesHudElement extends TransformedHudElementWrapper<ServerLogsTimeSeriesHudElement.TimeSeriesElement> {
  private final TimeSeriesElement timeSeriesElement;
  private final ServerLogEventService serverLogEventService;
  private final Config config;

  public ServerLogsTimeSeriesHudElement(InteractiveContext context, IElement parent, ServerLogEventService serverLogEventService, Config config) {
    super(context, parent);
    super.setCanDrag(true);
    super.setCanScale(true);
    super.setDefaultPosition(context.dimFactory.getMinecraftRect().getTopRight().setAnchor(DimAnchor.SCREEN), Anchor.TOP_RIGHT);
    this.serverLogEventService = serverLogEventService;
    this.config = config;

    this.timeSeriesElement = new TimeSeriesElement(context, this, serverLogEventService)
        .setMarkerSize(screen(8))
        .setMarkerAlpha(0.4f)
        .setTargetContentHeight(screen(16))
        .setMaxContentWidth(screen(140))
        .cast();
    super.setElement(this.timeSeriesElement);
  }

  @Override
  public void onRenderElement() {
    if (!this.config.getShowServerLogsTimeSeries().get() || !this.config.getDebugModeEnabledEmitter().get()) {
      return;
    }

    super.onRenderElement();
  }

  protected static class TimeSeriesElement extends SingleElement {
    private final ServerLogEventService serverLogEventService;

    private Dim markerSize;
    private float markerAlpha;

    public TimeSeriesElement(InteractiveContext context, IElement parent, ServerLogEventService serverLogEventService) {
      super(context, parent);
      this.serverLogEventService = serverLogEventService;
    }

    public TimeSeriesElement setMarkerSize(Dim markerSize) {
      if (!Objects.equals(this.markerSize, markerSize)) {
        this.markerSize = markerSize;
        super.onInvalidateSize();
      }
      return this;
    }

    public TimeSeriesElement setMarkerAlpha(float markerAlpha) {
      this.markerAlpha = markerAlpha;
      return this;
    }

    @Override
    public @Nullable List<IElement> getChildren() {
      return null;
    }

    @Override
    protected DimPoint calculateThisSize(Dim maxContentSize) {
      return new DimPoint(super.getMaxContentWidth(), super.getTargetContentHeight());
    }

    @Override
    protected void renderElement() {
      Long[] errors = this.serverLogEventService.getErrors();
      Long[] warnings = this.serverLogEventService.getWarnings();

      long maxX = new Date().getTime();
      long minX = maxX - 1000 * 60;

      List<Long> errorPoints = Collections.filter(Collections.list(errors), t -> t >= minX);
      List<Long> warningPoints = Collections.filter(Collections.list(warnings), t -> t >= minX);

      // get the centres of the indicators, where 0 corresponds to the top left of the screen, and 1 corresponds to the right side of the plot
      Function<Long, Double> getX = t -> ((double)t - minX) / (maxX - minX);
      List<DataPoint> dataPoints = Collections.map(errorPoints, t -> new DataPoint(getX.apply(t), STATUS_INDICATOR_RED));
      dataPoints.addAll(Collections.map(warningPoints, t -> new DataPoint(getX.apply(t), STATUS_INDICATOR_ORANGE)));
      dataPoints = Collections.orderBy(dataPoints, p -> p.x);

      // translate the plot based on the box coordinates
      Dim interiorWidth = super.getContentBox().getWidth().minus(this.markerSize.over(2).times(2));
      Dim yOffset = super.getContentBox().getHeight().minus(this.markerSize).over(2);
      DimPoint translation = new DimPoint(super.getBox().getX(), super.getBox().getY().plus(yOffset));

      // data points are drawn according to their top-left corner's position
      for (DataPoint point : dataPoints) {
        Dim pointOffset = interiorWidth.times((float)point.x);
        DimPoint pointTranslation = new DimPoint(translation.getX().plus(pointOffset), translation.getY());
        float scale = this.markerSize.over(gui(point.texture.width));
        float alpha;
        if (point.x < 0.25) {
          alpha = (float)point.x / 0.25f * this.markerAlpha;
        } else {
          alpha = this.markerAlpha;
        }
        GL11.glDisable(GL11.GL_ALPHA_TEST); // required for smooth alpha transitions
        RendererHelpers.drawTexture(super.context.minecraft.getTextureManager(), super.context.dimFactory, point.texture, pointTranslation, scale, Colour.WHITE.withAlpha(alpha));
        GL11.glEnable(GL11.GL_ALPHA_TEST);
      }
    }

    private static class DataPoint {
      public final double x;
      public final Asset.Texture texture;

      public DataPoint(double x, Asset.Texture texture) {
        this.x = x;
        this.texture = texture;
      }
    }
  }
}
