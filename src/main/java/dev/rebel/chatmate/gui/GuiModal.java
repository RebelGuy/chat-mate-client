package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.builder.TableLayout;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import scala.Function2;
import scala.Tuple2;
import scala.Tuple4;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static dev.rebel.chatmate.services.util.TextHelpers.splitText;

public class GuiModal extends GuiScreen {
  private static final int SCREEN_MIN_PADDING = 50;
  private static final int MODAL_PADDING = 10;
  private static final int TITLE_CONTENT_PADDING = 30;
  private static final int CONTENT_ACTIONS_PADDING = 30;

  private final Minecraft minecraft;
  private final TableLayout content;

  private final List<String> titleLines;
  private final Tuple4<Dim, Dim, Dim, Dim> rect;

  public GuiModal(
      Minecraft minecraft,
      DimFactory dimFactory,
      FontEngine fontEngine,
      String title,
      ModalSizing sizing,
      @Nullable Runnable onCancel,
      @Nullable Runnable onSubmit,
      @Nullable Supplier<Boolean> onValidateInput,
      Function2<List<GuiButton>, List<GuiLabel>, TableLayout> contentCreator
    ) {
    super();
    this.minecraft = minecraft;
    this.content = contentCreator.apply(this.buttonList, this.labelList);

    this.titleLines = splitText(title, content.width, fontEngine);
    this.rect = getRect(dimFactory, sizing, fontEngine, this.titleLines.size(), content.width, content.getTotalHeight());
  }

  @Override
  public void setWorldAndResolution(Minecraft mc, int width, int height) {
    // for some reason only the buttonList is cleared, but not the labelList
    this.labelList.clear();
    super.setWorldAndResolution(mc, width, height);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    this.content.onActionPerformed(button);
  }

  @Override
  protected void mouseClicked(int x, int y, int mouseEvent) throws IOException {
    super.mouseClicked(x, y, mouseEvent);
    this.content.onPostMousePressed(x, y);
  }

  @Override
  protected void mouseClickMove(int x, int y, int mouseButton, long timeSinceLastClick) {
    super.mouseClickMove(x, y, mouseButton, timeSinceLastClick);
    this.content.onPostMouseDragged(x, y);
  }

  @Override
  protected void mouseReleased(int x, int y, int mouseEvent) {
    super.mouseReleased(x, y, mouseEvent);
    this.content.onPostMouseReleased(x, y);
  }

  /** Returns the x, y, width, and height values for the modal. */
  private static Tuple4<Dim, Dim, Dim, Dim> getRect(DimFactory dimFactory, ModalSizing sizing, FontEngine font, int titleLines, int contentWidth, int contentHeight) {
    DimPoint minecraftDim = dimFactory.getMinecraftSize();

    Dim maxWidth = minecraftDim.getX().minus(dimFactory.fromGui(SCREEN_MIN_PADDING));
    Dim maxHeight = minecraftDim.getY().minus(dimFactory.fromGui(SCREEN_MIN_PADDING));

    Dim requiredWidth = dimFactory.fromGui(contentWidth);
    Dim requiredHeight = dimFactory.fromGui(font.FONT_HEIGHT * titleLines + TITLE_CONTENT_PADDING + contentHeight + CONTENT_ACTIONS_PADDING);

    Dim actualWidth, actualHeight;
    if (sizing == ModalSizing.FIT) {
      actualWidth = requiredWidth.gt(maxWidth) ? maxWidth : requiredWidth;
      actualHeight = requiredHeight.gt(maxHeight) ? maxHeight : requiredHeight;
    } else {
      throw new RuntimeException("Invalid ModalSizing " + sizing);
    }

    Dim x = minecraftDim.getX().over(2).minus(actualWidth.over(2));
    Dim y = minecraftDim.getY().over(2).minus(actualHeight.over(2));
    return new Tuple4<>(x, y, actualWidth, actualHeight);
  }

  @Override
  public void initGui() {

  }

  public enum ModalSizing {
    FIT, // LARGE, MEDIUM, SMALL
  }
}
