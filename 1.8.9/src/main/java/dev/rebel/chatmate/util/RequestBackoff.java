package dev.rebel.chatmate.util;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.HttpException;

import javax.annotation.Nullable;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static dev.rebel.chatmate.util.Objects.ifClass;

public class RequestBackoff {
  private static final long MAX_LIFETIME_MS = 10 * 60 * 1000;
  private static final double MAX_BACKOFF_MS = 60 * 1000;
  private static final double MULTIPLIER_ON_SUCCESS = 0.1d;
  private static final double MULTIPLIER_ON_FAILURE = 2;

  private long lastResponse = 0;
  private @Nullable Double currentBackoff = null;

  public boolean canDispose() {
    long now = new Date().getTime();
    return this.lastResponse > 0 && now - this.lastResponse > MAX_LIFETIME_MS;
  }

  public void wait(Runnable callback) {
    long backoffMs = this.currentBackoff == null ? -1 : (long)(double)this.currentBackoff; // at this point you just have to laugh at it
    long now = new Date().getTime();
    long requiredBackoffMs = this.lastResponse + backoffMs - now;

    if (requiredBackoffMs <= 0) {
      callback.run();
    } else {
      ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
      exec.schedule(callback, requiredBackoffMs, TimeUnit.MILLISECONDS);
    }
  }

  public void onSuccess() {
    this.lastResponse = new Date().getTime();

    if (this.currentBackoff != null) {
      this.currentBackoff *= MULTIPLIER_ON_SUCCESS;

      if (this.currentBackoff < 10) {
        this.currentBackoff = null;
      }
    }
  }

  public void onError(Throwable error) {
    this.lastResponse = new Date().getTime();

    // only backoff from internal errors
    if (!(error instanceof HttpException)
        && !(error instanceof  ChatMateApiException)
        || ifClass(HttpException.class, error, e -> e.statusCode < 500)
        || ifClass(ChatMateApiException.class, error, e -> e.apiResponseError.errorCode < 500)
    ) {
      return;
    }

    if (this.currentBackoff == null) {
      this.currentBackoff = 10d; // why can't it automatically do the conversion ffs piece of crap
    } else {
      this.currentBackoff *= MULTIPLIER_ON_FAILURE;
      if (this.currentBackoff > MAX_BACKOFF_MS) {
        this.currentBackoff = MAX_BACKOFF_MS;
      }
    }
  }
}
