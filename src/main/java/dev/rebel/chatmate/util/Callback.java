package dev.rebel.chatmate.util;

// stolen from https://stackoverflow.com/a/45419418
// why the fuck isn't this included in java
/** Represents a function with no input and output arguments. */
@FunctionalInterface
public interface Callback {
  void call();

  default Callback andThen(Callback after){
    return () -> {
      this.call();
      after.call();
    };
  }

  default Callback compose(Callback before){
    return () -> {
      before.call();
      this.call();
    };
  }
}


