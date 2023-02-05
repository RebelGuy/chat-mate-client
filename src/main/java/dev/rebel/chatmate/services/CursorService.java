package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.ForgeEventService;
import dev.rebel.chatmate.util.EnumHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import scala.Tuple2;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.*;

public class CursorService {
  private final Minecraft minecraft;
  private final LogService logService;
  private final ForgeEventService forgeEventService;

  private final Map<CursorType, Cursor> cursors;
  private final Set<Tuple2<CursorType, Integer>> nextCursors;
  private final WeakHashMap<Object, Tuple2<CursorType, Integer>> toggledCursors; // deliberate weak map

  private Cursor currentCursor;
  private int toggleId;

  public CursorService(Minecraft minecraft, LogService logService, ForgeEventService forgeEventService) {
    this.minecraft = minecraft;
    this.logService = logService;
    this.forgeEventService = forgeEventService;

    this.nextCursors = new HashSet<>();
    this.toggledCursors = new WeakHashMap<>();

    this.cursors = new HashMap<>();

    // some cursor changes might take one frame to come into effect - this is a known and accepted limitation
    this.forgeEventService.onRenderTick(this::onRenderTick);
  }

  /** Use this for an interactive-based cursor modification.
   * Required to be called every frame. The cursor with the highest weight wins. Falls back to `DEFAULT`. */
  public void setCursorType(CursorType type, int priority) {
    this.nextCursors.add(new Tuple2<>(type, priority));
  }

  /** Returns a new key for untoggling this cursor. Will automatically add the cursor type to the render set until `untoggleCursor` is called. */
  public int toggleCursor(CursorType type, int priority) {
    int id = this.toggleId++;
    this.toggledCursors.put(id, new Tuple2<>(type, priority));
    return id;
  }

  /** Toggle with a custom key. Use the same key to untoggle the cursor. The cursor is automatically untoggled when the key is garbage collected. */
  public void toggleCursor(CursorType type, Object key, int priority) {
    this.toggledCursors.put(key, new Tuple2<>(type, priority));
  }

  public void untoggleCursor(@Nullable Object key) {
    this.toggledCursors.remove(key);
  }

  private void onRenderTick(Event<?> event) {
    CursorType type = this.getAndResetCursorType();
    Cursor cursor = this.getCursorInstance(type);

    // I think setting the cursor is expensive, so do it only if something changed
    if (cursor != null && this.currentCursor != cursor) {
      this.setCursor(cursor);
    }
  }

  private CursorType getAndResetCursorType() {
    CursorType nextType = CursorType.DEFAULT;
    int nextPriority = Integer.MIN_VALUE;

    // check one-time cursors
    for (Tuple2<CursorType, Integer> pair : this.nextCursors) {
      CursorType cursor = pair._1;
      int priority = pair._2;
      if (priority > nextPriority || priority == nextPriority && cursor.ordinal() > nextType.ordinal()) {
        nextType = cursor;
        nextPriority = priority;
      }
    }
    this.nextCursors.clear();

    // check toggled cursors
    for (Tuple2<CursorType, Integer> pair : this.toggledCursors.values()) {
      CursorType cursor = pair._1;
      int priority = pair._2;
      if (priority > nextPriority || priority == nextPriority && cursor.ordinal() > nextType.ordinal()) {
        nextType = cursor;
        nextPriority = priority;
      }
    }

    return nextType;
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
          throw EnumHelpers.<CursorType>assertUnreachable(type);
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
      this.logService.logError(this, "Unable to construct cursor:", e);
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

  /** Ordered according to priority. E.g. CLICK type will always take precedence over WAIT type. */
  public enum CursorType {
    DEFAULT,
    WAIT,
    TIP,
    TEXT,
    CLICK
  }
}
