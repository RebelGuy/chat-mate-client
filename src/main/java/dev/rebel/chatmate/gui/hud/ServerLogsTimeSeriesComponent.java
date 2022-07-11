package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.events.ServerLogEventService;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_ORANGE;
import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_RED;

// alternate component for log errors as a time series over the past minute.
public class ServerLogsTimeSeriesComponent extends Box implements IHudComponent {
  private final ServerLogEventService serverLogEventService;
  private final Config config;

  private final Dim plotWidth;
  private final Dim plotHeight;
  private final Dim markerSize;
  private final float markerAlpha;

  public ServerLogsTimeSeriesComponent(DimFactory dimFactory, ServerLogEventService serverLogEventService, Config config) {
    super(dimFactory, dimFactory.zeroGui(), dimFactory.zeroGui(), dimFactory.zeroGui(), dimFactory.zeroGui(), false, false);

    this.serverLogEventService = serverLogEventService;
    this.config = config;

    this.plotWidth = dimFactory.fromScreen(70);
    this.plotHeight = dimFactory.fromScreen(8);
    this.markerSize = dimFactory.fromScreen(4);
    this.markerAlpha = 0.2f;

    this.setTopRightPosition();
  }

  private void setTopRightPosition() {
    Dim topPadding = this.plotHeight.minus(this.markerSize).over(2);
    super.setRect(dimFactory.getMinecraftSize().getX().minus(this.plotWidth).minus(topPadding), dimFactory.fromScreen(0), this.plotWidth, this.plotHeight);
  }

  public void onMinecraftResize() {
    this.setTopRightPosition();
  }

  @Override
  public float getContentScale() {
    return 1;
  }

  @Override
  public boolean canRescaleContent() {
    return false;
  }

  @Override
  public void onRescaleContent(float newScale) { }

  @Override
  public void render(RenderContext context) {
    if (!this.config.getShowServerLogsTimeSeries().get()) {
      return;
    }

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
    Dim interiorWidth = this.plotWidth.minus(this.markerSize.over(2).times(2));
    Dim yOffset = this.plotHeight.minus(this.markerSize).over(2);
    DimPoint translation = new DimPoint(this.getX(), this.getY().plus(yOffset));

    // data points are drawn according to their top-left corner's position
    for (DataPoint point : dataPoints) {
      Dim pointOffset = interiorWidth.times((float)point.x);
      DimPoint pointTranslation = new DimPoint(translation.getX().plus(pointOffset), translation.getY());
      float scale = this.markerSize.over(super.dimFactory.fromGui((float)point.texture.width));
      GlStateManager.enableBlend();
      GlStateManager.color(1, 1, 1, 0.2f);
      float alpha;
      if (point.x < 0.25) {
        alpha = (float)point.x / 0.25f * this.markerAlpha;
      } else {
        alpha = this.markerAlpha;
      }
      RendererHelpers.drawTexture(context.textureManager, super.dimFactory, point.texture, pointTranslation, scale, Colour.WHITE.withAlpha(alpha));
    }
  }

  private static class DataPoint {
    public final double x;
    public final Texture texture;

    public DataPoint(double x, Texture texture) {
      this.x = x;
      this.texture = texture;
    }
  }
}
