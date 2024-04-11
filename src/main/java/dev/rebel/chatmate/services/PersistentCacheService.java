package dev.rebel.chatmate.services;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Thread-safe file caching manager that synchronises reading/writing operations that involve the same key. */
public class PersistentCacheService {
  private final LogService logService;
  private final FileService fileService;
  private final ConcurrentMap<String, CacheAccess> activeCaches;
  private final Object lock = new Object();

  public PersistentCacheService(LogService logService, FileService fileService) {
    this.logService = logService;
    this.fileService = fileService;
    this.activeCaches = new ConcurrentHashMap<>();
  }

  public void setValueForKey(String key, byte[] value, @Nullable Runnable callback) {
    this.getOrSetCache(key).scheduleWrite(value, callback);
  }

  /** Accepts a function that returns a default value, if no value for the given key exists. Returns null if the key does not exist. */
  public void getValueForKey(String key, @Nullable Supplier<byte[]> defaultValue, Consumer<byte[]> callback) {
    CacheAccess cache = this.getOrSetCache(key);
    cache.scheduleRead(defaultValue, callback);
  }

  private CacheAccess getOrSetCache(String key) {
    synchronized (this.lock) {
      if (!this.activeCaches.containsKey(key)) {
        this.activeCaches.put(key, new CacheAccess(key, () -> {
          synchronized (this.lock) {
            this.activeCaches.remove(key);
          }
        }));
      }

      return this.activeCaches.get(key);
    }
  }

  public class CacheAccess {
    private final String key;
    private final Runnable onDone;
    private final ConcurrentLinkedQueue<IOperation> scheduledOperations;
    private boolean hasStarted = false;
    private boolean isFinished = false;
    private final Object lock = new Object();

    public CacheAccess(String key, Runnable onDone) {
      this.key = key;
      this.onDone = onDone;
      this.scheduledOperations = new ConcurrentLinkedQueue<>();
    }

    public void scheduleWrite(byte[] value, @Nullable Runnable callback) {
      this.addOperation(new WriteOperation(this.key, value, callback));
    }

    public void scheduleRead(@Nullable Supplier<byte[]> defaultValue, Consumer<byte[]> callback) {
      this.addOperation(new ReadOperation(this.key, defaultValue, callback));
    }

    private void addOperation(IOperation operation) {
      synchronized (this.lock) {
        if (this.isFinished) {
          throw new RuntimeException("The scheduled operations queue is already done - can't schedule additional work.");
        }

        this.scheduledOperations.add(operation);
      }

      this.performOperations();
    }

    private void performOperations() {
      synchronized (this.lock) {
        if (this.hasStarted) {
          return;
        } else {
          this.hasStarted = true;
        }
      }

      new Thread(() -> {
        while (this.scheduledOperations.size() > 0) {
          IOperation operation = this.scheduledOperations.poll();
          try {
            operation.execute();
          } catch (Exception e) {
            PersistentCacheService.this.logService.logError(this, "Failed to perform operation", operation.getType(), "for key", this.key, e);
          }

          // if, at this point, nothing is scheduled, we are done and won't accept any more operations
          synchronized (this.lock) {
            if (this.scheduledOperations.size() == 0) {
              this.isFinished = true;
            }
          }
        }

        this.onDone.run();
      }).start();
    }
  }

  enum OperationType { READ, WRITE }

  interface IOperation {
    OperationType getType();
    void execute() throws IOException;
  }

  private class WriteOperation implements IOperation {
    private final String key;
    private final byte[] value;
    private final @Nullable Runnable callback;

    public WriteOperation(String key, byte[] value, @Nullable Runnable callback) {
      this.key = key;
      this.value = value;
      this.callback = callback;
    }

    @Override
    public OperationType getType() {
      return OperationType.WRITE;
    }

    @Override
    public void execute() throws IOException {
      PersistentCacheService.this.fileService.writeBinaryFile(this.key, this.value);

      if (this.callback != null) {
        this.callback.run();
      }
    }
  }

  private class ReadOperation implements IOperation {
    private final String key;
    @Nullable
    private final Supplier<byte[]> defaultValue;
    private final Consumer<byte[]> callback;

    public ReadOperation(String key, @Nullable Supplier<byte[]> defaultValue, Consumer<byte[]> callback) {
      this.key = key;
      this.defaultValue = defaultValue;
      this.callback = callback;
    }

    @Override
    public OperationType getType() {
      return OperationType.READ;
    }

    @Override
    public void execute() throws IOException {
      byte[] result = PersistentCacheService.this.fileService.readBinaryFile(this.key);

      if (result == null && this.defaultValue != null) {
        result = this.defaultValue.get();

        // cache the default value
        if (result != null) {
          PersistentCacheService.this.fileService.writeBinaryFile(this.key, result);
        }
      }

      this.callback.accept(result);
    }
  }
}