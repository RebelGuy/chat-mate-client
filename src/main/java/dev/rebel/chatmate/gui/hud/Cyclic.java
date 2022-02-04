package dev.rebel.chatmate.gui.hud;

/** Represents a cyclic number line that is constrained to [min, max). */
public class Cyclic {
  private final float min;
  private final float max;
  private final float range;

  public Cyclic(float min, float max) {
    if (min >= max) {
      throw new RuntimeException("Min must be less than max");
    }

    this.min = min;
    this.max = max;
    range = max - min;
  }

  public float clamp(float x) {
    float result;
    if (x < this.min) {
      // ensure `min - range <= x < min`
      x = x + (int)((this.min - x) / this.range) * this.range;
      result = this.max - (this.min - x);
    } else if (x > this.max) {
      // ensure `max < x <= max + range`
      x = x - (int)((x - this.max) / this.range) * this.range;
      result = this.min + (x - this.max);
    } else {
      result = x;
    }

    return result == this.max ? this.min : result;
  }

  public float add(float a, float b) {
    return this.clamp(a + b);
  }

  /** Returns the signed value that minimises the distance between `from` and `to`. */
  public float distance(float from, float to) {
    from = this.clamp(from);
    to = this.clamp(to);

    if (Math.abs(to - from) < this.min + this.range / 2) {
      // do not loop
      return to - from;
    } else if (from < to) {
      // loop backwards
      return -(from - this.min + this.max - to);
    } else {
      // loop forwards
      return to - this.min + this.max - from;
    }
  }

  /** Maps the normalised value into the range. */
  public float map(float norm) {
    norm = new Cyclic(0, 1).clamp(norm);
    return this.min + norm * this.range;
  }

  /** Normalises the value. */
  public float unmap(float value) {
    value = this.clamp(value);
    return (value - this.min) / this.range;
  }
}
