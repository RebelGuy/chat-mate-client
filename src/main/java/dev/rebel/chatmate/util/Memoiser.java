package dev.rebel.chatmate.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Memoiser {
  Map<String, MemoisedResult> memoised = new HashMap<>();

  /** Memoise the key-value pair (where the value is returned by `fn`) against the given `args`. */
  public <Output> Output memoise(String key, Supplier<Output> fn, Object... args) {
    Input input = new Input(args);
    MemoisedResult result = this.memoised.get(key);

    if (result != null && result.input.equals(input)) {
      return (Output)result.output;
    } else {
      Output output = fn.get();
      this.memoised.put(key, new MemoisedResult(input, output));
      return output;
    }
  }

  public void clear() {
    this.memoised.clear();
  }

  private static class MemoisedResult {
    public Input input;
    public Object output;

    public MemoisedResult(Input input, Object output) {
      this.input = input;
      this.output = output;
    }
  }

  private static class Input {
    private Object[] args;

    public Input(Object[] args) {
      this.args = args;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Input input = (Input)o;
      return Arrays.equals(args, input.args);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(args);
    }
  }
}
