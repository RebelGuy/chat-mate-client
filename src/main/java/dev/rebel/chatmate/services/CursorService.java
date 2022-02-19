package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.Tick;
import dev.rebel.chatmate.stores.ChatMateEndpointStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class CursorService {
  private final Minecraft minecraft;
  private final LogService logService;
  private final ChatMateEndpointStore chatMateEndpointStore;
  private final ForgeEventService forgeEventService;
  private final Map<CursorType, Cursor> cursors;

  private Cursor currentCursor;
  private CursorType cursorType;

  public CursorService(Minecraft minecraft, LogService logService, ChatMateEndpointStore chatMateEndpointStore, ForgeEventService forgeEventService) {
    this.minecraft = minecraft;
    this.logService = logService;
    this.chatMateEndpointStore = chatMateEndpointStore;
    this.forgeEventService = forgeEventService;

    this.cursors = new HashMap<>();

    this.forgeEventService.onRenderTick(this::onRenderTick, null);
  }

  public void setCursor(CursorType type) {
    // todo: require that this is called every frame, then aggregate all set types and choose the most appropriate one.
    // e.g. if someone sets to default, and another one to click, obviously we want to show click.
    // if not set, use default
    this.cursorType = type;
  }

  private Tick.Out onRenderTick(Tick.In in) {
    CursorType type = this.cursorType == null ? CursorType.DEFAULT : this.cursorType;

    // if we have nothing better to show and are waiting for a request, use the waiting cursor
    if (type == CursorType.DEFAULT && this.chatMateEndpointStore.isWaitingForResponse()) {
      type = CursorType.WAIT;
    }

    Cursor cursor = this.getCursorInstance(type);
    if (cursor != null && this.currentCursor != cursor) {
      this.setCursor(cursor);
    }

    return new Tick.Out();
  }

  private void setCursor(Cursor cursor) {
    try {
      Mouse.setNativeCursor(cursor);
      this.currentCursor = cursor;

    } catch(Exception e){
      this.logService.logWarning(this, "Unable to set cursor:", e);
    }
  }

  private @Nullable Cursor getCursorInstance(CursorType type) {
    if (this.cursors.containsKey(type) && this.cursors.get(type) != null) {
      return this.cursors.get(type);
    }

    try {
      Asset.CursorImage cursorImage;
      switch (type) {
        case DEFAULT:
          cursorImage = Asset.CURSOR_DEFAULT;
          break;
        case TIP:
          cursorImage = Asset.CURSOR_TIP;
          break;
        case WAIT:
          cursorImage = Asset.CURSOR_WAIT;
          break;
        case TEXT:
          cursorImage = Asset.CURSOR_TEXT;
          break;
        case CLICK:
          cursorImage = Asset.CURSOR_CLICK;
          break;
        default:
          throw new RuntimeException("No cursor image exists for type " + type);
      }

      // from https://forums.minecraftforge.net/topic/38133-19solved-changing-cursor-icon-to-one-of-quotstandartquot-system-icons/
      IResource resource = this.minecraft.getResourceManager().getResource(cursorImage.resourceLocation);
      BufferedImage image = ImageIO.read(resource.getInputStream());
      int width = image.getWidth();
      int height = image.getHeight();

      IntBuffer imageBuffer = IntBuffer.wrap(image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
      IntBuffer imageBufferCopy = BufferUtils.createIntBuffer(imageBuffer.remaining());

      // The image will be drawn in reverse
      flipImage(width, height, 0, imageBuffer, imageBufferCopy);
      Cursor cursor = new Cursor(
          width,
          height,
          cursorImage.xHotspot,
          height - cursorImage.yHotspot - 1,
          1,
          imageBufferCopy,
          null);

      this.cursors.put(type, cursor);
      return cursor;
    } catch (Exception e) {
      this.logService.logWarning(this, "Unable to construct cursor:", e);
      return null;
    }
  }

  // stolen from Cursor::flipImage
  /**
   * @param width Width of image
   * @param height Height of images
   * @param start_index index into source buffer to copy to
   * @param images Source images
   * @param images_copy Destination images
   */
  private static void flipImage(int width, int height, int start_index, IntBuffer images, IntBuffer images_copy) {
    for (int y = 0; y < height>>1; y++) {
      int index_y_1 = y*width + start_index;
      int index_y_2 = (height - y - 1)*width + start_index;
      for (int x = 0; x < width; x++) {
        int index1 = index_y_1 + x;
        int index2 = index_y_2 + x;
        int temp_pixel = images.get(index1 + images.position());
        images_copy.put(index1, images.get(index2 + images.position()));
        images_copy.put(index2, temp_pixel);
      }
    }
  }

  public enum CursorType {
    DEFAULT,
    TIP,
    WAIT,
    TEXT,
    CLICK
  }
}
